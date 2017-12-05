package org.mblascoespar.transactionservice.repository;


import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.validators.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;


@Component
public class StatisticsRepositoryV2Impl implements StatisticsRepositoryV2 {

    private static Logger log = LoggerFactory.getLogger(StatisticsRepositoryV2Impl.class);

    @Value("${statistics.precision}")
    private Integer precision;

    private ConcurrentHashMap<Integer, Statistics> map = new ConcurrentHashMap<>();

    @Override
    public void upsertStatistic(int index, Statistics statistics) {
        ArgumentValidator.validateNotNull(statistics, log, "Tried to insert null statistics");
        log.debug("Inserted in slot {} from window: {}", index, statistics);
        map.put(index, statistics);
    }


    @Override
    public void compute(Integer index, BiFunction<? super Integer, ? super Statistics, ? extends Statistics> fun) {
        map.compute(index,  fun);
    }

    @Override
    public Map<Integer, Statistics> getStatistics() {
        log.debug("Returning a copy of the statistics map");
        return new HashMap<>(map);
    }



}
