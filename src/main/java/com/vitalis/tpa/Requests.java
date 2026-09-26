package com.vitalis.tpa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pending teleport requests. Pure logic, unit-tested.
 * target -> sender -> request (one active request per sender).
 */
public class Requests {
    public enum Kind { TO, HERE }

    public static class Req {
        public final UUID from;
        public final String fromName;
        public final Kind kind;
        public final long expiresAt;

        Req(UUID from, String fromName, Kind kind, long expiresAt) {
            this.from = from;
            this.fromName = fromName;
            this.kind = kind;
            this.expiresAt = expiresAt;
        }

        public boolean expired(long now) {
            return now >= expiresAt;
        }
    }

    private final Map<UUID, Map<UUID, Req>> pending = new LinkedHashMap<>();

    /** Add request (replaces same sender's old one). Returns open requests count for target. */
    public synchronized int add(UUID target, UUID from, String fromName, Kind kind, long now, long expireMs) {
        Map<UUID, Req> m = pending.computeIfAbsent(target, k -> new LinkedHashMap<>());
        m.put(from, new Req(from, fromName, kind, now + expireMs));
        prune(target, now);
        return m.size();
    }

    /** Take (and remove) a request: by sender name, or the latest one. Null if none/expired. */
    public synchronized Req take(UUID target, String senderName, long now) {
        Map<UUID, Req> m = pending.get(target);
        if (m == null || m.isEmpty()) return null;
        prune(target, now);
        if (m.isEmpty()) return null;
        if (senderName != null && !senderName.isEmpty()) {
            for (Req r : m.values()) {
                if (r.fromName.equalsIgnoreCase(senderName)) {
                    m.remove(r.from);
                    if (m.isEmpty()) pending.remove(target);
                    return r.expired(now) ? null : r;
                }
            }
            return null;
        }
        // latest = last inserted
        List<Req> all = new ArrayList<>(m.values());
        Req last = all.get(all.size() - 1);
        m.remove(last.from);
        if (m.isEmpty()) pending.remove(target);
        return last.expired(now) ? null : last;
    }

    /** Remove request without taking (deny/cancel/logout). */
    public synchronized void remove(UUID target, UUID from) {
        Map<UUID, Req> m = pending.get(target);
        if (m == null) return;
        m.remove(from);
        if (m.isEmpty()) pending.remove(target);
    }

    /** All expired requests (for timeout messages). Returns "target:request" pairs. */
    public synchronized List<UUID[]> expire(long now) {
        List<UUID[]> out = new ArrayList<>();
        for (Map.Entry<UUID, Map<UUID, Req>> e : new ArrayList<>(pending.entrySet())) {
            for (Req r : new ArrayList<>(e.getValue().values())) {
                if (r.expired(now)) {
                    e.getValue().remove(r.from);
                    out.add(new UUID[]{e.getKey(), r.from});
                }
            }
            if (e.getValue().isEmpty()) pending.remove(e.getKey());
        }
        return out;
    }

    public synchronized int count(UUID target) {
        Map<UUID, Req> m = pending.get(target);
        return m == null ? 0 : m.size();
    }

    /** Drop every request FROM this sender (logout): no ghost accepts. */
    public synchronized void clearSender(UUID from) {
        for (UUID target : new ArrayList<>(pending.keySet())) {
            remove(target, from);
        }
    }

    private void prune(UUID target, long now) {
        Map<UUID, Req> m = pending.get(target);
        if (m == null) return;
        m.values().removeIf(r -> r.expired(now));
        if (m.isEmpty()) pending.remove(target);
    }
}
