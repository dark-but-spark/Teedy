package com.sismics.util.filter;

import com.sismics.docs.core.model.jpa.User;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertNull;

class HeaderBasedSecurityFilterTest {
    @Test
    void shouldReturnNullWhenHeaderAuthenticationIsDisabled() {
        TestableHeaderBasedSecurityFilter filter = new TestableHeaderBasedSecurityFilter();
        filter.init(filterConfig("false"));

        assertNull(filter.authenticateForTest(requestWithHeader("alice")));
    }

    @Test
    void shouldReturnNullWhenHeaderIsMissingEvenIfEnabled() {
        TestableHeaderBasedSecurityFilter filter = new TestableHeaderBasedSecurityFilter();
        filter.init(filterConfig("true"));

        assertNull(filter.authenticateForTest(requestWithHeader(null)));
    }

    @Test
    void shouldReadEnabledFlagFromSystemProperty() {
        String previous = System.getProperty("docs.header_authentication");
        System.setProperty("docs.header_authentication", "true");
        try {
            TestableHeaderBasedSecurityFilter filter = new TestableHeaderBasedSecurityFilter();
            filter.init(filterConfig("false"));

            assertNull(filter.authenticateForTest(requestWithHeader(null)));
        } finally {
            if (previous == null) {
                System.clearProperty("docs.header_authentication");
            } else {
                System.setProperty("docs.header_authentication", previous);
            }
        }
    }

    private FilterConfig filterConfig(String enabled) {
        return new FilterConfig() {
            @Override
            public String getFilterName() {
                return "header-based-security-filter";
            }

            @Override
            public ServletContext getServletContext() {
                return null;
            }

            @Override
            public String getInitParameter(String name) {
                return "enabled".equals(name) ? enabled : null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(Collections.singleton("enabled"));
            }
        };
    }

    private HttpServletRequest requestWithHeader(String username) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getHeader".equals(method.getName())) {
                        return HeaderBasedSecurityFilter.AUTHENTICATED_USER_HEADER.equals(args[0]) ? username : null;
                    }
                    if ("toString".equals(method.getName())) {
                        return "MockHttpServletRequest";
                    }
                    return null;
                });
    }

    private static class TestableHeaderBasedSecurityFilter extends HeaderBasedSecurityFilter {
        User authenticateForTest(HttpServletRequest request) {
            return super.authenticate(request);
        }
    }
}
