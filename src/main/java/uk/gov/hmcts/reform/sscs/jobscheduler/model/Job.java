package uk.gov.hmcts.reform.sscs.jobscheduler.model;

import java.time.ZonedDateTime;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;

public class Job<T> {

    @NotBlank
    public final String group;

    @NotBlank
    public final String name;

    @NotNull
    public final T payload;

    @NotNull
    public final ZonedDateTime triggerAt;

    public Job(
        String group,
        String name,
        T payload,
        ZonedDateTime triggerAt
    ) {
        this.group = group;
        this.name = name;
        this.payload = payload;
        this.triggerAt = triggerAt;
    }

}
