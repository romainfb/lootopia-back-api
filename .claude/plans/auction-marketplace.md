# Plan d'implémentation — Marketplace d'enchères d'artefacts

> Document destiné à un agent d'implémentation. Suis les étapes dans l'ordre, chaque étape correspond à un commit. Ne
> dévie pas du périmètre. Si une décision n'est pas explicite ici, **arrête-toi et demande**.

---

## 0. Contexte et conventions du projet

**Stack** : Spring Boot 3.4.2, Java 21, Postgres, Liquibase, JPA, Lombok, JWT (resource server), JUnit 5 + Mockito,
MockMvc.

**Architecture hexagonale** déjà en place :

- `domain/model/` — POJOs Lombok `@Data @Builder @AllArgsConstructor @NoArgsConstructor`
- `application/port/in/` — interfaces UseCase (entrée)
- `application/port/out/` — interfaces PersistencePort (sortie)
- `application/service/` — implémentations
- `infrastructure/in/rest/` — controllers, DTOs, mappers REST, exceptions
- `infrastructure/out/persistance/` — entities JPA, repositories, mappers
- `config/` — config Spring

**Conventions à respecter strictement :**

- Tables et colonnes DB en français (existant : `utilisateur`, `artefact`, `solde_couronnes`, etc.). On garde cette
  convention pour les nouvelles tables.
- Code Java en anglais (classes, méthodes, variables).
- Mappers statiques (`toDomain` / `toEntity`).
- Pas de Lombok @Data sur les entités JPA avec relations bidirectionnelles (risque de boucle equals/hashCode) — on
  utilisera `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` pour les nouvelles entités complexes.
- JWT subject = userId (`Long.valueOf(jwt.getSubject())`).
- Exceptions custom : `ResourceNotFoundException`, `InvalidParameterException`, `UnauthorizedAccessException`,
  `ActionNotAllowedException`.
- Tests services : Mockito `@Mock` + `@InjectMocks`, `MockitoAnnotations.openMocks(this)` dans `@BeforeEach`.
- Tests controllers : `MockMvcBuilders.standaloneSetup(...)`.
- Liquibase : un fichier XML par changeset, ajouté au `db.changelog-master.xml`.

**Existant à réutiliser :**

- `UserEntity.balance` (Integer, mappé sur `solde_couronnes`) — c'est notre monnaie.
- `ArtifactEntity.userId` — propriétaire actuel d'un artefact.
- `TransactionEntity` — historique financier (montant BigDecimal, type, date, artefact, utilisateur).

**Existant à retenir (vérifié) :**

- `UserPersistencePort.findById(Long id)` retourne **`Optional<UserEntity>`** (pas le domain `User`). Cohérent avec
  l'existant : on manipule directement `UserEntity` (`getBalance() / setBalance()`) puis `userPort.save(entity)`. **Ne
  pas chercher à passer par le domain model `User` pour les opérations de balance**, ça serait incohérent avec le port.
- `TransactionPersistencePort` ne contient actuellement QUE `findByUserId`. Pas de `save`. Il faudra **l'étendre avec
  une méthode `save`** (cf commit 3) ainsi que son adapter et le repository.
- `Transaction` (domain) référence directement `UserEntity` et `ArtifactEntity` dans ses champs (cf `Transaction.java`
  ligne 19/23). C'est l'existant — on s'aligne dessus.

---

## 1. Spécification fonctionnelle (figée — ne pas négocier)

### Type d'enchère

**Anglaise classique** (montante publique). Pas de proxy bidding pour le MVP.

### Cycle de vie

```
SCHEDULED ── starts_at atteint ──► OPEN ── ends_at + dernier bid ──► CLOSED ─► SETTLED
                                     │
                                     └── seller annule (avant tout bid) ──► CANCELLED
```

### Règles métier

1. **Création** : un user crée une enchère sur un de ses artefacts (`artifact.userId == seller.id`). L'artefact ne peut
   pas être déjà en vente (= il n'existe pas d'enchère active dont c'est l'objet). À la création : `status = SCHEDULED`
   si `starts_at > now()`, sinon `OPEN`. `current_price = start_price` (pas encore "current bid", c'est juste le prix
   plancher), `current_winner_id = null`.
2. **Bid** :
    - L'enchère doit être `OPEN`.
    - `now() < ends_at`.
    - `bidder_id != seller_id` (interdit de bid sur sa propre enchère).
    - `amount >= current_price + min_increment` si déjà au moins un bid, sinon `amount >= start_price`.
    - Le bidder doit avoir une `balance >= amount` **disponible** (c'est-à-dire balance brute moins ses holds actifs).
3. **Escrow** : à chaque bid accepté :
    - Si le bidder avait déjà un hold actif sur cette enchère, le libérer (`status = RELEASED`).
    - Créer un nouveau `solde_blocage` `(utilisateur_id, enchere_id, montant, statut=HELD)` pour le nouveau bid.
    - Si le précédent winner était quelqu'un d'autre, libérer son hold.
4. **Soft close anti-sniping** : si un bid est placé dans les **30 dernières secondes**, on prolonge `ends_at` de 30
   secondes.
5. **Annulation par seller** : possible **uniquement si aucun bid n'a été placé**. Si annulation, statut `CANCELLED`.
   Pas d'escrow à libérer (vu qu'il n'y a pas de bid).
6. **Clôture (par scheduler)** : à `ends_at`, le service de clôture :
    - Marque l'enchère `CLOSED`.
    - Si un winner existe :
        - Capture son hold (`status=CAPTURED`), `winner.balance -= amount`.
        - Crédite seller : `seller.balance += amount`.
        - Transfert artefact : `artefact.userId = winner.id`.
        - Crée 2 `TransactionEntity` (debit côté winner, credit côté seller, type "AUCTION_BUY" / "AUCTION_SELL").
        - Marque l'enchère `SETTLED`.
    - Si pas de winner : direct `SETTLED` (rien à faire). L'artefact reste au seller.
7. **Visibilité** : on expose le `username` du current_winner et des bidders dans les bids list. Pas anonyme.

### Hors scope explicite (NE PAS faire)

- Pas de proxy bidding / auto-bid.
- Pas de retrait de bid par le bidder.
- Pas de notifications externes (email, push).
- Pas de chat.
- Pas de RabbitMQ, pas de WebSocket.
- Pas de gestion multi-instance (mono-instance, events in-memory).
- Pas d'admin moderation (suspension d'enchère par admin).
- **Pas d'`Idempotency-Key` header** sur le POST /bids. Le client ne doit pas retry à l'aveugle ; on accepte cette
  limitation pour le MVP.
