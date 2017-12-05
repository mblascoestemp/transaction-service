package org.mblascoespar.transactionservice.services;


import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.model.Transaction;
import org.mblascoespar.transactionservice.repository.StatisticsRepositoryV2;
import org.mblascoespar.transactionservice.validators.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
public class StatisticsServiceV2Impl implements StatisticsServiceV2 {

    private static Logger log = LoggerFactory.getLogger(StatisticsServiceV2Impl.class);

    @Autowired
    private StatisticsRepositoryV2 statisticsRepository;

    @Autowired
    private StatisticsServiceUtils utils;

    @Override
    public void addTransaction(Transaction transaction) {
        ArgumentValidator.validateNotNull(transaction, log, "Tried to insert null transaction");
        ArgumentValidator.validateNotFutureOrNegative(transaction.getTimestamp(), log, String.format(" invalid timestamp %d", transaction.getTimestamp()));

        log.debug("Adding transaction {}", transaction);

        int index = utils.calculateBucketPosition(transaction.getTimestamp());


        statisticsRepository.compute(index, computeStatistics(transaction, index));
    }

    private BiFunction<Integer, Statistics, Statistics> computeStatistics(Transaction transaction, int index) {
        return (integer, statistics) -> {
            if (statistics != null) {
                log.debug("Recalculation stats on bucket {} with transaction {} and statistic {} ", index, transaction, statistics);
                return utils.recalculateStats(transaction,statistics);
            }

            else{
                log.debug("Recalculation stats on bucket {} with transaction {} and statistic {} ", index, transaction, Statistics.baseStatistics);
                return utils.recalculateStats(transaction, Statistics.baseStatistics);
            }

        };
    }

    @Override
    public Statistics getStatistics(long receptionTime) {
        ArgumentValidator.validateNotFutureOrNegative(receptionTime, log, String.format(" invalid timestamp %d", receptionTime));
        Map<Integer, Statistics> statsWindowCopy = statisticsRepository.getStatistics();
        log.debug("getting statistics at {}", receptionTime);

        statsWindowCopy
                .entrySet()
                .removeIf(i -> !utils.isStatisticsInCurrentWindowTime(i.getValue(), receptionTime));


        Statistics calcStats;
        if (statsWindowCopy.isEmpty())
            calcStats = Statistics.baseStatistics;
        else
            calcStats = statsWindowCopy.values().stream().reduce(Statistics.baseStatistics, (a, b) -> utils.mergeStats(a, b));
        log.info("Calculated new statistics at timestamp {} with value {}", receptionTime, calcStats);
        return calcStats;

    }


}
