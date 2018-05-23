package uk.gov.hmcts.reform.sscs.jobscheduler.services;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

/**
 * SonarQube reports as error. Max allowed - 5 parents
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class JobNotFoundException extends UnknownErrorCodeException {

    public JobNotFoundException(String message) {
        super(AlertLevel.P4, message);
    }
}