- **Pas d'auth via query param sur le SSE.** L'endpoint reste protégé `@PreAuthorize("isAuthenticated()")` standard. *
  *Côté front, l'intégration utilise `fetch()` + `ReadableStream` avec le header `Authorization` (PAS `EventSource`
  natif qui ne supporte pas les custom headers).** C'est explicitement documenté dans la JavaDoc de l'endpoint stream.

---

## 2. Architecture de la feature

```
[user] ──HTTP POST /api/auctions/{id}/bids──► [BidController]
                                                  │
                                                  ▼
                                          [BidService]
                                                  │
                                                  ├── @Transactional
                                                  │     ├── verrou pessimiste sur AuctionEntity
                                                  │     ├── validations métier
                                                  │     ├── INSERT bid
                                                  │     ├── INSERT/UPDATE solde_blocage
                                                  │     ├── UPDATE auction (current_price, current_winner)
                                                  │     ├── INSERT outbox_event (BidPlaced)
                                                  │     └── COMMIT
                                                  │
                                                  └── publie event in-memory ApplicationEventPublisher
                                                            │
                                                            ▼
                                                   [SseDispatcher @EventListener]
                                                            │
                                                            ▼
                                                   push à tous les SseEmitter
                                                   abonnés à auction {id}

[Scheduler @Scheduled] ──► AuctionCloseService ──► clôt les enchères dont ends_at < now()
                                                  (même flow events + SSE)
```

**Pourquoi outbox + ApplicationEventPublisher en parallèle ?**

- `ApplicationEventPublisher` = livraison rapide in-memory pour SSE (mono-instance).
- `outbox_event` = trace durable des events. Optionnel pour le MVP fonctionnel mais permet de brancher un broker plus
  tard sans toucher au code métier. **À implémenter dès maintenant** (table + INSERT dans la transaction), même si on ne
  consomme pas encore l'outbox. Coût : ~10 lignes.

---

## 3. Modèle de données

### Tables à créer (Liquibase)

#### `enchere`

| Colonne           | Type        | Contraintes                                                |
|-------------------|-------------|------------------------------------------------------------|
| id                | BIGINT      | PK, autoIncrement                                          |
| artefact_id       | BIGINT      | FK → artefact.id, NOT NULL                                 |
| vendeur_id        | BIGINT      | FK → utilisateur.id, NOT NULL                              |
| prix_depart       | INTEGER     | NOT NULL, > 0                                              |
| prix_actuel       | INTEGER     | NOT NULL                                                   |
| increment_min     | INTEGER     | NOT NULL, > 0, default 1                                   |
| gagnant_actuel_id | BIGINT      | FK → utilisateur.id, NULLABLE                              |
| debut_at          | TIMESTAMP   | NOT NULL                                                   |
| fin_at            | TIMESTAMP   | NOT NULL                                                   |
| statut            | VARCHAR(20) | NOT NULL — SCHEDULED / OPEN / CLOSED / SETTLED / CANCELLED |
| version           | BIGINT      | NOT NULL, default 0 (optimistic locking JPA `@Version`)    |
| created_at        | TIMESTAMP   | NOT NULL, default CURRENT_TIMESTAMP                        |

Index : `idx_enchere_statut_fin_at` sur `(statut, fin_at)` pour le scheduler.

**Index unique partiel anti-doublon** (empêche deux enchères actives sur le même artefact, même en cas de race condition
entre deux POST simultanés) :

```xml
<sql>
  CREATE UNIQUE INDEX uk_enchere_artefact_active
    ON enchere (artefact_id)
    WHERE statut IN ('SCHEDULED','OPEN');
</sql>
```

La validation applicative `existsActiveByArtefactId` fait office de message d'erreur propre, mais l'index est la
garantie ultime contre les races.

#### `enchere_offre` (bid)

| Colonne         | Type      | Contraintes                         |
|-----------------|-----------|-------------------------------------|
| id              | BIGINT    | PK, autoIncrement                   |
| enchere_id      | BIGINT    | FK → enchere.id, NOT NULL           |
| encherisseur_id | BIGINT    | FK → utilisateur.id, NOT NULL       |
| montant         | INTEGER   | NOT NULL                            |
| place_at        | TIMESTAMP | NOT NULL, default CURRENT_TIMESTAMP |

Index : `idx_offre_enchere_place_at` sur `(enchere_id, place_at DESC)`.

#### `solde_blocage` (escrow / hold de balance)

> **Nom français** pour rester cohérent avec le reste (`utilisateur`, `enchere`, `solde_couronnes`).

| Colonne        | Type        | Contraintes                           |
|----------------|-------------|---------------------------------------|
| id             | BIGINT      | PK, autoIncrement                     |
| utilisateur_id | BIGINT      | FK → utilisateur.id, NOT NULL         |
| enchere_id     | BIGINT      | FK → enchere.id, NOT NULL             |
| montant        | INTEGER     | NOT NULL                              |
| statut         | VARCHAR(20) | NOT NULL — HELD / RELEASED / CAPTURED |
| created_at     | TIMESTAMP   | NOT NULL, default CURRENT_TIMESTAMP   |

Index : `idx_solde_blocage_user_statut` sur `(utilisateur_id, statut)` pour calculer le held total.
Contrainte : un seul hold `HELD` par `(utilisateur_id, enchere_id)` — index unique partiel.

**Liquibase syntax** (Postgres-specific, à utiliser via `<sql>`) :

```xml
<sql>
  CREATE UNIQUE INDEX uk_solde_blocage_user_enchere_held
    ON solde_blocage (utilisateur_id, enchere_id)
    WHERE statut = 'HELD';
</sql>
```

Liquibase `<createIndex>` ne supporte pas la clause `WHERE` partielle → utiliser `<sql>` brut.

#### `outbox_event`

| Colonne        | Type        | Contraintes                         |
|----------------|-------------|-------------------------------------|
| id             | BIGINT      | PK, autoIncrement                   |
| aggregate_type | VARCHAR(50) | NOT NULL (ex: "AUCTION")            |
| aggregate_id   | BIGINT      | NOT NULL                            |
| event_type     | VARCHAR(50) | NOT NULL (ex: "BID_PLACED")         |
| payload        | TEXT        | NOT NULL (JSON sérialisé)           |
| created_at     | TIMESTAMP   | NOT NULL, default CURRENT_TIMESTAMP |
| published_at   | TIMESTAMP   | NULLABLE                            |

Pas d'index nécessaire pour le MVP (aucun consumer).

### Modifications à NE PAS faire

