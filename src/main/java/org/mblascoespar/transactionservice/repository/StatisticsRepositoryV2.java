package org.mblascoespar.transactionservice.repository;

import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.model.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface StatisticsRepositoryV2 {

    void upsertStatistic(int index, Statistics statistics);

    void compute(Integer index, BiFunction<? super Integer, ? super Statistics, ? extends Statistics> fun);

    Map<Integer,Statistics> getStatistics();

}
