package org.mblascoespar.transactionservice.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;
import java.util.UUID;

public class Transaction {

    @JsonIgnore
    private final int id;
    private final double amount;
    private final long timestamp;

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getId() {
        return id;
    }

    public Transaction(TransactionRequestBody transactionRequestBody) {
        super();
        this.amount = transactionRequestBody.getAmount();

        this.timestamp = transactionRequestBody.getTimestamp();
        this.id = hashCode();
    }

    public Transaction(double amount, long timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.id = UUID.randomUUID().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(timestamp, that.timestamp);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, amount, timestamp);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", dateInMillis=" + timestamp +
                '}';
    }
}