- Ne touche pas à `ArtifactEntity` (le transfert de propriété se fait juste en update du `userId` lors du settlement).
- Ne crée pas de table de "vente" ou "marketplace" séparée — l'enchère est l'unité.

---

## 4. Découpage en commits

Chaque commit doit :

- compiler (`./mvnw compile`)
- passer les tests existants (`./mvnw test`)
- avoir ses propres tests pour le code ajouté

### Commit 1 : Schéma Liquibase

**Fichier** : `src/main/resources/liquibase/changelog/changelog-add-auction-tables.xml`
**Action** : créer les 4 tables ci-dessus avec les FK et index. Ajouter l'include dans `db.changelog-master.xml`.
**Test** : démarrer l'app en local, vérifier que Liquibase applique sans erreur. Vérifier les 4 tables existent (`\dt`
en psql).

### Commit 2 : Domain models + Entities + Repositories

**Fichiers à créer** :

- `domain/AuctionStatus.java` (enum : SCHEDULED, OPEN, CLOSED, SETTLED, CANCELLED)
- `domain/HoldStatus.java` (enum : HELD, RELEASED, CAPTURED)
- `domain/model/Auction.java` (POJO Lombok)
- `domain/model/Bid.java` (POJO Lombok)
- `domain/model/WalletHold.java` (POJO Lombok)
- `infrastructure/out/persistance/entity/AuctionEntity.java` (table `enchere`, avec `@Version` sur version)
- `infrastructure/out/persistance/entity/BidEntity.java` (table `enchere_offre`)
- `infrastructure/out/persistance/entity/WalletHoldEntity.java` (table `solde_blocage` ; le nom de classe Java reste
  anglais : `WalletHoldEntity`)
- `infrastructure/out/persistance/entity/OutboxEventEntity.java` (table `outbox_event`)
- `infrastructure/out/persistance/repository/AuctionRepository.java`
- `infrastructure/out/persistance/repository/BidRepository.java`
- `infrastructure/out/persistance/repository/WalletHoldRepository.java`
- `infrastructure/out/persistance/repository/OutboxEventRepository.java`
- `infrastructure/out/persistance/mapper/AuctionPersistenceMapper.java`
- `infrastructure/out/persistance/mapper/BidPersistenceMapper.java`
- `infrastructure/out/persistance/mapper/WalletHoldPersistenceMapper.java`

**Détails entities :**

```java
// AuctionEntity — utiliser @Getter @Setter et PAS @Data (relations potentielles).
// Champs avec @Column(name="...") en français.
// @Enumerated(EnumType.STRING) pour AuctionStatus.
// @Version private Long version;
// PrePersist : si createdAt null → Instant.now()

// AuctionRepository : extends JpaRepository<AuctionEntity, Long>
//
// Méthodes nécessaires :
//   @Lock(LockModeType.PESSIMISTIC_WRITE)
//   @Query("SELECT a FROM AuctionEntity a WHERE a.id = :id")
//   Optional<AuctionEntity> findByIdForUpdate(@Param("id") Long id);
//
//   List<AuctionEntity> findByStatutAndFinAtBefore(String statut, Instant fin);
//
//   @Query("SELECT COUNT(a) > 0 FROM AuctionEntity a WHERE a.artefactId = :artefactId AND a.statut IN ('SCHEDULED','OPEN')")
//   boolean existsActiveByArtefactId(@Param("artefactId") Long artefactId);

// WalletHoldRepository (table = solde_blocage, mappée via @Table(name = "solde_blocage")) :
//   @Query("SELECT COALESCE(SUM(h.montant), 0) FROM WalletHoldEntity h WHERE h.utilisateurId = :userId AND h.statut = 'HELD'")
//   Integer sumHeldByUserId(@Param("userId") Long userId);
//
//   Optional<WalletHoldEntity> findByUtilisateurIdAndEnchereIdAndStatut(Long userId, Long auctionId, String statut);

// BidRepository :
//   List<BidEntity> findByEnchereIdOrderByPlaceAtDesc(Long auctionId);
```

**Tests à écrire** : aucun test pour cette étape (uniquement des structures de données). Le test viendra avec le
service.

### Commit 3 : Ports out + adapters

**Fichiers à créer** :

- `application/port/out/AuctionPersistencePort.java`
- `application/port/out/BidPersistencePort.java`
- `application/port/out/WalletHoldPersistencePort.java`
- `application/port/out/OutboxEventPort.java`
- Adapters correspondants dans `infrastructure/out/persistance/` (un fichier par port, classe `XxxPersistenceAdapter`
  avec `@Component`)

**Fichiers à MODIFIER** :

- `application/port/out/TransactionPersistencePort.java` — ajouter une méthode
  `Transaction save(Transaction transaction);`. Vérifier que l'adapter existant (chercher la classe dans
  `infrastructure/out/persistance/` qui implémente ce port) implémente cette nouvelle méthode. Le
  `TransactionRepository` JPA existe déjà avec `save(...)` hérité de `JpaRepository` — l'adapter doit juste le mapper. *
  *Lis `TransactionMapper.java` (persistance)** pour voir les conversions Domain↔Entity.

**Signatures des ports :**

```java
// AuctionPersistencePort
Auction save(Auction auction);

Optional<Auction> findById(Long id);

Optional<Auction> findByIdForUpdate(Long id);  // verrou pessimiste

List<Auction> findAll();

List<Auction> findAllByStatus(AuctionStatus status);

List<Auction> findOpenEndedBefore(Instant moment); // statut=OPEN AND fin_at < moment

boolean existsActiveByArtifactId(Long artifactId);

// BidPersistencePort
Bid save(Bid bid);

List<Bid> findByAuctionIdOrderByPlacedAtDesc(Long auctionId);

// WalletHoldPersistencePort
WalletHold save(WalletHold hold);

Optional<WalletHold> findActiveByUserAndAuction(Long userId, Long auctionId);

Integer sumHeldByUserId(Long userId);

List<WalletHold> findActiveByAuctionId(Long auctionId);

// OutboxEventPort
void append(String aggregateType, Long aggregateId, String eventType, String jsonPayload);
```

**Tests à écrire** : aucun (adapters fins, sans logique).

### Commit 4 : UseCases (ports in)

**Fichiers à créer** dans `application/port/in/` :

- `CreateAuctionUseCase.java` — `Auction createAuction(Long sellerId, CreateAuctionCommand cmd);`
- `CancelAuctionUseCase.java` — `void cancelAuction(Long sellerId, Long auctionId);`
- `PlaceBidUseCase.java` — `Bid placeBid(Long bidderId, Long auctionId, int amount);`
- `FetchAuctionsUseCase.java` — `List<Auction> fetchOpen();`, `Auction fetchById(Long id);`,
  `List<Bid> fetchBids(Long auctionId);`
