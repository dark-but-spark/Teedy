package com.sismics.security;

import com.google.common.collect.Sets;
import com.sismics.docs.core.constant.Constants;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityPrincipalTest {
    @Test
    void shouldExposeAnonymousPrincipalState() {
        AnonymousPrincipal principal = new AnonymousPrincipal();
        DateTimeZone zone = DateTimeZone.forID("Asia/Shanghai");
        principal.setDateTimeZone(zone);

        assertNull(principal.getId());
        assertEquals(AnonymousPrincipal.ANONYMOUS, principal.getName());
        assertTrue(principal.isAnonymous());
        assertSame(zone, principal.getDateTimeZone());
        assertNull(principal.getEmail());
        assertTrue(principal.getGroupIdSet().isEmpty());
        assertFalse(principal.isGuest());
    }

    @Test
    void shouldExposeUserPrincipalStateAndGuestFlag() {
        UserPrincipal principal = new UserPrincipal("user-1", "alice");
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        principal.setDateTimeZone(zone);
        principal.setEmail("alice@example.com");
        principal.setBaseFunctionSet(Sets.newHashSet("READ", "WRITE"));
        principal.setGroupIdSet(Sets.newHashSet("group-1"));

        assertEquals("user-1", principal.getId());
        assertEquals("alice", principal.getName());
        assertSame(zone, principal.getDateTimeZone());
        assertEquals("alice@example.com", principal.getEmail());
        assertEquals(2, principal.getBaseFunctionSet().size());
        assertEquals(1, principal.getGroupIdSet().size());
        assertFalse(principal.isAnonymous());
        assertFalse(principal.isGuest());

        principal.setId(Constants.GUEST_USER_ID);
        principal.setName("guest");
        assertTrue(principal.isGuest());
        assertEquals("guest", principal.getName());
    }
}
