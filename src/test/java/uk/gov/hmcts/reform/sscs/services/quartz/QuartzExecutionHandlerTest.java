package uk.gov.hmcts.reform.sscs.services.quartz;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.JobDataKeys;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobExecutor;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.JobPayloadDeserializer;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz.QuartzExecutionHandler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class QuartzExecutionHandlerTest {

    private final JobPayloadDeserializer jobPayloadDeserializer = mock(JobPayloadDeserializer.class);
    private final JobExecutor jobExecutor = mock(JobExecutor.class);
    private final QuartzExecutionHandler quartzExecutionHandler =
        new QuartzExecutionHandler(
            jobPayloadDeserializer,
            jobExecutor
        );

    @Test
    public void execute_deserializes_payload_and_delegates_execution() {

        assertThatCode(
            () -> {

                JobExecutionContext context = mock(JobExecutionContext.class);
                JobDetail jobDetail = mock(JobDetail.class);
                JobDataMap jobDataMap = mock(JobDataMap.class);

                when(context.getJobDetail()).thenReturn(jobDetail);
                when(jobDetail.getKey()).thenReturn(new JobKey("job-id"));
                when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);

                when(jobDataMap.getString(JobDataKeys.PAYLOAD)).thenReturn("payload-stuff");
                when(jobPayloadDeserializer.deserialize("payload-stuff")).thenReturn("deserialized-payload-stuff");

                quartzExecutionHandler.execute(context);

                verify(jobExecutor, times(1)).execute(
                    eq("job-id"),
                    eq("deserialized-payload-stuff")
                );
            }
        ).doesNotThrowAnyException();
    }

}