- `CloseExpiredAuctionsUseCase.java` — `int closeExpired();` (retourne le nb d'enchères clôturées, utile pour les
  logs/tests)

**Records pour les commands :**

```java
public record CreateAuctionCommand(
    Long artifactId,
    int startPrice,
    int minIncrement,
    Instant startsAt,
    Instant endsAt
) {}
```

**Tests** : aucun (interfaces).

### Commit 5 : `AuctionService` (création + lecture + annulation)

**Fichier** : `application/service/AuctionService.java`
**Implémente** : `CreateAuctionUseCase`, `CancelAuctionUseCase`, `FetchAuctionsUseCase`.

**Logique critique :**

```java

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionService implements CreateAuctionUseCase, CancelAuctionUseCase, FetchAuctionsUseCase {

    private final AuctionPersistencePort auctionPort;
    private final BidPersistencePort bidPort;
    private final ArtifactPersistencePort artifactPort;
    private final OutboxEventPort outboxPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public Auction createAuction(Long sellerId, CreateAuctionCommand cmd) {
        // 1. Charger l'artefact, vérifier existence
        Artifact artifact = artifactPort.findById(cmd.artifactId())
                .orElseThrow(() -> new ResourceNotFoundException("Artifact", "id", cmd.artifactId()));
        // 2. Vérifier ownership
        if (!sellerId.equals(artifact.getUserId())) {
            throw new UnauthorizedAccessException("Vous n'êtes pas le propriétaire de cet artefact");
        }
        // 3. Vérifier qu'aucune enchère active n'existe déjà pour cet artefact
        if (auctionPort.existsActiveByArtifactId(cmd.artifactId())) {
            throw new ActionNotAllowedException("Cet artefact est déjà mis en vente");
        }
        // 4. Validations
        if (cmd.startPrice() <= 0) throw new InvalidParameterException("Le prix de départ doit être positif");
        if (cmd.minIncrement() <= 0) throw new InvalidParameterException("L'incrément minimum doit être positif");
        if (cmd.endsAt().isBefore(cmd.startsAt()))
            throw new InvalidParameterException("La fin doit être après le début");
        Instant now = Instant.now(clock); // Clock injecté
        if (cmd.endsAt().isBefore(now)) throw new InvalidParameterException("La fin doit être dans le futur");
        // 5. Statut initial
        AuctionStatus status = cmd.startsAt().isAfter(now) ? AuctionStatus.SCHEDULED:AuctionStatus.OPEN;
        // 6. Build + save
        Auction auction = Auction.builder()
                .artifactId(cmd.artifactId())
                .sellerId(sellerId)
                .startPrice(cmd.startPrice())
                .currentPrice(cmd.startPrice())
                .minIncrement(cmd.minIncrement())
                .startsAt(cmd.startsAt())
                .endsAt(cmd.endsAt())
                .status(status)
                .build();
        Auction saved = auctionPort.save(auction);
        // 7. Outbox event
        outboxPort.append("AUCTION", saved.getId(), "AUCTION_CREATED", toJson(saved));
        return saved;
    }

    @Override
    public void cancelAuction(Long sellerId, Long auctionId) {
        Auction auction = auctionPort.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));
        if (!sellerId.equals(auction.getSellerId())) {
            throw new UnauthorizedAccessException("Vous n'êtes pas le vendeur");
        }
        if (auction.getStatus()!=AuctionStatus.SCHEDULED && auction.getStatus()!=AuctionStatus.OPEN) {
            throw new ActionNotAllowedException("Cette enchère n'est plus active");
        }
        if (auction.getCurrentWinnerId()!=null) {
            throw new ActionNotAllowedException("Impossible d'annuler : des offres ont déjà été placées");
        }
        auction.setStatus(AuctionStatus.CANCELLED);
        Auction saved = auctionPort.save(auction);
        outboxPort.append("AUCTION", auctionId, "AUCTION_CANCELLED", toJson(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> fetchOpen() {
        return auctionPort.findAllByStatus(AuctionStatus.OPEN);
    }

    @Override
    @Transactional(readOnly = true)
    public Auction fetchById(Long id) {
        return auctionPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bid> fetchBids(Long auctionId) {
        if (auctionPort.findById(auctionId).isEmpty()) {
            throw new ResourceNotFoundException("Auction", "id", auctionId);
        }
        return bidPort.findByAuctionIdOrderByPlacedAtDesc(auctionId);
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize event", e);
        }
    }
}
```

**Tests** : `AuctionServiceTest` avec @Mock de tous les ports. Couvre :

- `createAuction_happyPath_returnsOpenAuction` (startsAt = now)
- `createAuction_scheduled_whenStartsInFuture`
- `createAuction_throws_whenArtifactNotFound`
- `createAuction_throws_whenNotOwner` (UnauthorizedAccessException)
- `createAuction_throws_whenArtifactAlreadyOnSale` (ActionNotAllowedException)
- `createAuction_throws_whenStartPriceZeroOrNegative`
- `createAuction_throws_whenEndsBeforeStarts`
- `createAuction_throws_whenEndsInPast`
- `cancelAuction_happyPath_setsStatusCancelled`
- `cancelAuction_throws_whenNotSeller`
- `cancelAuction_throws_whenAlreadyClosed`
- `cancelAuction_throws_whenHasBids` (currentWinnerId != null)
- `fetchById_throws_whenNotFound`
- `fetchBids_throws_whenAuctionNotFound`

### Commit 6 : `BidService` (placement de bid)

**Fichier** : `application/service/BidService.java`
**Implémente** : `PlaceBidUseCase`.

**Logique critique :**

