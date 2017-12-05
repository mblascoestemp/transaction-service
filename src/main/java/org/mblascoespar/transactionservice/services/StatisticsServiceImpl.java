package org.mblascoespar.transactionservice.services;


import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.model.Transaction;
import org.mblascoespar.transactionservice.repository.StatisticsRepository;
import org.mblascoespar.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatisticsServiceImpl implements StatisticsService {

    private static Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Value( "${statistics.window.size.in.millis}" )
    private long window;

    @Override
    public void addTransaction(Transaction transaction) {
        log.debug("Adding transaction {}",transaction);
        if (transaction == null)
            throw new InvalidParameterException("Tried To add a null transaction");
        transactionRepository.insertOrUpdate(transaction);
    }

    @Override
    public Statistics getStatistics() {

        long boundaryTimeInMillis = Instant.now().minusMillis(window).toEpochMilli();
        ArrayList<Transaction> copy = (ArrayList<Transaction>) transactionRepository.getAll();
        log.debug("Current number of transactions {}",copy.size());


        List<Transaction> transactionsInTimeRange= copy.stream()
                .filter(i -> i.getTimestamp() > boundaryTimeInMillis).collect(Collectors.toList());

        log.debug("Transactions in time range {}",transactionsInTimeRange.size());
        Statistics statistics;

        if (transactionsInTimeRange.isEmpty()) {
            statistics = new Statistics(0, 0, 0, 0, 0);
        }
        else {
            List<Transaction> sortedFilteredCopy = transactionsInTimeRange.stream()
                    .sorted((i, j) -> Double.compare(j.getAmount(),i.getAmount()))
                    .collect(Collectors.toList());
            statistics = new Statistics(
                    sortedFilteredCopy.size(),
                    sortedFilteredCopy.get(sortedFilteredCopy.size() - 1).getAmount(),
                    sortedFilteredCopy.get(0).getAmount(),
                    sortedFilteredCopy.stream().mapToDouble(Transaction::getAmount).average().getAsDouble(),
                    sortedFilteredCopy.stream().mapToDouble(Transaction::getAmount).sum());
        }
        //  CleanUp old entries
        copy.removeAll(transactionsInTimeRange);
        log.debug("Transactions to be cleaned {}",copy.size());
        transactionRepository.remove(copy);
        log.debug("Calculated new statistics: {}",statistics);
        return statisticsRepository.update(statistics);
    }
}
