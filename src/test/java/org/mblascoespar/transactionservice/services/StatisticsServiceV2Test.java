package org.mblascoespar.transactionservice.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.model.Transaction;
import org.mblascoespar.transactionservice.repository.StatisticsRepositoryV2;
import org.mblascoespar.transactionservice.repository.StatisticsRepositoryV2Impl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.function.BiFunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class StatisticsServiceV2Test {


    @Mock
    private
    StatisticsRepositoryV2 statisticsRepository;

    @Mock
    private
    StatisticsServiceUtils utils;

    @InjectMocks
    private StatisticsServiceV2Impl statisticsService = new StatisticsServiceV2Impl();

    private final int PRECISION = 6000;

    private final int WINDOW_SIZE = 6000;

    private final Statistics INIT_STATISTICS = Statistics.baseStatistics;

    private final Statistics SAMPLE_STATISTICS = new Statistics(1, 0, 100, 50, 100);

    private final Transaction SAMPLE_TRANSACTION = new Transaction(100.0, Instant.now().toEpochMilli());

    private static Logger log = LoggerFactory.getLogger(StatisticsRepositoryV2Impl.class);

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(utils, "precision", 6000);
        ReflectionTestUtils.setField(utils, "windowSize", 6000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStatisticsFutureTimestamp() throws Exception {
        Statistics stats = statisticsService.getStatistics(Instant.now().plusMillis(1000).toEpochMilli());
    }

    @Test
    public void getStatisticsOneValidStatisticInMap() throws Exception {

        HashMap<Integer, Statistics> sampleMap = new HashMap<>();
        sampleMap.put(0, SAMPLE_STATISTICS);
        when(statisticsRepository.getStatistics()).thenReturn(sampleMap);

        when(utils.isStatisticsInCurrentWindowTime(any(), anyLong())).thenReturn(true);
        Statistics stats = statisticsService.getStatistics(Instant.now().toEpochMilli());
        verify(utils, times(1)).mergeStats(any(), any());

    }

    @Test
    public void getStatisticsNoValidStatisticInMapReturnsInitStatistic() throws Exception {

        HashMap<Integer, Statistics> sampleMap = new HashMap<>();
        sampleMap.put(0, SAMPLE_STATISTICS);
        when(statisticsRepository.getStatistics()).thenReturn(sampleMap);

        when(utils.isStatisticsInCurrentWindowTime(any(), anyLong())).thenReturn(false);
        Statistics stats = statisticsService.getStatistics(Instant.now().toEpochMilli());
        assertThat(stats.getCount(), equalTo(0));
        assertThat(stats.getSum(), equalTo(0.0));

    }

    @Test
    public void getStatisticsMultipleValidStatisticGetMerged() throws Exception {

        HashMap<Integer, Statistics> sampleMap = new HashMap<>();
        Statistics a = new Statistics(2, 50, 200, 125, 250);
        Statistics b = new Statistics(4, 100, 300, 200, 800);
        sampleMap.put(0, a);
        sampleMap.put(1, b);
        when(statisticsRepository.getStatistics()).thenReturn(sampleMap);
        when(utils.isStatisticsInCurrentWindowTime(any(), anyLong())).thenReturn(true);
        when(utils.mergeStats(any(), eq(a))).thenReturn(a);
        Statistics stats = statisticsService.getStatistics(1);
        verify(utils, times(1)).mergeStats(
                argThat(hasProperty("count", equalTo(0))),
                argThat(hasProperty("count", equalTo(2))));
        verify(utils, times(1)).
                mergeStats(
                        argThat(hasProperty("count", equalTo(2))),
                        argThat(hasProperty("count", equalTo(4))));
    }

    @Test
    public void getStatisticsRejectedDoNotGetMerged() throws Exception {

        HashMap<Integer, Statistics> sampleMap = new HashMap<>();
        Statistics a = new Statistics(2, 50, 200, 125, 250);
        Statistics b = new Statistics(4, 100, 300, 200, 800);
        sampleMap.put(0, a);
        sampleMap.put(1, b);
        when(statisticsRepository.getStatistics()).thenReturn(sampleMap);
        when(utils.isStatisticsInCurrentWindowTime(eq(a), anyLong())).thenReturn(false);
        when(utils.isStatisticsInCurrentWindowTime(eq(b), anyLong())).thenReturn(true);
        statisticsService.getStatistics(1);
        verify(utils).mergeStats(
                argThat(hasProperty("count", equalTo(0))),
                argThat(hasProperty("count", equalTo(4))));
    }


    @Test
    public void getStatisticsAllOldStatistics() throws Exception {

        HashMap<Integer, Statistics> sampleMap = new HashMap<>();
        Statistics a = new Statistics(2, 50, 200, 125, 250);
        Statistics b = new Statistics(4, 100, 300, 200, 800);
        sampleMap.put(0, a);
        sampleMap.put(1, b);
        when(utils.isStatisticsInCurrentWindowTime(any(), anyLong())).thenReturn(false);
        Statistics stats = statisticsService.getStatistics(1);
        assertThat(stats.getCount(), equalTo(0));
        assertThat(stats.getSum(), equalTo(0.0));
    }

    @Test
    public void getStatisticsNonSecuentialIndexes() throws Exception {
        HashMap<Integer, Statistics> sampleMap = new HashMap<>();
        sampleMap.put(0, new Statistics(2, 50, 200, 125, 250));
        sampleMap.put(15, new Statistics(4, 100, 300, 200, 800));
        when(statisticsRepository.getStatistics()).thenReturn(sampleMap);
        when(utils.isStatisticsInCurrentWindowTime(any(), anyLong())).thenReturn(true);
        when(utils.mergeStats(any(), any())).thenCallRealMethod();
        Statistics stats = statisticsService.getStatistics(Instant.now().toEpochMilli());
        assertThat(stats.getCount(), equalTo(6));
        assertThat(stats.getSum(), equalTo(1050.0));
        assertThat(stats.getAvg(), equalTo(175.0));
        assertThat(stats.getMax(), equalTo(300.0));
        assertThat(stats.getMin(), equalTo(50.0));
    }


    @Test(expected = IllegalArgumentException.class)
    public void addTransactionFutureTimestampReception() throws Exception {
        statisticsService.addTransaction(new Transaction(100.0, Instant.now().plusMillis(1000).toEpochMilli()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTransactionFutureTimestampTransaction() throws Exception {
        statisticsService.addTransaction(new Transaction(
                100.0, Instant.now().plusMillis(1000).toEpochMilli()));
    }


    @Test
    public void addTransactionUpdatesStatistics() throws Exception {
        HashMap<Integer, Statistics> sampleMap = new HashMap<>();
        sampleMap.put(0, Statistics.baseStatistics);
        when(utils.calculateBucketPosition(anyLong())).thenReturn(0);
        statisticsService.addTransaction(SAMPLE_TRANSACTION);
        verify(statisticsRepository).compute( eq(0), any(BiFunction.class));
        verify(utils).calculateBucketPosition(SAMPLE_TRANSACTION.getTimestamp());

    }


}
