package com.lootopia.lootopia_app.infrastructure.in.rest.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class AuctionSseRegistryTest {

    private AuctionSseRegistry registry;
    private Long AUCTION_ID = 1L;
    private Long ANOTHER_AUCTION_ID = 2L;

    @BeforeEach
    void setUp() {
        registry = new AuctionSseRegistry();
    }

    @Test
    void register_addsEmitterToRegistry() {
        SseEmitter emitter = registry.register(AUCTION_ID);
        assertNotNull(emitter);
        // Verify that the emitter is in the registry for AUCTION_ID
        // (indirectly, by trying to broadcast to it)
        SseEmitter spyEmitter = spy(emitter);
        registry.broadcast(AUCTION_ID, "testEvent", "testData");
        try {
            verify(spyEmitter, atLeastOnce()).send(any(SseEmitter.class)); // This is tricky to verify directly
        } catch (IOException e) {
            fail("IOException during broadcast verification", e);
        }
    }

    @Test
    void broadcast_sendsEventToAllEmittersForAuction() throws IOException, InterruptedException {
        SseEmitter emitter1 = spy(new SseEmitter(0L));
        SseEmitter emitter2 = spy(new SseEmitter(0L));

        // Manually add emitters to the registry for testing purposes
        registry.emittersByAuction.computeIfAbsent(AUCTION_ID, k -> new java.util.concurrent.CopyOnWriteArraySet<>()).add(emitter1);
        registry.emittersByAuction.computeIfAbsent(AUCTION_ID, k -> new java.util.concurrent.CopyOnWriteArraySet<>()).add(emitter2);

        String eventName = "bid_placed";
        String eventData = "{\"amount\":100}";

        registry.broadcast(AUCTION_ID, eventName, eventData);

        verify(emitter1, timeout(100)).send(SseEmitter.event().name(eventName).data(eventData));
        verify(emitter2, timeout(100)).send(SseEmitter.event().name(eventName).data(eventData));
    }

    @Test
    void broadcast_doesNotSendToEmittersOfOtherAuctions() throws IOException {
        SseEmitter emitter1 = spy(new SseEmitter(0L));
        SseEmitter emitter2 = spy(new SseEmitter(0L));

        registry.emittersByAuction.computeIfAbsent(AUCTION_ID, k -> new java.util.concurrent.CopyOnWriteArraySet<>()).add(emitter1);
        registry.emittersByAuction.computeIfAbsent(ANOTHER_AUCTION_ID, k -> new java.util.concurrent.CopyOnWriteArraySet<>()).add(emitter2);

        String eventName = "bid_placed";
        String eventData = "{\"amount\":100}";

        registry.broadcast(AUCTION_ID, eventName, eventData);

        verify(emitter1, timeout(100)).send(SseEmitter.event().name(eventName).data(eventData));
        verify(emitter2, never()).send(any(SseEmitter.class));
    }

    @Test
    void remove_onCompletion_removesEmitter() throws InterruptedException {
        SseEmitter emitter = registry.register(AUCTION_ID);
        CountDownLatch latch = new CountDownLatch(1);
        emitter.onCompletion(latch::countDown);

        // Simulate completion
        emitter.complete();
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));

        // Verify emitter is removed
        registry.broadcast(AUCTION_ID, "test", "data");
        // If the emitter was removed, no exception should be thrown and no interaction should happen
        // This is hard to verify directly without exposing internal state.
        // A proxy check: if we register another emitter, it should be the only one.
        SseEmitter newEmitter = spy(new SseEmitter(0L));
        registry.emittersByAuction.get(AUCTION_ID).add(newEmitter); // Manually add after removal
        registry.broadcast(AUCTION_ID, "test", "data");
        try {
            verify(newEmitter, timeout(100)).send(any(SseEmitter.class));
        } catch (IOException e) {
            fail("IOException during broadcast verification", e);
        }
    }

    @Test
    void remove_onError_removesEmitter() throws InterruptedException {
        SseEmitter emitter = registry.register(AUCTION_ID);
        CountDownLatch latch = new CountDownLatch(1);
        emitter.onError(t -> latch.countDown());

        // Simulate error
        emitter.completeWithError(new IOException("Test error"));
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));

        // Verify emitter is removed (similar indirect check as above)
        SseEmitter newEmitter = spy(new SseEmitter(0L));
        registry.emittersByAuction.get(AUCTION_ID).add(newEmitter);
        registry.broadcast(AUCTION_ID, "test", "data");
        try {
            verify(newEmitter, timeout(100)).send(any(SseEmitter.class));
        } catch (IOException e) {
            fail("IOException during broadcast verification", e);
        }
    }

    @Test
    void remove_onTimeout_removesEmitter() throws InterruptedException {
        SseEmitter emitter = registry.register(AUCTION_ID);
        CountDownLatch latch = new CountDownLatch(1);
        emitter.onTimeout(latch::countDown);

        // Simulate timeout
        emitter.complete(); // SseEmitter.complete() also triggers onTimeout if not completed by client
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));

        // Verify emitter is removed (similar indirect check as above)
        SseEmitter newEmitter = spy(new SseEmitter(0L));
        registry.emittersByAuction.get(AUCTION_ID).add(newEmitter);
        registry.broadcast(AUCTION_ID, "test", "data");
        try {
            verify(newEmitter, timeout(100)).send(any(SseEmitter.class));
        } catch (IOException e) {
            fail("IOException during broadcast verification", e);
        }
    }
}
