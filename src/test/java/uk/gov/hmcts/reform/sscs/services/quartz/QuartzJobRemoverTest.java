package uk.gov.hmcts.reform.sscs.services.quartz;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobException;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobNotFoundException;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz.QuartzJobRemover;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QuartzJobRemoverTest {

    private final Scheduler scheduler = mock(Scheduler.class);
    private final QuartzJobRemover quartzJobRemover = new QuartzJobRemover(scheduler);

    @Test
    public void job_is_removed_from_scheduler() {

        assertThatCode(
            () -> {

                String jobId = "job-id";

                when(scheduler.deleteJob(JobKey.jobKey(jobId)))
                    .thenReturn(true);

                quartzJobRemover.remove(jobId);

                verify(scheduler, times(1)).deleteJob(
                    eq(JobKey.jobKey(jobId))
                );
            }
        ).doesNotThrowAnyException();
    }

    @Test
    public void job_throws_when_job_not_found() {

        assertThatThrownBy(
            () -> {

                String jobId = "missing-job-id";

                when(scheduler.deleteJob(JobKey.jobKey(jobId)))
                    .thenReturn(false);

                quartzJobRemover.remove(jobId);
            }

        ).hasMessage("missing-job-id")
            .isExactlyInstanceOf(JobNotFoundException.class);
    }

    @Test
    public void job_throws_when_quartz_fails() {

        assertThatThrownBy(
            () -> {

                String jobId = "failing-job-id";

                doThrow(SchedulerException.class)
                    .when(scheduler)
                    .deleteJob(JobKey.jobKey(jobId));

                quartzJobRemover.remove(jobId);
            }
        ).hasMessage("Error while removing job. Job ID: failing-job-id")
            .isExactlyInstanceOf(JobException.class);
    }

}
