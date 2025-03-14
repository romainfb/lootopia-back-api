package com.lootopia.lootopia_app.infrastructure.out.persistance;
import com.lootopia.lootopia_app.domain.model.Hunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HuntRepository extends JpaRepository<Hunt, Long> {
}