```java
@Service
@RequiredArgsConstructor
@Transactional
public class BidService implements PlaceBidUseCase {

    private static final long SOFT_CLOSE_WINDOW_SECONDS = 30L;

    private final AuctionPersistencePort auctionPort;
    private final BidPersistencePort bidPort;
    private final WalletHoldPersistencePort holdPort;
    private final UserPersistencePort userPort;          // existant (retourne Optional<UserEntity>)
    private final OutboxEventPort outboxPort;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;                            // injecté pour les tests temporels (cf section 7)

    @Override
    public Bid placeBid(Long bidderId, Long auctionId, int amount) {
        // 1. Verrouiller l'enchère
        Auction auction = auctionPort.findByIdForUpdate(auctionId)
            .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));

        // 2. Validations
        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new ActionNotAllowedException("L'enchère n'est pas ouverte");
        }
        Instant now = Instant.now(clock);
        if (now.isAfter(auction.getEndsAt())) {
            throw new ActionNotAllowedException("L'enchère est terminée");
        }
        if (bidderId.equals(auction.getSellerId())) {
            throw new ActionNotAllowedException("Vous ne pouvez pas enchérir sur votre propre vente");
        }

        // 3. Montant minimal
        boolean hasBids = auction.getCurrentWinnerId() != null;
        int minimumAmount = hasBids
            ? auction.getCurrentPrice() + auction.getMinIncrement()
            : auction.getStartPrice();
        if (amount < minimumAmount) {
            throw new InvalidParameterException("Montant insuffisant. Minimum requis : " + minimumAmount);
        }

        // 4. Vérifier solde dispo (balance - holds actifs, en excluant l'éventuel hold du bidder sur cette même enchère car il sera remplacé)
        // ATTENTION : userPort.findById retourne Optional<UserEntity> dans le projet (cf UserPersistencePort.java).
        // On manipule donc directement UserEntity ; pas de conversion vers domain User pour cette opération.
        UserEntity bidder = userPort.findById(bidderId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", bidderId));
        Integer heldTotal = holdPort.sumHeldByUserId(bidderId);
        Optional<WalletHold> existingHold = holdPort.findActiveByUserAndAuction(bidderId, auctionId);
        int heldExcludingThisAuction = heldTotal - existingHold.map(WalletHold::getAmount).orElse(0);
        int available = bidder.getBalance() - heldExcludingThisAuction;
        if (available < amount) {
            throw new ActionNotAllowedException("Solde insuffisant");
        }

        // 5. Libérer le hold précédent du bidder s'il existe
        existingHold.ifPresent(h -> {
            h.setStatus(HoldStatus.RELEASED);
            holdPort.save(h);
        });

        // 6. Libérer le hold de l'ancien gagnant s'il existe et != bidder
        if (auction.getCurrentWinnerId() != null && !auction.getCurrentWinnerId().equals(bidderId)) {
            holdPort.findActiveByUserAndAuction(auction.getCurrentWinnerId(), auctionId)
                .ifPresent(h -> {
                    h.setStatus(HoldStatus.RELEASED);
                    holdPort.save(h);
                });
        }

        // 7. Créer le nouveau hold
        WalletHold newHold = WalletHold.builder()
            .userId(bidderId)
            .auctionId(auctionId)
            .amount(amount)
            .status(HoldStatus.HELD)
            .build();
        holdPort.save(newHold);

        // 8. Insérer le bid
        Bid bid = Bid.builder()
            .auctionId(auctionId)
            .bidderId(bidderId)
            .amount(amount)
            .placedAt(now)
            .build();
        Bid savedBid = bidPort.save(bid);

        // 9. Mettre à jour l'enchère
        auction.setCurrentPrice(amount);
        auction.setCurrentWinnerId(bidderId);

        // 10. Soft close anti-sniping
        long secondsUntilEnd = Duration.between(now, auction.getEndsAt()).getSeconds();
        boolean extended = false;
        if (secondsUntilEnd < SOFT_CLOSE_WINDOW_SECONDS) {
            auction.setEndsAt(now.plusSeconds(SOFT_CLOSE_WINDOW_SECONDS));
            extended = true;
        }
        Auction savedAuction = auctionPort.save(auction);

        // 11. Outbox events
        outboxPort.append("AUCTION", auctionId, "BID_PLACED", toJson(Map.of(
            "bidId", savedBid.getId(),
            "bidderId", bidderId,
            "amount", amount,
            "newCurrentPrice", savedAuction.getCurrentPrice(),
            "newEndsAt", savedAuction.getEndsAt().toString()
        )));
        if (extended) {
            outboxPort.append("AUCTION", auctionId, "AUCTION_EXTENDED", toJson(Map.of(
                "newEndsAt", savedAuction.getEndsAt().toString()
            )));
        }

        // 12. In-memory event pour SSE
        eventPublisher.publishEvent(new BidPlacedEvent(auctionId, savedBid, savedAuction, extended));

        return savedBid;
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (Exception e) { throw new IllegalStateException("Cannot serialize event", e); }
    }
}
```

**Note** : `UserPersistencePort.findById` retourne `Optional<UserEntity>` (vérifié dans le code existant). On manipule
donc directement `UserEntity` pour les opérations sur balance. Pas besoin d'ajouter de nouvelle méthode au port.

**Event class** :

```java
// application/service/event/BidPlacedEvent.java
public record BidPlacedEvent(Long auctionId, Bid bid, Auction auction, boolean extended) {
}

public record AuctionClosedEvent(Long auctionId, Auction auction, Long winnerId, Integer winningAmount) {
}

public record AuctionCancelledEvent(Long auctionId) {
}
```

**Tests `BidServiceTest`** (Mockito) — couvre TOUTES ces branches :

- `placeBid_happyPath_firstBid` (pas de winner précédent)
- `placeBid_happyPath_secondBid_releasesPreviousWinnerHold`
- `placeBid_sameUserRebids_releasesOwnPreviousHold` (bidder relance sur lui-même)
- `placeBid_extendsEndsAt_whenWithin30sOfEnd`
- `placeBid_doesNotExtend_whenOutsideSoftCloseWindow`
- `placeBid_throws_whenAuctionNotFound`
- `placeBid_throws_whenAuctionNotOpen` (status SCHEDULED, CLOSED, CANCELLED)
- `placeBid_throws_whenAuctionEnded` (now > endsAt mais statut encore OPEN)
- `placeBid_throws_whenBidderIsSeller`
- `placeBid_throws_whenAmountBelowStartPrice` (premier bid)
- `placeBid_throws_whenAmountBelowCurrentPlusIncrement` (bids suivants)
- `placeBid_throws_whenInsufficientBalance`
- `placeBid_throws_whenInsufficientAvailable_dueToHoldsOnOtherAuctions`
- `placeBid_publishesBidPlacedEvent`
- `placeBid_publishesAuctionExtendedEvent_whenSoftClosed`
- `placeBid_appendsOutboxEvents`

### Commit 7 : `AuctionCloseService` (clôture + scheduler)

**Fichier** : `application/service/AuctionCloseService.java`
**Implémente** : `CloseExpiredAuctionsUseCase`.

