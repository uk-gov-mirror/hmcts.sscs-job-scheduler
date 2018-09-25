package uk.gov.hmcts.reform.sscs.jobscheduler;

import static java.util.Arrays.asList;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.sscs.jobscheduler.config.QuartzConfiguration;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz.JobClassMapper;
import uk.gov.hmcts.reform.sscs.jobscheduler.services.quartz.JobClassMapping;

@SpringBootApplication
@ComponentScan(basePackageClasses = SpringBootContextRoot.class, lazyInit = true)
@Import(QuartzConfiguration.class)
public class SpringBootContextRoot {
    // used as the root configuration for the test spring boot context
}
