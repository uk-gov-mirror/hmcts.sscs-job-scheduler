package uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.Job;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.JobDataKeys;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobException;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobPayloadSerializer;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobScheduler;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class QuartzJobScheduler<T> implements JobScheduler<T> {

    private final Scheduler scheduler;
    private final JobPayloadSerializer<T> jobPayloadSerializer;

    public QuartzJobScheduler(
        Scheduler scheduler,
        JobPayloadSerializer<T> jobPayloadSerializer
    ) {
        this.scheduler = scheduler;
        this.jobPayloadSerializer = jobPayloadSerializer;
    }

    public String create(Job<T> job) {
        try {

            String jobId = UUID.randomUUID().toString();

            scheduler.scheduleJob(
                newJob(QuartzExecutionHandler.class)
                    .withIdentity(jobId)
                    .withDescription(job.name)
                    .usingJobData(
                        JobDataKeys.PAYLOAD,
                        jobPayloadSerializer.serialize(job.payload)
                    )
                    .requestRecovery()
                    .build(),
                toQuartzTrigger(job.triggerAt)
            );

            return jobId;

        } catch (SchedulerException exc) {
            throw new JobException("Error while scheduling job", exc);
        }
    }

    private static org.quartz.Trigger toQuartzTrigger(ZonedDateTime triggerDateTime) {
        return newTrigger()
            .startAt(Date.from(triggerDateTime.toInstant()))
            .usingJobData(JobDataKeys.ATTEMPT, 1)
            .build();
    }
}
