package br.com.sales.code.bug.iam.utils;

import java.util.regex.Pattern;

public class DomainUtil {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    public static boolean isValidUuid(String value) {
        return value != null && UUID_PATTERN.matcher(value).matches();
    }
}
