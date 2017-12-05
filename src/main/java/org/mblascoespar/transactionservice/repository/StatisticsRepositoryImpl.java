package org.mblascoespar.transactionservice.repository;


import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.validators.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StatisticsRepositoryImpl implements StatisticsRepository {

    private Statistics stats = Statistics.baseStatistics;

    private static Logger log = LoggerFactory.getLogger(StatisticsRepositoryImpl.class);


    @Override
    public Statistics update(Statistics statistics) {
        ArgumentValidator.validateNotNull(statistics,log, "Tried to insert null statistics");
        stats = new Statistics(statistics);
        return stats;
    }

    @Override
    public Statistics get() {
        return this.stats;
    }
}
