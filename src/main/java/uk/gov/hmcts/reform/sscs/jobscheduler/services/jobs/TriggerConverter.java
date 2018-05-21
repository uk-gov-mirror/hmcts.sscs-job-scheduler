package uk.gov.hmcts.reform.sscs.jobscheduler.services.jobs;

import static org.quartz.TriggerBuilder.newTrigger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import uk.gov.hmcts.reform.sscs.jobscheduler.model.Trigger;

final class TriggerConverter {

    private TriggerConverter() {
        // static class constructor
    }

    static org.quartz.Trigger toQuartzTrigger(final Trigger trigger) {
        return newTrigger()
            .startAt(Date.from(trigger.startDateTime.toInstant()))
            .usingJobData(JobDataKeys.ATTEMPT, 1)
            .build();
    }

    static Trigger toPlatformTrigger(org.quartz.Trigger trigger) {
        return new Trigger(
            ZonedDateTime.ofInstant(trigger.getStartTime().toInstant(), ZoneId.systemDefault())
        );
    }
}
