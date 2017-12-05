package org.mblascoespar.transactionservice.repository;


import org.mblascoespar.transactionservice.model.Transaction;
import org.mblascoespar.transactionservice.services.StatisticsServiceImpl;
import org.mblascoespar.transactionservice.validators.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class TransactionRepositoryImpl implements TransactionRepository {


    private static Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private ConcurrentMap<Integer, Transaction> map = new ConcurrentHashMap<>();

    @Override
    public void insertOrUpdate(Transaction transaction) {
        ArgumentValidator.validateNotNull(transaction, log, "Tried to insert null transaction");
        map.put(transaction.getId(), transaction);
        log.debug("Added transaction to map {}", transaction);

    }

    @Override
    public void remove(Transaction transaction) {
        ArgumentValidator.validateNotNull(transaction, log, "Tried to insert null transaction");
        map.remove(transaction.getId());
    }

    @Override
    synchronized public void remove(Collection<Transaction> transactions) {
        transactions.stream().forEach(i -> remove(i));

    }

    @Override
    public Collection<Transaction> getAll() {
        return new ArrayList<Transaction>(map.values());
    }
}
