package uk.gov.hmcts.reform.sscs.services.quartz;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.time.ZonedDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.Job;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.JobDataKeys;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobException;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobPayloadSerializer;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz.QuartzJobScheduler;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class QuartzJobSchedulerTest {

    private final JobPayloadSerializer jobPayloadSerializer = mock(JobPayloadSerializer.class);
    private final Scheduler scheduler = mock(Scheduler.class);
    private final QuartzJobScheduler<String> quartzJobScheduler = new QuartzJobScheduler<>(
        scheduler,
        jobPayloadSerializer
    );

    @Test
    public void job_is_scheduled() {

        assertThatCode(
            () -> {

                String jobName = "test-job";
                String jobPayload = "payload";
                ZonedDateTime triggerAt = ZonedDateTime.now();

                Job<String> job = new Job<>(
                    jobName,
                    jobPayload,
                    triggerAt
                );

                when(jobPayloadSerializer.serialize(jobPayload))
                    .thenReturn("serialized-payload");

                String actualJobId = quartzJobScheduler.schedule(job);

                ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
                ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);

                verify(scheduler, times(1)).scheduleJob(
                    jobDetailCaptor.capture(),
                    triggerCaptor.capture()
                );

                JobDetail actualJobDetail = jobDetailCaptor.getValue();
                assertEquals(actualJobId, actualJobDetail.getKey().getName());
                assertEquals(jobName, actualJobDetail.getDescription());
                assertTrue(jobName, actualJobDetail.getJobDataMap().containsKey(JobDataKeys.PAYLOAD));
                assertEquals("serialized-payload", actualJobDetail.getJobDataMap().get(JobDataKeys.PAYLOAD));

                Trigger actualTrigger = triggerCaptor.getValue();
                assertEquals(actualTrigger.getStartTime().toInstant(), triggerAt.toInstant());
            }
        ).doesNotThrowAnyException();
    }

    @Test
    public void schedule_wraps_exception_from_client_deserializer() {

        assertThatThrownBy(
            () -> {

                String jobName = "test-job";
                String jobPayload = "payload";
                ZonedDateTime triggerAt = ZonedDateTime.now();

                Job<String> job = new Job<>(
                    jobName,
                    jobPayload,
                    triggerAt
                );

                doThrow(RuntimeException.class)
                    .when(jobPayloadSerializer)
                    .serialize(jobPayload);

                quartzJobScheduler.schedule(job);
            }
        ).hasMessage("Error while scheduling job")
            .isExactlyInstanceOf(JobException.class);
    }

    @Test
    public void schedule_throws_when_quartz_fails() {

        assertThatThrownBy(
            () -> {

                String jobName = "test-job";
                String jobPayload = "payload";
                ZonedDateTime triggerAt = ZonedDateTime.now();

                Job<String> job = new Job<>(
                    jobName,
                    jobPayload,
                    triggerAt
                );

                when(jobPayloadSerializer.serialize(jobPayload))
                    .thenReturn("serialized-payload");

                doThrow(RuntimeException.class)
                    .when(scheduler)
                    .scheduleJob(any(), any());

                quartzJobScheduler.schedule(job);
            }
        ).hasMessage("Error while scheduling job")
            .isExactlyInstanceOf(JobException.class);
    }

}
