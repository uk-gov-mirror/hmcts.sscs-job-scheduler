package uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.JobDataKeys;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobExecutor;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobPayloadDeserializer;

import java.time.Instant;

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

        log.info("Executing job {}", jobId);

        try {

            Instant jobStart = Instant.now();

            String payloadSource =
                jobDetail
                    .getJobDataMap()
                    .getString(JobDataKeys.PAYLOAD);

            T payload = jobPayloadDeserializer.deserialize(payloadSource);

            jobExecutor.execute(jobId, payload);

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
