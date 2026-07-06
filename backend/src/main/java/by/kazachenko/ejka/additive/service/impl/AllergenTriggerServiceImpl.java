package by.kazachenko.ejka.additive.service.impl;

import by.kazachenko.ejka.additive.dto.response.AllergenTriggerResponse;
import by.kazachenko.ejka.additive.mapper.AllergenTriggerMapper;
import by.kazachenko.ejka.additive.repository.AllergenTriggerRepository;
import by.kazachenko.ejka.additive.service.AllergenTriggerService;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllergenTriggerServiceImpl implements AllergenTriggerService {

    private final AllergenTriggerRepository allergenTriggerRepository;
    private final AllergenTriggerMapper allergenTriggerMapper;

    @Override
    public List<AllergenTriggerResponse> getAllAllergenTriggers() {
        return allergenTriggerRepository
                .findAll()
                .stream()
                .map(allergenTriggerMapper::toResponse)
                .toList();
    }
}
