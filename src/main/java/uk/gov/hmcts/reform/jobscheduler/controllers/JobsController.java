package uk.gov.hmcts.reform.jobscheduler.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jobscheduler.model.Job;
import uk.gov.hmcts.reform.jobscheduler.services.JobsService;
import uk.gov.hmcts.reform.jobscheduler.services.auth.AuthService;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/jobs")
public class JobsController {

    private final JobsService jobsService;
    private final AuthService authService;

    public JobsController(JobsService jobsService, AuthService authService) {
        this.jobsService = jobsService;
        this.authService = authService;
    }

    @PostMapping(path = "")
    @ApiOperation("Create a new job")
    public ResponseEntity<Void> create(
        @RequestBody Job job,
        @RequestHeader("ServiceAuthorization") String serviceAuthHeader
    ) {
        String serviceName = authService.authenticate(serviceAuthHeader);
        String id = this.jobsService.create(job, serviceName);

        URI newJobUri = fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();

        return created(newJobUri).build();
    }
}