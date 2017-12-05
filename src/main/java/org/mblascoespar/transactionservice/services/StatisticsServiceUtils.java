package org.mblascoespar.transactionservice.services;

import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.model.Transaction;
import org.mblascoespar.transactionservice.validators.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StatisticsServiceUtils {

    @Value("${statistics.precision}")
    private Integer precision;

    @Value("${statistics.window.size.in.millis}")
    private Integer windowSize;

    private static Logger log = LoggerFactory.getLogger(StatisticsServiceUtils.class);


    protected int calculateBucketPosition(long timestamp) {
        long offset = timestamp % windowSize;
        log.debug("Offset of the bucket {}", offset);
        int index = (int) Math.floor((offset * precision) / windowSize);
        if (index < 0 || index > precision) {
            String msg = String.format("Unexpected error: index calculated out of the bucket. Index %d, precision %d", index, precision);
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return index;

    }

    protected Statistics recalculateStats(Transaction transaction, Statistics stats) {
        ArgumentValidator.validateNotNull(stats, log, "Tried recalculate with null statistics");
        ArgumentValidator.validateNotNull(transaction, log, "Tried recalculate with  null transaction");
        ArgumentValidator.validateNotFutureOrNegative(transaction.getTimestamp(), log, String.format(" invalid timestamp %d", transaction.getTimestamp()));
        ArgumentValidator.validateNotFutureOrNegative(stats.getTimestamp(), log, String.format(" invalid timestamp %d", stats.getTimestamp()));

        Statistics recalculatedStats;
        if (transaction.getTimestamp() - windowSize > stats.getTimestamp())
            recalculatedStats = recalculateStatsFromTransaction(transaction, Statistics.baseStatistics);
        else {
            recalculatedStats = recalculateStatsFromTransaction(transaction, stats);
        }
        return recalculatedStats;
    }

    private Statistics recalculateStatsFromTransaction(Transaction transaction, Statistics stats) {
        return new Statistics(
                stats.getCount() + 1,
                stats.getCount() == 0 ? transaction.getAmount() : Math.min(stats.getMin(), transaction.getAmount()),
                Math.max(stats.getMax(), transaction.getAmount()),
                stats.getAvg() + (transaction.getAmount() - stats.getAvg()) / (stats.getCount() + 1),
                stats.getSum() + transaction.getAmount());
    }


    protected Statistics mergeStats(Statistics statsA, Statistics statsB) {
        ArgumentValidator.validateNotNull(statsA, log, "Tried to insert null statistics");
        ArgumentValidator.validateNotNull(statsB, log, "Tried to insert null statistics");

        double min = Math.min(
                statsA.getCount() == 0 ? statsB.getMin() : statsA.getMin(),
                statsB.getCount() == 0 ? statsA.getMin() : statsB.getMin());

        return new Statistics(
                statsA.getCount() + statsB.getCount(),
                min,
                Math.max(statsA.getMax(), statsB.getMax()),
                statsA.getCount() == 0 && statsB.getCount() == 0 ? 0 :
                        (statsA.getAvg() * statsA.getCount() + statsB.getAvg() * statsB.getCount()) / (statsA.getCount() + statsB.getCount()),
                statsA.getSum() + statsB.getSum());
    }


    protected boolean isStatisticsInCurrentWindowTime(Statistics statistics, long receptionTime) {
        ArgumentValidator.validateNotNull(statistics, log, "isStatisticsInCurrentWindowTime statistics not null");
        ArgumentValidator.validateNotFutureOrNegative(statistics.getTimestamp(), log, String.format("invalid timestamp %d", statistics.getTimestamp()));
        ArgumentValidator.validateNotFutureOrNegative(receptionTime, log, String.format("invalid timestamp %d", receptionTime));

        boolean isInwindow = false;
        if (receptionTime - windowSize < statistics.getTimestamp()) {
            log.debug("statistics {} evaluated at {} is in time window", statistics, receptionTime);
            isInwindow = true;
        }

        return isInwindow;
    }

}
