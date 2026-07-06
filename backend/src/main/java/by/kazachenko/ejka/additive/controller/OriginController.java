package by.kazachenko.ejka.additive.controller;

import by.kazachenko.ejka.additive.dto.request.OriginRequest;
import by.kazachenko.ejka.additive.dto.response.OriginResponse;
import by.kazachenko.ejka.additive.service.OriginService;
import by.kazachenko.ejka.common.dto.response.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/origins")
@RequiredArgsConstructor
public class OriginController {

    private final OriginService originService;

    @GetMapping("/{id}")
    public ResponseEntity<OriginResponse> getOriginById(@PathVariable Long id) {
        OriginResponse originResponse = originService.getOriginById(id);

        return ResponseEntity.ok(originResponse);
    }

    @GetMapping
    public ResponseEntity<PageResponse<OriginResponse>> getAllOrigins(
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) Integer limit,
            @RequestParam(defaultValue = "type") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PageResponse<OriginResponse> originsResponse = originService.getAllOrigins(
                offset,
                limit,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(originsResponse);
    }

    @PostMapping
    public ResponseEntity<OriginResponse> createOrigin(@RequestBody @Valid OriginRequest request) {
        OriginResponse originResponse = originService.createOrigin(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(originResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrigin(@PathVariable Long id) {
        originService.deleteOriginById(id);

        return ResponseEntity.noContent().build();
    }

}
