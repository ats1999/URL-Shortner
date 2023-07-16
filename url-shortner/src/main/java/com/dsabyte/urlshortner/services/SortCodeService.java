package com.dsabyte.urlshortner.services;

import org.springframework.stereotype.Service;

@Service
public class SortCodeService {
    private static long counter = 0;
    private static final String BASE62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static String encodeBase62(long number) {
        StringBuilder sb = new StringBuilder();
        int base = BASE62_CHARACTERS.length();

        while (number > 0) {
            int index = (int) (number % base);
            sb.insert(0, BASE62_CHARACTERS.charAt(index));
            number /= base;
        }

        return sb.toString();
    }

    private static synchronized long getCounter() {
        return counter++;
    }

    public static String getSortCode() {
        String base62Sortcode = encodeBase62(getCounter());
        int sortCodeLength = 7;

        base62Sortcode = "0"
                .repeat(sortCodeLength - base62Sortcode.length())
                + base62Sortcode;

        return base62Sortcode;
    }
}
