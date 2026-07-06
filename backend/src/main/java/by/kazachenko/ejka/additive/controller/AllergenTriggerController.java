package by.kazachenko.ejka.additive.controller;

import by.kazachenko.ejka.additive.dto.response.AllergenTriggerResponse;
import by.kazachenko.ejka.additive.service.AllergenTriggerService;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/allergen-triggers")
@RequiredArgsConstructor
public class AllergenTriggerController {

    private final AllergenTriggerService allergensTriggerService;

    @GetMapping
    public ResponseEntity<List<AllergenTriggerResponse>> getAllAllergenTriggers() {
        return ResponseEntity.ok(allergensTriggerService.getAllAllergenTriggers());
    }

}
