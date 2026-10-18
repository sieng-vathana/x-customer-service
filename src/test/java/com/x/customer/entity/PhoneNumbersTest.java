package com.x.customer.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhoneNumbersTest {

    @Test
    void cambodianNumbersMatchHoweverTheyAreTyped() {
        assertEquals("012345678", PhoneNumbers.normalize("012 345 678"));
        assertEquals("012345678", PhoneNumbers.normalize("+855 12 345 678"));
        assertEquals("012345678", PhoneNumbers.normalize("855-12-345-678"));
        assertEquals("012345678", PhoneNumbers.normalize("12345678"));
        assertEquals("0961234567", PhoneNumbers.normalize("(+855) 96 123 4567"));
    }

    @Test
    void textWithoutDigitsHasNoLookupForm() {
        assertNull(PhoneNumbers.normalize(null));
        assertNull(PhoneNumbers.normalize(" - "));
    }
}
