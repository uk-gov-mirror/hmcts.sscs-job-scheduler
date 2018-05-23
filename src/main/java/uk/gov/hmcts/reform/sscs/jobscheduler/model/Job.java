package uk.gov.hmcts.reform.sscs.jobscheduler.model;

import org.hibernate.validator.constraints.NotBlank;

import java.time.ZonedDateTime;
import javax.validation.constraints.NotNull;

public class Job<T> {

    @NotBlank
    public final String name;

    @NotNull
    public final T payload;

    @NotNull
    public final ZonedDateTime triggerAt;

    public Job(
        String name,
        T payload,
        ZonedDateTime triggerAt
    ) {
        this.name = name;
        this.payload = payload;
        this.triggerAt = triggerAt;
    }

}
