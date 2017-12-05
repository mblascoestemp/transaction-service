package org.mblascoespar.transactionservice.repository;

import org.mblascoespar.transactionservice.model.Transaction;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Repository
public interface TransactionRepository {

    void insertOrUpdate(Transaction transaction);

    void remove(Transaction transaction);

    void remove(Collection<Transaction> transactions);

    Collection<Transaction> getAll();

}
