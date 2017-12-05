package org.mblascoespar.transactionservice.validators;

import org.slf4j.Logger;

import java.time.Instant;

public class ArgumentValidator {

    public static <E> E validateNotNull(E value , Logger log , String msg) {
        if (value == null) {
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return value;
    }


    public static long validateNotFutureOrNegative(long timestamp, Logger log , String msg) {
        if (timestamp > Instant.now().toEpochMilli() || timestamp < 0) {
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return timestamp;
    }


}
