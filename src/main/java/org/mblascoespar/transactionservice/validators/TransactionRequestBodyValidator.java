package org.mblascoespar.transactionservice.validators;

import org.mblascoespar.transactionservice.model.TransactionRequestBody;
import org.springframework.validation.Errors;

import java.time.Instant;


public class TransactionRequestBodyValidator {

    private long duration;

    public TransactionRequestBodyValidator(long duration) {
        this.duration = duration;
    }

    public void validate(Object o, Errors errors) {
        TransactionRequestBody requestBody = (TransactionRequestBody) o;
        long now = Instant.now().toEpochMilli();

        if (errors.hasFieldErrors())
            return;
        if (requestBody.getTimestamp() > Instant.now().toEpochMilli())
            errors.rejectValue("timestamp", "timestamp.outOfBoundaries", "The timestamp  cannot be after time of reception.");
        if (requestBody.getTimestamp() < 0)
            errors.rejectValue("timestamp", "timestamp.outOfBoundaries", "The timestamp is negative.");
        if (requestBody.getTimestamp() > Instant.now().toEpochMilli())
            errors.rejectValue("timestamp", "timestamp.outOfBoundaries", "The timestamp  cannot be after time of reception.");
        if (now - duration > requestBody.getTimestamp())
            errors.rejectValue(
                    "timestamp",
                    "timestamp.outOfBoundaries",
                    String.format("The timestamp is too old transaction timestamp {} limit timestamp {}", requestBody.getTimestamp(), now - duration));
    }
}