```java

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionCloseService implements CloseExpiredAuctionsUseCase {

    private final AuctionPersistencePort auctionPort;
    private final WalletHoldPersistencePort holdPort;
    private final UserPersistencePort userPort;
    private final ArtifactPersistencePort artifactPort;
    private final TransactionPersistencePort transactionPort;  // étendu avec save() au commit 3
    private final ArtifactRepository artifactRepository;       // entorse au pattern, voir étape 5 ci-dessous
    private final OutboxEventPort outboxPort;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    public int closeExpired() {
        Instant now = Instant.now(clock);
        List<Auction> toClose = auctionPort.findOpenEndedBefore(now);
        int closed = 0;
        for (Auction a : toClose) {
            try {
                settleSingle(a.getId());
                closed++;
            } catch (Exception e) {
                log.error("Failed to close auction {}", a.getId(), e);
            }
        }
        return closed;
    }

    @Transactional
    public void settleSingle(Long auctionId) {
        Auction auction = auctionPort.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));
        // Idempotence : si déjà clos par un autre thread/instance, on sort sans erreur
        if (auction.getStatus()!=AuctionStatus.OPEN) return;
        if (Instant.now().isBefore(auction.getEndsAt())) return; // safety net

        auction.setStatus(AuctionStatus.CLOSED);

        Long winnerId = auction.getCurrentWinnerId();
        Integer winningAmount = auction.getCurrentPrice();

        if (winnerId!=null) {
            // 1. Capture hold winner
            WalletHold winnerHold = holdPort.findActiveByUserAndAuction(winnerId, auctionId)
                    .orElseThrow(() -> new IllegalStateException("Hold actif manquant pour winner " + winnerId));
            winnerHold.setStatus(HoldStatus.CAPTURED);
            holdPort.save(winnerHold);

            // 2. Débiter winner — UserEntity direct (cf section 0)
            UserEntity winner = userPort.findById(winnerId).orElseThrow();
            winner.setBalance(winner.getBalance() - winningAmount);
            userPort.save(winner);

            // 3. Créditer seller
            UserEntity seller = userPort.findById(auction.getSellerId()).orElseThrow();
            seller.setBalance(seller.getBalance() + winningAmount);
            userPort.save(seller);

            // 4. Transfert artefact (port retourne Artifact domain)
            Artifact artifact = artifactPort.findById(auction.getArtifactId()).orElseThrow();
            artifact.setUserId(winnerId);
            artifactPort.save(artifact);

            // 5. Transactions historiques.
            // ATTENTION : Transaction (domain) référence directement UserEntity et ArtifactEntity
            // (cf domain/model/Transaction.java) — c'est l'existant. On s'aligne dessus.
            // Pour récupérer l'ArtifactEntity (nécessaire au champ Transaction.artefact), on injecte
            // directement ArtifactRepository (JPA) dans ce service en plus du port. C'est une entorse
            // au pattern hexagonal isolée à cette opération de settlement.
            ArtifactEntity artifactEntity = artifactRepository.findById(auction.getArtifactId()).orElseThrow();
            Timestamp nowTs = Timestamp.from(Instant.now(clock));

            Transaction debitTx = Transaction.builder()
                    .utilisateur(winner)
                    .artefact(artifactEntity)
                    .montant(BigDecimal.valueOf(winningAmount).negate())
                    .typeTransaction("AUCTION_BUY")
                    .date(nowTs)
                    .build();
            transactionPort.save(debitTx);

            Transaction creditTx = Transaction.builder()
                    .utilisateur(seller)
                    .artefact(artifactEntity)
                    .montant(BigDecimal.valueOf(winningAmount))
                    .typeTransaction("AUCTION_SELL")
                    .date(nowTs)
                    .build();
            transactionPort.save(creditTx);

            auction.setStatus(AuctionStatus.SETTLED);
        } else {
            // Pas de winner : direct SETTLED, rien à transférer
            auction.setStatus(AuctionStatus.SETTLED);
        }

        Auction savedAuction = auctionPort.save(auction);

        outboxPort.append("AUCTION", auctionId, "AUCTION_CLOSED", toJson(Map.of(
                "winnerId", String.valueOf(winnerId),
                "winningAmount", winningAmount
        )));
        eventPublisher.publishEvent(new AuctionClosedEvent(auctionId, savedAuction, winnerId, winningAmount));
    }

    private String toJson(Object o) { /* idem */ }
}
```

**Note importante** : `TransactionPersistencePort` est étendu au commit 3 avec `Transaction save(Transaction);`.
L'adapter doit utiliser le `TransactionMapper` existant pour convertir Domain↔Entity. Vu que `Transaction` (domain)
contient directement `UserEntity` et `ArtifactEntity`, le mapping est presque pass-through.

**Scheduler** :

```java
// infrastructure/scheduler/AuctionCloseScheduler.java
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionCloseScheduler {
    private final CloseExpiredAuctionsUseCase closeUseCase;

    @Scheduled(fixedDelayString = "PT2S")  // toutes les 2s
    public void closeExpiredAuctions() {
        try {
            int n = closeUseCase.closeExpired();
            if (n > 0) log.info("Closed {} expired auction(s)", n);
        } catch (Exception e) {
            log.error("Auction close scheduler error", e);
        }
    }
}
```

**Activer @Scheduled** : ajouter `@EnableScheduling` directement sur la classe `LootopiaAppApplication` (la classe avec
`@SpringBootApplication`). Pas de configuration dédiée, c'est l'option la plus standard.

**Tests `AuctionCloseServiceTest`** :

- `closeExpired_settlesAuctionWithWinner_transfersBalanceAndArtifact`
- `closeExpired_settlesAuctionWithoutWinner_noTransfer`
- `closeExpired_idempotent_skipsAlreadyClosed`
- `closeExpired_skipsIfEndsAtStillFuture` (safety net)
- `closeExpired_continuesOnFailure` (1 sur 3 plante, les 2 autres se font)
- `closeExpired_publishesAuctionClosedEvent`

### Commit 8 : Infrastructure SSE

**Fichiers à créer** :

- `infrastructure/in/rest/sse/AuctionSseRegistry.java` — `Map<Long, Set<SseEmitter>>` thread-safe (`ConcurrentHashMap` +
  `CopyOnWriteArraySet`).
- `infrastructure/in/rest/sse/AuctionSseDispatcher.java` — `@Component` avec `@EventListener` pour `BidPlacedEvent`,
  `AuctionClosedEvent`, `AuctionCancelledEvent`. Sérialise en JSON (Jackson) et `emit(...)` à tous les emitters du
  registry pour cet auctionId.

