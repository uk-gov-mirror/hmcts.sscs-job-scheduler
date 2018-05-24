package uk.gov.hmcts.reform.sscs.jobscheduler.services;

public interface JobExecutor<T> {

    void execute(String jobId, String jobName, T payload);
}
