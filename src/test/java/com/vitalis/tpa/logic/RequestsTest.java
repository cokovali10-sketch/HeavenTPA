package com.vitalis.tpa.logic;

import com.vitalis.tpa.Requests;
import com.vitalis.tpa.Requests.Kind;
import com.vitalis.tpa.Requests.Req;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Request lifecycle: add, take latest/by-name, deny, expiry, logout cleanup. */
class RequestsTest {
    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();

    @Test
    void addAndTakeLatest() {
        Requests r = new Requests();
        r.add(b, a, "A", Kind.TO, 1000, 60_000);
        r.add(b, c, "C", Kind.HERE, 2000, 60_000);
        Req last = r.take(b, "", 3000);
        assertNotNull(last);
        assertEquals("C", last.fromName);
        Req first = r.take(b, "", 3000);
        assertNotNull(first);
        assertEquals("A", first.fromName);
        assertNull(r.take(b, "", 3000));
    }

    @Test
    void takeByName() {
        Requests r = new Requests();
        r.add(b, a, "A", Kind.TO, 1000, 60_000);
        r.add(b, c, "C", Kind.HERE, 1000, 60_000);
        Req got = r.take(b, "a", 2000);
        assertNotNull(got);
        assertEquals("A", got.fromName);
        assertEquals(1, r.count(b));
        assertNull(r.take(b, "Nobody", 2000));
    }

    @Test
    void expiredGone() {
        Requests r = new Requests();
        r.add(b, a, "A", Kind.TO, 1000, 1000);
        assertNull(r.take(b, "", 5000));
        assertEquals(0, r.count(b));
    }

    @Test
    void resendReplaces() {
        Requests r = new Requests();
        r.add(b, a, "A", Kind.TO, 1000, 60_000);
        r.add(b, a, "A", Kind.HERE, 2000, 60_000);
        assertEquals(1, r.count(b));
        Req got = r.take(b, "", 3000);
        assertNotNull(got);
        assertEquals(Kind.HERE, got.kind);
    }

    @Test
    void denyAndLogoutCleanup() {
        Requests r = new Requests();
        r.add(b, a, "A", Kind.TO, 1000, 60_000);
        r.add(c, a, "A", Kind.TO, 1000, 60_000);
        r.remove(b, a);
        assertEquals(0, r.count(b));
        assertEquals(1, r.count(c));
        r.clearSender(a);
        assertEquals(0, r.count(c));
    }

    @Test
    void sweeperFindsExpired() {
        Requests r = new Requests();
        r.add(b, a, "A", Kind.TO, 1000, 1000);
        r.add(b, c, "C", Kind.TO, 1000, 600_000);
        List<UUID[]> dead = r.expire(5000);
        assertEquals(1, dead.size());
        assertEquals(1, r.count(b));
    }
}