```java
// AuctionSseRegistry
@Component
public class AuctionSseRegistry {
    private final Map<Long, Set<SseEmitter>> emittersByAuction = new ConcurrentHashMap<>();

    public SseEmitter register(Long auctionId) {
        SseEmitter emitter = new SseEmitter(0L); // pas de timeout (ou 30 min)
        emittersByAuction.computeIfAbsent(auctionId, k -> ConcurrentHashMap.newKeySet()).add(emitter);
        emitter.onCompletion(() -> remove(auctionId, emitter));
        emitter.onTimeout(() -> remove(auctionId, emitter));
        emitter.onError(t -> remove(auctionId, emitter));
        return emitter;
    }

    public void broadcast(Long auctionId, String eventName, Object payload) {
        Set<SseEmitter> set = emittersByAuction.get(auctionId);
        if (set == null) return;
        for (SseEmitter e : set) {
            try {
                e.send(SseEmitter.event().name(eventName).data(payload));
            } catch (Exception ex) {
                remove(auctionId, e);
            }
        }
    }

    private void remove(Long auctionId, SseEmitter e) {
        Set<SseEmitter> set = emittersByAuction.get(auctionId);
        if (set != null) set.remove(e);
    }
}
```

**Tests** :

- `AuctionSseRegistryTest` — register / broadcast / unregister on completion. Test simple, instancier directement.
- `AuctionSseDispatcherTest` — vérifie qu'un `BidPlacedEvent` déclenche un `broadcast(...)` avec le bon eventName et
  payload.

### Commit 9 : Controllers + DTOs + Mappers REST

**Fichiers à créer dans `infrastructure/in/rest/`** :

- `AuctionController.java` — endpoints user-facing.
- `dto/CreateAuctionRequest.java`
- `dto/PlaceBidRequest.java`
- `dto/AuctionResponse.java` (avec username vendeur, gagnant)
- `dto/BidResponse.java` (avec username bidder)
- `mapper/AuctionRestMapper.java`

**Endpoints** (méthode, path, body, response, security) :

| Méthode | Path                        | Body                   | Response                | Auth          | Description                                  |
|---------|-----------------------------|------------------------|-------------------------|---------------|----------------------------------------------|
| POST    | `/api/auctions`             | `CreateAuctionRequest` | `AuctionResponse` 201   | authenticated | Crée une enchère (seller = user authentifié) |
| GET     | `/api/auctions`             | -                      | `List<AuctionResponse>` | authenticated | Enchères ouvertes                            |
| GET     | `/api/auctions/{id}`        | -                      | `AuctionResponse`       | authenticated | Détail                                       |
| GET     | `/api/auctions/{id}/bids`   | -                      | `List<BidResponse>`     | authenticated | Historique des bids                          |
| POST    | `/api/auctions/{id}/bids`   | `PlaceBidRequest`      | `BidResponse` 201       | authenticated | Place un bid                                 |
| DELETE  | `/api/auctions/{id}`        | -                      | 204                     | authenticated | Annule (seller only, pas de bid)             |
| GET     | `/api/auctions/{id}/stream` | -                      | `text/event-stream`     | authenticated | Flux SSE                                     |

**Squelette controller :**

```java

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuctionController {

    private final CreateAuctionUseCase createUC;
    private final CancelAuctionUseCase cancelUC;
    private final PlaceBidUseCase placeBidUC;
    private final FetchAuctionsUseCase fetchUC;
    private final AuctionSseRegistry sseRegistry;
    private final AuctionRestMapper mapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionResponse create(@AuthenticationPrincipal Jwt jwt,
                                  @RequestBody @Valid CreateAuctionRequest req) {
        Long sellerId = Long.valueOf(jwt.getSubject());
        Auction created = createUC.createAuction(sellerId, mapper.toCommand(req));
        return mapper.toResponse(created);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public List<AuctionResponse> listOpen() {
        return fetchUC.fetchOpen().stream().map(mapper::toResponse).toList();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public AuctionResponse getOne(@PathVariable Long id) {
        return mapper.toResponse(fetchUC.fetchById(id));
    }

    @GetMapping(value = "/{id}/bids", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public List<BidResponse> listBids(@PathVariable Long id) {
        return fetchUC.fetchBids(id).stream().map(mapper::toBidResponse).toList();
    }

    @PostMapping(value = "/{id}/bids", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public BidResponse placeBid(@AuthenticationPrincipal Jwt jwt,
                                @PathVariable Long id,
                                @RequestBody @Valid PlaceBidRequest req) {
        Long bidderId = Long.valueOf(jwt.getSubject());
        Bid bid = placeBidUC.placeBid(bidderId, id, req.getAmount());
        return mapper.toBidResponse(bid);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long sellerId = Long.valueOf(jwt.getSubject());
        cancelUC.cancelAuction(sellerId, id);
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter stream(@PathVariable Long id) {
        // 404 si l'enchère n'existe pas
        fetchUC.fetchById(id);
        return sseRegistry.register(id);
    }
}
```

**DTOs (jakarta validation) :**

```java
// CreateAuctionRequest
@Data public class CreateAuctionRequest {
    @NotNull private Long artifactId;
    @NotNull @Positive private Integer startPrice;
    @NotNull @Positive private Integer minIncrement;
    @NotNull private Instant startsAt;
    @NotNull @Future private Instant endsAt;
}

// PlaceBidRequest
@Data public class PlaceBidRequest {
    @NotNull @Positive private Integer amount;
}

// AuctionResponse
@Data @Builder public class AuctionResponse {
    private Long id;
    private Long artifactId;
    private Long sellerId;
    private String sellerUsername;       // résolu via UserPort dans le mapper
    private Integer startPrice;
    private Integer currentPrice;
    private Integer minIncrement;
    private Long currentWinnerId;        // nullable
    private String currentWinnerUsername;// nullable
    private Instant startsAt;
    private Instant endsAt;
    private String status;
}

// BidResponse
@Data @Builder public class BidResponse {
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private String bidderUsername;
    private Integer amount;
    private Instant placedAt;
}
```

**Tests** :

- `AuctionControllerTest` (MockMvc, standaloneSetup) :
    - `create_returns201` — mock le UseCase, vérifie payload + status
    - `create_returns400_whenInvalidBody` (champ null)
    - `listOpen_returnsList`
    - `getOne_returnsDetail`
    - `getOne_returns404_whenNotFound` (UseCase throw → ExceptionTranslator)
    - `placeBid_returns201`
    - `placeBid_returns400_whenInvalidAmount`
    - `cancel_returns204`
    - `stream_returnsSseContentType`

### Commit 10 : Branchement SSE sur les events

