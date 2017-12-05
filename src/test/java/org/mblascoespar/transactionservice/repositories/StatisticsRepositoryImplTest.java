package org.mblascoespar.transactionservice.repositories;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.repository.StatisticsRepository;
import org.mblascoespar.transactionservice.repository.StatisticsRepositoryImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JUnit4.class)
public class StatisticsRepositoryImplTest {

    StatisticsRepository statisticsRepository = new StatisticsRepositoryImpl();

    @Test(expected = IllegalArgumentException.class)
    public void updateNull() throws Exception {
        statisticsRepository.update(null);
    }

    @Test
    public void getBase() throws Exception {
        assertThat(statisticsRepository.get().getCount(), equalTo(0));
    }

    @Test
    public void getAfterUpdate() throws Exception {
        Statistics statistics = new Statistics(1, 1, 1, 1, 1);
        statisticsRepository.update(statistics);
        assertThat(statisticsRepository.get().getCount(), equalTo(1));
    }
}
