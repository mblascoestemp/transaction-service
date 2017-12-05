package org.mblascoespar.transactionservice.repository;

import org.mblascoespar.transactionservice.model.Statistics;

public interface StatisticsRepository {

    Statistics update(Statistics statistics);

    Statistics get();

}
