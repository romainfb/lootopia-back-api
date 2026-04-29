package com.lootopia.lootopia_app.infrastructure.in.rest.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class AuctionSseRegistry {
    private final Map<Long, Set<SseEmitter>> emittersByAuction = new ConcurrentHashMap<>();

    public SseEmitter register(Long auctionId) {
        SseEmitter emitter = new SseEmitter(0L); // 0L means no timeout
        this.emittersByAuction.computeIfAbsent(auctionId, k -> new CopyOnWriteArraySet<>()).add(emitter);

        emitter.onCompletion(() -> remove(auctionId, emitter));
        emitter.onTimeout(() -> remove(auctionId, emitter));
        emitter.onError(t -> {
            // Log the error if needed
            remove(auctionId, emitter);
        });
        return emitter;
    }

    public void broadcast(Long auctionId, String eventName, Object payload) {
        Set<SseEmitter> emitters = emittersByAuction.get(auctionId);
        if (emitters==null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (Exception ex) {
                // Log the exception if needed
                emitter.completeWithError(ex); // Complete the emitter on error
                remove(auctionId, emitter);
            }
        }
    }

    private void remove(Long auctionId, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersByAuction.get(auctionId);
        if (emitters!=null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emittersByAuction.remove(auctionId);
            }
        }
    }
}
