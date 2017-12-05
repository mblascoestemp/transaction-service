package org.mblascoespar.transactionservice.repositories;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.repository.StatisticsRepository;
import org.mblascoespar.transactionservice.repository.StatisticsRepositoryImpl;
import org.mblascoespar.transactionservice.repository.StatisticsRepositoryV2;
import org.mblascoespar.transactionservice.repository.StatisticsRepositoryV2Impl;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JUnit4.class)
public class StatisticsRepositoryV2ImplTest {

    StatisticsRepositoryV2 statisticsRepository = new StatisticsRepositoryV2Impl();

    final static Statistics SAMPLE_STATS = Statistics.baseStatistics;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(statisticsRepository, "precision", 100);
    }

    @Test
    public void upsertStatisticInserts() throws Exception {
        statisticsRepository.upsertStatistic(0,SAMPLE_STATS);
        assertThat(statisticsRepository.getStatistics().get(0),equalTo(SAMPLE_STATS));

    }

    @Test
    public void upsertStatisticUpdates() throws Exception {
        statisticsRepository.upsertStatistic(0,SAMPLE_STATS);
        Statistics newStats = new Statistics(1,1,1,1,1);
        statisticsRepository.upsertStatistic(0,newStats);
        assertThat(statisticsRepository.getStatistics().get(0),equalTo(newStats));

    }

    @Test(expected = IllegalArgumentException.class)
    public void upsertStatisticNullStatistics() throws Exception {
        statisticsRepository.upsertStatistic(0,null);
    }
    @Test
    public void upsertStatisticJumpyIndex() throws Exception {
        statisticsRepository.upsertStatistic(3,Statistics.baseStatistics);
        assertThat(statisticsRepository.getStatistics().get(3),equalTo(SAMPLE_STATS));

    }
    
    @Test
    public void getStatisticsEmptyMap() throws Exception {

    }

    @Test
    public void getStatisticsDefensiveCopyWorks() throws Exception {

    }

}
