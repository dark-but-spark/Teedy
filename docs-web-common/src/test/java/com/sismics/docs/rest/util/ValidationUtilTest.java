package com.sismics.docs.rest.util;

import com.sismics.rest.exception.ClientException;
import com.sismics.rest.util.ValidationUtil;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilTest {
    @Test
    void shouldValidateRequiredLengthAndBlankRules() {
        ValidationUtil.validateRequired("value", "field");

        assertThrows(ClientException.class, () -> ValidationUtil.validateRequired(null, "field"));
        assertEquals("trimmed", ValidationUtil.validateLength("  trimmed  ", "field", 3, 10));
        assertEquals("", ValidationUtil.validateLength("   ", "field", 1, 10, true));
        assertThrows(ClientException.class, () -> ValidationUtil.validateLength(null, "field", 1, 10));
        assertThrows(ClientException.class, () -> ValidationUtil.validateLength("ab", "field", 3, 10));
        assertThrows(ClientException.class, () -> ValidationUtil.validateLength("toolongvalue", "field", 1, 5));
        assertThrows(ClientException.class, () -> ValidationUtil.validateStringNotBlank("   ", "field"));
    }

    @Test
    void shouldValidatePatternsAndFormats() {
        ValidationUtil.validateEmail("user@example.com", "email");
        assertThrows(ClientException.class, () -> ValidationUtil.validateEmail("invalid", "email"));

        ValidationUtil.validateHexColor("#A1B2C3", "color", false);
        assertEquals("", ValidationUtil.validateLength("", "color", 7, 7, true));
        assertThrows(ClientException.class, () -> ValidationUtil.validateHexColor("#123", "color", false));

        ValidationUtil.validateTagName("finance");
        assertThrows(ClientException.class, () -> ValidationUtil.validateTagName("bad tag"));
        assertThrows(ClientException.class, () -> ValidationUtil.validateTagName("bad:tag"));

        assertEquals("https://teedy.io", ValidationUtil.validateHttpUrl(" https://teedy.io ", "url"));
        assertThrows(ClientException.class, () -> ValidationUtil.validateHttpUrl("ftp://teedy.io", "url"));

        ValidationUtil.validateAlphanumeric("Alpha_123", "value");
        assertThrows(ClientException.class, () -> ValidationUtil.validateAlphanumeric("Alpha-123", "value"));

        ValidationUtil.validateUsername("user.name-1@example", "username");
        assertThrows(ClientException.class, () -> ValidationUtil.validateUsername("bad username", "username"));

        ValidationUtil.validateRegex("ABC-12", "code", "[A-Z]{3}-\\d{2}");
        assertThrows(ClientException.class, () -> ValidationUtil.validateRegex("abc-12", "code", "[A-Z]{3}-\\d{2}"));
    }

    @Test
    void shouldValidateNumbersAndDates() {
        assertEquals(42, ValidationUtil.validateInteger("42", "count"));
        assertEquals(1234567890123L, ValidationUtil.validateLong("1234567890123", "count"));
        assertThrows(ClientException.class, () -> ValidationUtil.validateInteger("NaN", "count"));
        assertThrows(ClientException.class, () -> ValidationUtil.validateLong("NaN", "count"));

        Date parsed = ValidationUtil.validateDate("1714287600000", "date", false);
        assertEquals(1714287600000L, parsed.getTime());
        assertNull(ValidationUtil.validateDate(null, "date", true));
        assertThrows(ClientException.class, () -> ValidationUtil.validateDate("", "date", false));
        assertThrows(ClientException.class, () -> ValidationUtil.validateDate("not-a-date", "date", false));
    }
}
