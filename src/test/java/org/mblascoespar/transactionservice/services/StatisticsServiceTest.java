package org.mblascoespar.transactionservice.services;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mblascoespar.transactionservice.model.Statistics;
import org.mblascoespar.transactionservice.model.Transaction;
import org.mblascoespar.transactionservice.repository.StatisticsRepository;
import org.mblascoespar.transactionservice.repository.TransactionRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class StatisticsServiceTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    StatisticsRepository statisticsRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService = new StatisticsServiceImpl();


    private Statistics INIT_STATISTICS = new Statistics(0, 0, 0, 0, 0);


    private final int WINDOW_IN_MILLIS = 60000;


    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(statisticsService, "window", 60000);
    }

    @Test
    public void addTransactionHappyCase() throws Exception {
        Transaction transaction = new Transaction(1.0, Instant.now().toEpochMilli());
        doNothing().when(transactionRepository).insertOrUpdate(transaction);
        statisticsService.addTransaction(transaction);
        verify(transactionRepository).insertOrUpdate(transaction);
    }

    @Test(expected = InvalidParameterException.class)
    public void addTransactionNull() throws Exception {
        statisticsService.addTransaction(null);
    }


    @Test
    public void getUpdatedStatsOneTransaction() throws Exception {
        when(transactionRepository.getAll()).thenReturn(Lists.newArrayList(new Transaction(1.0, Instant.now().toEpochMilli())));
        when(statisticsRepository.get()).thenReturn(INIT_STATISTICS);
        Statistics expectedNewStats = new Statistics(1, 1, 1, 1, 1);

        when(statisticsRepository.update(any(Statistics.class))).thenReturn(expectedNewStats);

        statisticsService.getStatistics();

        verify(statisticsRepository).update(argThat(hasProperty("count",equalTo(1))));
    }

    @Test
    public void getStaticsMultipleTransactions() throws Exception {
        when(transactionRepository.getAll())
                .thenReturn(Lists.newArrayList(
                        new Transaction(1.0, Instant.now().toEpochMilli()),
                        new Transaction(5.0, Instant.now().toEpochMilli())));


        statisticsService.getStatistics();

        verify(statisticsRepository).update(argThat(hasProperty("sum",equalTo(6.0))));

    }

    @Test
    public void getStaticsSameAmountTransactions() throws Exception {
        when(transactionRepository.getAll())
                .thenReturn(Lists.newArrayList(
                        new Transaction(1.0, Instant.now().toEpochMilli()),
                        new Transaction(1.0, Instant.now().toEpochMilli())));

        statisticsService.getStatistics();

        verify(statisticsRepository).update(argThat(hasProperty("count",equalTo(2))));

    }

    @Test
    public void getStaticsSameTimestampTransactions() throws Exception {

        Long timestamp = Instant.now().toEpochMilli();
        when(transactionRepository.getAll())
                .thenReturn(Lists.newArrayList(
                        new Transaction(1.0, timestamp),
                        new Transaction(1.0, timestamp)));

        statisticsService.getStatistics();

        verify(statisticsRepository).update(argThat(hasProperty("count",equalTo(2))));

    }

    @Test
    public void getStaticsNoTransactions() throws Exception {
        when(transactionRepository.getAll())
                .thenReturn(new ArrayList<>());

        statisticsService.getStatistics();

        verify(statisticsRepository).update(argThat(hasProperty("count",equalTo(0))));

    }


    @Test
    public void getStaticsWithOldTransactions() throws Exception {
        when(transactionRepository.getAll())
                .thenReturn(new ArrayList<>());

        when(transactionRepository.getAll())
                .thenReturn(Lists.newArrayList(
                        new Transaction(1.0, Instant.now().toEpochMilli()),
                        new Transaction(1.0, Instant.now().minusMillis(WINDOW_IN_MILLIS +1).toEpochMilli())));
        statisticsService.getStatistics();
        verify(statisticsRepository).update(argThat(hasProperty("count",equalTo(1))));

    }


    @Test
    public void getStaticsAllOldTransactions() throws Exception {
        when(transactionRepository.getAll())
                .thenReturn(new ArrayList<>());

        when(transactionRepository.getAll())
                .thenReturn(Lists.newArrayList(
                        new Transaction(1.0, Instant.now().minusMillis(WINDOW_IN_MILLIS +1).toEpochMilli()),
                        new Transaction(1.0, Instant.now().minusMillis(WINDOW_IN_MILLIS +1).toEpochMilli())));
        statisticsService.getStatistics();
        verify(statisticsRepository).update(argThat(hasProperty("count",equalTo(0))));

    }
}