**Fichier** : `infrastructure/in/rest/sse/AuctionSseDispatcher.java`

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionSseDispatcher {

    private final AuctionSseRegistry registry;
    private final AuctionRestMapper mapper;

    @EventListener
    public void onBidPlaced(BidPlacedEvent ev) {
        registry.broadcast(ev.auctionId(), "bid_placed", Map.of(
            "bid", mapper.toBidResponse(ev.bid()),
            "auction", mapper.toResponse(ev.auction()),
            "extended", ev.extended()
        ));
    }

    @EventListener
    public void onAuctionClosed(AuctionClosedEvent ev) {
        registry.broadcast(ev.auctionId(), "auction_closed", Map.of(
            "auction", mapper.toResponse(ev.auction()),
            "winnerId", ev.winnerId() != null ? ev.winnerId() : "null",
            "winningAmount", ev.winningAmount() != null ? ev.winningAmount() : 0
        ));
    }

    @EventListener
    public void onAuctionCancelled(AuctionCancelledEvent ev) {
        registry.broadcast(ev.auctionId(), "auction_cancelled", Map.of("auctionId", ev.auctionId()));
    }
}
```

**Test** : déjà couvert par `AuctionSseDispatcherTest` du commit 8 (à compléter avec ces listeners si pas fait).

### Commit 11 : Endpoint public SSE et CORS

- Vérifier que la config `WebConfig` autorise CORS pour le SSE depuis le front.
- Vérifier que `/api/auctions/**` n'est pas dans les `PUBLIC_PATHS` de `SecurityConfig` (doit être authentifié).
- Si le front utilise `EventSource`, attention : `EventSource` n'envoie pas le header `Authorization`. Documenter dans
  le code et OUVRIR un ticket TODO si c'est un problème (on peut soit accepter le token en query param `?token=...` côté
  SSE, soit utiliser `fetch` + `ReadableStream` côté front). **Pour le MVP** : exposer l'endpoint en authenticated, le
  front gérera. Ne pas implémenter d'auth via query param sans validation produit.

---

## 5. Récapitulatif des fichiers créés/modifiés

**Créés (~ 38 fichiers) :**

- 1 changelog Liquibase
- 3 enums domain
- 3 domain models
- 4 entities JPA
- 4 repositories JPA
- 3 mappers persistance
- 4 ports out
- 4 adapters out
- 5 ports in (UseCase)
- 1 record CreateAuctionCommand
- 3 events (records)
- 3 services (Auction, Bid, AuctionClose)
- 1 scheduler
- 2 SSE (registry + dispatcher)
- 1 controller
- 4 DTOs REST
- 1 mapper REST
- ~10 fichiers de test

**Modifiés :**

- `db.changelog-master.xml` (1 ligne ajoutée)
- `LootopiaAppApplication.java` ou config dédiée (`@EnableScheduling`)

---

## 6. Critères d'acceptation finaux (à vérifier avant de déclarer terminé)

- [ ] `./mvnw clean test` passe sans erreur (tous les tests, anciens et nouveaux).
- [ ] `./mvnw spring-boot:run` démarre sans erreur, Liquibase applique le nouveau changelog.
- [ ] Création d'une enchère via `POST /api/auctions` retourne 201 et un objet cohérent.
- [ ] Bid via `POST /api/auctions/{id}/bids` :
    - rejette si user pas authentifié (401)
    - rejette montant trop bas (400)
    - rejette si bidder = seller (400)
    - rejette si solde insuffisant (400)
    - accepté avec montant correct (201)
- [ ] Le hold du bidder précédent est bien `RELEASED` quand un autre user surenchérit (vérifier en DB).
- [ ] À la fin de l'enchère (passer manuellement `ends_at` dans le passé en DB), le scheduler clôt :
    - statut → SETTLED
    - balance winner décrémentée
    - balance seller incrémentée
    - artefact.userId = winner.id
    - 2 transactions créées
- [ ] Connexion SSE sur `/api/auctions/{id}/stream` ne se ferme pas immédiatement, et reçoit un event `bid_placed` quand
  un bid est placé sur l'enchère.
- [ ] Aucune table `outbox_event` ne se remplit anarchiquement (1 ligne par event business, pas de doublons).
- [ ] Couverture de test : tous les tests listés dans les commits 5, 6, 7, 8, 9 sont écrits et passent.

---

## 7. Points d'attention pour l'agent d'implémentation

- **Lis l'existant avant d'écrire.** Notamment : `UserPersistencePort`, `UserService`, `TransactionPersistencePort`,
  `ArtifactPersistencePort`. Si une signature de méthode n'existe pas, tu peux soit l'ajouter (si c'est trivial) soit
  adapter ta propre approche. **Ne jamais casser un port existant.**
- **Pas de `@Data` sur les entities JPA avec `@Version`** : utilise
  `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` à la place.
- **Toujours `@Transactional`** sur les méthodes métier qui écrivent. `@Transactional(readOnly = true)` sur les
  lectures.
- **Verrou pessimiste** : seulement sur `findByIdForUpdate` dans `BidService` et `AuctionCloseService`. Pas ailleurs.
- **L'`outbox_event`** ne doit JAMAIS être inséré hors d'une transaction qui modifie l'agrégat associé. Si tu fais un
  INSERT outbox sans modification métier, tu corromps la sémantique.
- **Tests** : un mock par port. Ne mock pas les services entre eux (un service par test). Mock minimal — utilise des
  assertions de comportement (`verify`) seulement quand le comportement est l'objet du test, sinon utilise des
  assertions sur les retours.
- **`Clock` injectable obligatoire** dans `BidService` et `AuctionCloseService` (pas dans `AuctionService` puisqu'il ne
  fait pas de logique temporelle critique au-delà du `endsAt > now`, mais l'injecter aussi par cohérence est OK).
  Déclarer un bean global :

```java
// config/ClockConfig.java
@Configuration
public class ClockConfig {
    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
```

Tests : injecte `Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneOffset.UTC)`. Plus jamais d'appel à
`Instant.now()` sans `clock` dans les services.

- **Pas de RabbitMQ, pas de WebSocket, pas de WebFlux.** Si tu es tenté d'en ajouter, **arrête-toi et demande**.
- **Convention DB déjà tranchée** : tables = `enchere`, `enchere_offre`, `solde_blocage`, `outbox_event`. Colonnes en
  français comme l'existant. Les noms de classes Java restent en anglais (`AuctionEntity`, `BidEntity`,
  `WalletHoldEntity`, `OutboxEventEntity`) avec `@Table(name = "...")`.

---

## 8. Si quelque chose te semble flou

Stop l'implémentation et pose la question. Mieux vaut perdre 5 min à demander que de dérailler le plan.
