package uk.gov.hmcts.reform.sscs.jobscheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.Job;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobExecutor;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobPayloadDeserializer;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobPayloadSerializer;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobRemover;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobScheduler;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobService;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootContextRoot.class)
@ActiveProfiles("development")
public class ApplicationTest {

    @Autowired
    @Qualifier("scheduler")
    private Scheduler quartzScheduler;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobScheduler<TestPayload> jobScheduler;

    @Autowired
    private JobRemover jobRemover;

    @MockBean
    private JobPayloadSerializer<TestPayload> jobPayloadSerializer;

    @MockBean
    private JobPayloadDeserializer<TestPayload> jobPayloadDeserializer;

    @MockBean
    private JobExecutor<TestPayload> jobExecutor;

    TestPayload testPayload = new TestPayload();

    @Before
    public void setUp() {

        jobService.start();

        try {
            quartzScheduler.clear();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        given(jobPayloadSerializer.serialize(testPayload)).willReturn("serialized-payload");
        given(jobPayloadDeserializer.deserialize("serialized-payload")).willReturn(testPayload);
    }

    @Test
    public void jobIsScheduledAndExecutesInTheFuture() {

        assertTrue("Job scheduler is empty at start", getScheduledJobCount() == 0);

        Job<TestPayload> job = new Job<>(
            "test-job",
            testPayload,
            ZonedDateTime.now().plusSeconds(2)
        );

        String jobId = jobScheduler.create(job);

        assertNotNull(jobId);

        assertTrue("Job was scheduled into Quartz", getScheduledJobCount() == 1);

        // job is executed
        verify(jobExecutor, timeout(10000)).execute(
            eq(jobId),
            eq(testPayload)
        );

        assertTrue("Job was removed from Quartz after execution", getScheduledJobCount() == 0);
    }

    @Test
    public void jobIsScheduledAndThenRemoved() {

        assertTrue("Job scheduler is empty at start", getScheduledJobCount() == 0);

        Job<TestPayload> job = new Job<>(
            "test-job",
            testPayload,
            ZonedDateTime.now().plusSeconds(2)
        );

        String jobId = jobScheduler.create(job);

        assertNotNull(jobId);

        assertTrue("Job was scheduled into Quartz", getScheduledJobCount() == 1);

        jobRemover.remove(jobId);

        assertTrue("Job was removed from Quartz after execution", getScheduledJobCount() == 0);

        // job is /never/ executed
        verify(jobExecutor, after(10000).never()).execute(
            eq(jobId),
            eq(testPayload)
        );
    }

    public int getScheduledJobCount() {

        try {

            return quartzScheduler
                .getJobKeys(GroupMatcher.anyGroup())
                .size();

        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    private class TestPayload {

        public String getFoo() {
            return "bar";
        }
    }

}
