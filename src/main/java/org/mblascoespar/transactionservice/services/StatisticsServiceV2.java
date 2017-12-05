package org.mblascoespar.transactionservice.services;


import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.model.Transaction;

public interface StatisticsServiceV2 {

    void addTransaction(Transaction transaction);

    Statistics getStatistics(long receptionTime);
}
