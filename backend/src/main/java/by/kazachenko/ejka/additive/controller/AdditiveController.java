package by.kazachenko.ejka.additive.controller;

import by.kazachenko.ejka.additive.dto.request.AdditiveRequest;
import by.kazachenko.ejka.additive.dto.response.AdditiveResponse;
import by.kazachenko.ejka.additive.dto.response.WorkerAdditiveResponse;
import by.kazachenko.ejka.additive.model.enums.DangerLevel;
import by.kazachenko.ejka.additive.service.AdditiveService;
import by.kazachenko.ejka.common.dto.response.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/additives")
@RequiredArgsConstructor
public class AdditiveController {

    private final AdditiveService additiveService;

    @GetMapping("/{id}")
    public ResponseEntity<AdditiveResponse> getAdditiveById(@PathVariable Long id) {
        AdditiveResponse additiveResponse = additiveService.getAdditiveById(id);

        return ResponseEntity.ok(additiveResponse);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<AdditiveResponse> getAdditiveByCode(@PathVariable String code) {
        AdditiveResponse additiveResponse = additiveService.getAdditiveByCode(code);

        return ResponseEntity.ok(additiveResponse);
    }

    @GetMapping("/batch")
    public ResponseEntity<List<AdditiveResponse>> getAdditivesBatch(@RequestParam List<Long> ids) {
        List<AdditiveResponse> responses = additiveService.getAdditivesByIds(ids);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdditiveResponse>> getAllAdditives(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) DangerLevel dangerLevel,
            @RequestParam(required = false) List<String> origin,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) Integer limit,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponse<AdditiveResponse> additivesResponse = additiveService.getFilteredAdditives(
                searchQuery,
                category,
                dangerLevel,
                origin,
                offset,
                limit,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(additivesResponse);
    }

    @GetMapping("/worker-view")
    public ResponseEntity<List<WorkerAdditiveResponse>> getAllWorkerAdditives() {
        List<WorkerAdditiveResponse> workerAdditiveResponses = additiveService.getAllWorkerAdditives();

        return ResponseEntity.ok(workerAdditiveResponses);
    }

    @PostMapping
    public ResponseEntity<AdditiveResponse> createAdditive(@RequestBody @Valid AdditiveRequest request) {
        AdditiveResponse additiveResponse = additiveService.createAdditive(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(additiveResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdditiveResponse> updateAdditive(
            @PathVariable Long id,
            @RequestBody @Valid AdditiveRequest request
    ) {
        AdditiveResponse additiveResponse = additiveService.updateAdditive(id, request);

        return ResponseEntity.ok(additiveResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdditive(@PathVariable Long id) {
        additiveService.deleteAdditiveById(id);

        return ResponseEntity.noContent().build();
    }

}
