package com.monolito.ecommerce.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class NotificationStore {

    private static final int MAX_PER_USER = 30;

    private final ConcurrentHashMap<Long, List<NotificationEvent>> store = new ConcurrentHashMap<>();

    public void agregar(NotificationEvent event) {
        store.compute(event.userId(), (userId, lista) -> {
            List<NotificationEvent> actualizada = lista == null
                    ? new ArrayList<>()
                    : new ArrayList<>(lista);
            actualizada.add(0, event);
            if (actualizada.size() > MAX_PER_USER) {
                actualizada.subList(MAX_PER_USER, actualizada.size()).clear();
            }
            return actualizada;
        });
    }

    public List<NotificationEvent> obtener(Long userId) {
        return Collections.unmodifiableList(
                store.getOrDefault(userId, Collections.emptyList()));
    }
}
