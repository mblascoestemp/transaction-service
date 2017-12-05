package org.mblascoespar.transactionservice.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
public class TransactionRequestBody {

    @NotNull(message = "amount must not be null or empty")
    private Double amount;

    @NotNull(message = "timestamp must not be null or empty")
    private Long timestamp;

    public TransactionRequestBody(double amount, Long timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public TransactionRequestBody() {
    }

    @Override
    public String toString() {
        return "TransactionRequestBody{" +
                "amount='" + amount + '\'' +
                ", dateInMillis=" + timestamp +
                '}';
    }
}
