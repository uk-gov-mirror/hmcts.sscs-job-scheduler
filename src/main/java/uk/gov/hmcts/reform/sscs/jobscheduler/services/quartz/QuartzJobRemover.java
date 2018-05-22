package uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobException;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobNotFoundException;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobRemover;

@Service
public class QuartzJobRemover implements JobRemover {

    private final Scheduler scheduler;

    public QuartzJobRemover(
        Scheduler scheduler
    ) {
        this.scheduler = scheduler;
    }

    public void remove(String jobId) {
        try {

            boolean jobFound = scheduler.deleteJob(JobKey.jobKey(jobId));
            if (!jobFound) {
                throw new JobNotFoundException(jobId);
            }

        } catch (SchedulerException e) {
            throw new JobException(
                "Error while deleting job. ID: " + jobId,
                e
            );
        }
    }

}
