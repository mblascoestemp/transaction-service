package org.mblascoespar.transactionservice.services;


import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.model.Transaction;

public interface StatisticsService {

    void addTransaction(Transaction transaction);

    Statistics getStatistics();
}
