package com.eatool.backend.applications;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for Applications, ported from the frontend's former
 * localStorage-only rules in src/catalog.js
 * (createApplication/updateApplication/deleteApplication). TIME
 * Classification and Business Fit Band are always recomputed server-side
 * from Business Fit and Tech Fit (see ApplicationNormalizer).
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<Application> list() {
        return applicationService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Application create(@RequestBody ApplicationRequest request) {
        return applicationService.create(request);
    }

    @PutMapping("/{id}")
    public Application update(@PathVariable UUID id, @RequestBody ApplicationRequest request) {
        return applicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        applicationService.delete(id);
    }
}
