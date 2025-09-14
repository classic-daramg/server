package com.daramg.server.auth.util;

import java.util.concurrent.ThreadLocalRandom;

public final class VerificationCodeGenerator {
    private VerificationCodeGenerator() {}

    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int MIN_BOUND = (int) Math.pow(10, VERIFICATION_CODE_LENGTH - 1);
    private static final int MAX_BOUND = (int) Math.pow(10, VERIFICATION_CODE_LENGTH);

    public static String generate() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(MIN_BOUND, MAX_BOUND));
    }
}
