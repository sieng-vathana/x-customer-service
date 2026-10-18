package com.x.customer.entity;

/** Puts phone numbers in one form so "+855 12 345 678", "012-345-678" and "12345678" match. */
public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    /** Digits only, with Cambodia's +855 replaced by a leading 0; null when there are no digits. */
    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.startsWith("855") && digits.length() >= 11) {
            digits = "0" + digits.substring(3);
        } else if (!digits.startsWith("0") && (digits.length() == 8 || digits.length() == 9)) {
            digits = "0" + digits;
        }
        return digits;
    }
}
