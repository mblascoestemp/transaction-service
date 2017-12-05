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

import static java.lang.Math.floor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class StatisticsServiceUtilsTest {


    private final Statistics INIT_STATISTICS = Statistics.baseStatistics;

    private final Statistics SAMPLE_STATISTICS = new Statistics(1, 0, 100, 50, 100);

    private final Transaction SAMPLE_TRANSACTION = new Transaction(100.0, Instant.now().toEpochMilli());

    private StatisticsServiceUtils utils = new StatisticsServiceUtils();

    
    

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(utils, "precision", 6000);
        ReflectionTestUtils.setField(utils, "windowSize", 6000);
    }

    @Test
    public void recalculateStatsBaseStats() throws Exception {
        Statistics stats = utils.recalculateStats(SAMPLE_TRANSACTION, INIT_STATISTICS);

        assertThat(stats.getCount(), equalTo(1));
        assertThat(stats.getSum(), equalTo(100.0));
        assertThat(stats.getAvg(), equalTo(100.0));
        assertThat(stats.getMax(), equalTo(100.0));
        assertThat(stats.getMin(), equalTo(100.0));
    }

    @Test
    public void recalculateStatsStatsUpdatedByTransaction() throws Exception {


        Statistics stats = utils.recalculateStats(
                SAMPLE_TRANSACTION,
                new Statistics(1, 100, 100, 100, 100));

        assertThat(stats.getCount(), equalTo(2));
        assertThat(stats.getSum(), equalTo(200.0));
        assertThat(stats.getAvg(), equalTo(100.0));
        assertThat(stats.getMax(), equalTo(100.0));
        assertThat(stats.getMin(), equalTo(100.0));
    }

    @Test
    public void recalculateStatsStatsUpdatedByZeroAmountTransaction() throws Exception {


        Statistics stats = utils.recalculateStats(
                new Transaction(0.0,1),
                Statistics.baseStatistics);

        assertThat(stats.getCount(), equalTo(1));
        assertThat(stats.getSum(), equalTo(0.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void recalculateStatsNullTransaction() throws Exception {
        utils.recalculateStats(null, INIT_STATISTICS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void recalculateStatsNullStatistics() throws Exception {
        utils.recalculateStats(SAMPLE_TRANSACTION, null);
    }


    @Test
    public void recalculateStatsTooOldStatsResetTransaction() throws Exception {
        ReflectionTestUtils.setField(utils, "windowSize", 0);
        //With zero time window this should be enough time diff
        Transaction transaction = new Transaction(100, Instant.now().toEpochMilli());
        utils.recalculateStats(transaction, INIT_STATISTICS);
    }

    @Test
    public void mergeStatsStatsMerged() throws Exception {

        Statistics stats = utils.mergeStats(
                new Statistics(2, 50, 200, 125, 250),
                new Statistics(1, 100, 100, 100, 100));

        assertThat(stats.getCount(), equalTo(3));
        assertThat(stats.getSum(), equalTo(350.0));
        assertThat(stats.getAvg(), equalTo(116.66666666666667));
        assertThat(stats.getMax(), equalTo(200.0));
        assertThat(stats.getMin(), equalTo(50.0));

    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeStatsStatsNullFirst() throws Exception {
        utils.mergeStats(null, INIT_STATISTICS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeStatsStatsNullSecond() throws Exception {
        utils.mergeStats(INIT_STATISTICS, null);
    }

    @Test
    public void mergeStatsStatsBothInitStats() throws Exception {
        Statistics stats = utils.mergeStats(INIT_STATISTICS, INIT_STATISTICS);
        assertThat(stats.getCount(), equalTo(0));
        assertThat(stats.getSum(), equalTo(0.0));
        assertThat(stats.getAvg(), equalTo(0.0));
        assertThat(stats.getMax(), equalTo(0.0));
        assertThat(stats.getMin(), equalTo(0.0));
    }


    @Test
    public void isStatisticsInCurrentWindowTimeAccept() throws Exception {
        assertThat(utils.isStatisticsInCurrentWindowTime(Statistics.baseStatistics,Instant.now().toEpochMilli()),equalTo(true));
    }

    @Test
    public void isStatisticsInCurrentWindowTimeReject() throws Exception {
        ReflectionTestUtils.setField(utils, "windowSize", 0);
        assertThat(utils.isStatisticsInCurrentWindowTime(Statistics.baseStatistics,Instant.now().toEpochMilli()),equalTo(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isStatisticsInCurrentWindowTimeFutureReceptionTime() throws Exception {
        utils.isStatisticsInCurrentWindowTime(Statistics.baseStatistics,Instant.now().plusMillis(10000).toEpochMilli());
    }

    @Test(expected = IllegalArgumentException.class)
    public void isStatisticsInCurrentNullStatistics() throws Exception {
        utils.isStatisticsInCurrentWindowTime(null,Instant.now().toEpochMilli());
    }

    @Test
    public void calculateBucketPositionSameBucketSameWindow() throws Exception {
        ReflectionTestUtils.setField(utils, "precision", 2);
        ReflectionTestUtils.setField(utils, "windowSize", 4);
         assertThat(utils.calculateBucketPosition(4),equalTo( utils.calculateBucketPosition(5)));

    }

    @Test
    public void calculateBucketPositionDifferentBucketSameWindow() throws Exception {
        ReflectionTestUtils.setField(utils, "precision", 2);
        ReflectionTestUtils.setField(utils, "windowSize", 4);
        assertThat(utils.calculateBucketPosition(5),not(equalTo( utils.calculateBucketPosition(6))));
    }

    @Test
    public void calculateBucketPositionSameBucketDifferentWindow() throws Exception {
        ReflectionTestUtils.setField(utils, "precision", 2);
        ReflectionTestUtils.setField(utils, "windowSize", 4);
        assertThat(utils.calculateBucketPosition(4),equalTo( utils.calculateBucketPosition(8)));
    }


    @Test(expected = IllegalArgumentException.class)
    public void calculateBucketInvalidIndexCalculated() throws Exception {
        utils.calculateBucketPosition(-5);

    }
}
