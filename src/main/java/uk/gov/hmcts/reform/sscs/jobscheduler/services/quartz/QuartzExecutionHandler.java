package uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz;

import java.time.Instant;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.JobDataKeys;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobExecutor;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobPayloadDeserializer;

@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class QuartzExecutionHandler<T> implements Job {

    private static final Logger log = LoggerFactory.getLogger(QuartzExecutionHandler.class);

    private final JobPayloadDeserializer<T> jobPayloadDeserializer;
    private final JobExecutor<T> jobExecutor;

    public QuartzExecutionHandler(
        JobPayloadDeserializer<T> jobPayloadDeserializer,
        JobExecutor<T> jobExecutor
    ) {
        this.jobPayloadDeserializer = jobPayloadDeserializer;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDetail jobDetail = context.getJobDetail();
        String jobId = jobDetail.getKey().getName();
        String jobGroup = jobDetail.getKey().getGroup();
        String jobName = jobDetail.getDescription();

        log.info("Executing job {}", jobId);

        try {

            Instant jobStart = Instant.now();

            String payloadSource = "";

            if (jobDetail.getJobDataMap().containsKey(JobDataKeys.PAYLOAD)) {

                payloadSource =
                    jobDetail
                        .getJobDataMap()
                        .getString(JobDataKeys.PAYLOAD);
            }

            T payload = jobPayloadDeserializer.deserialize(payloadSource);

            jobExecutor.execute(jobId, jobGroup, jobName, payload);

            log.info(
                "Job {} executed in {}ms.",
                jobId, (Instant.now().toEpochMilli() - jobStart.toEpochMilli())
            );

        } catch (Exception e) {

            String errorMessage = String.format("Job failed. Job ID: %s", jobId);
            log.error(errorMessage, e);

            throw new JobExecutionException(errorMessage, e);
        }
    }

}
