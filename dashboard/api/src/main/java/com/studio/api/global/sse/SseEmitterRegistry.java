package com.studio.api.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SseEmitterRegistry {

    private static final long TIMEOUT = Duration.ofMinutes(30).toMillis();
    private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

    private final PresenceRecorder presenceRecorder;

    public SseEmitter add(Long memberNo) {
        String id = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onCompletion(() -> remove(memberNo, id));
        emitter.onTimeout(() -> remove(memberNo, id));
        emitter.onError(e -> remove(memberNo, id));

        emitters.computeIfAbsent(memberNo, key -> new ConcurrentHashMap<>())
                .put(id, emitter);

        presenceRecorder.touch(memberNo);

        try {
            emitter.send(SseEmitter.event().name("connected").data(id));
        } catch (IOException e) {
            remove(memberNo, id);
        }
        return emitter;
    }

    private void remove(Long memberNo, String id) {
        boolean[] wentOffline = {false};

        emitters.computeIfPresent(memberNo, (key, connections) -> {
            connections.remove(id);

            if (connections.isEmpty()) {
                wentOffline[0] = true;
                return null;
            }
            return connections;
        });

        if (wentOffline[0]) presenceRecorder.touch(memberNo);
    }

    public boolean isOnline(Long memberNo) {
        return emitters.containsKey(memberNo);
    }

    @Scheduled(fixedRate = 20_000)
    public void ping() {
        emitters.forEach((memberNo, map) ->
                map.forEach((id, emitter) -> {
                    try {
                        emitter.send(SseEmitter.event().comment("ping"));
                    } catch (Exception e) {
                        remove(memberNo, id);
                    }
                }));
    }

    public void send(Long memberNo, String eventName, Object data) {
        Map<String, SseEmitter> connections = emitters.get(memberNo);
        if (connections == null) return;

        connections.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                remove(memberNo, id);
            }
        });
    }
}
