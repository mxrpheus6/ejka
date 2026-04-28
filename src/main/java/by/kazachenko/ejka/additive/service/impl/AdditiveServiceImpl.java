package by.kazachenko.ejka.additive.service.impl;

import by.kazachenko.ejka.additive.dto.request.AdditiveRequest;
import by.kazachenko.ejka.additive.dto.response.AdditiveResponse;
import by.kazachenko.ejka.additive.dto.response.WorkerAdditiveResponse;
import by.kazachenko.ejka.additive.mapper.AdditiveMapper;
import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.additive.model.Origin;
import by.kazachenko.ejka.additive.model.enums.DangerLevel;
import by.kazachenko.ejka.additive.repository.AdditiveRepository;
import by.kazachenko.ejka.additive.service.AdditiveService;
import by.kazachenko.ejka.additive.service.OriginService;
import by.kazachenko.ejka.additive.specification.AdditiveSpecifications;
import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.common.exception.cutom.AdditiveAlreadyExistsException;
import by.kazachenko.ejka.common.exception.cutom.AdditiveNotFoundException;
import by.kazachenko.ejka.common.mapper.PageResponseMapper;

import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdditiveServiceImpl implements AdditiveService {

    private final AdditiveRepository additiveRepository;
    private final AdditiveMapper additiveMapper;

    private final OriginService originService;

    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public AdditiveResponse getAdditiveById(Long id) {
        Additive additive = additiveRepository.findById(id)
                .orElseThrow(() -> new AdditiveNotFoundException("Добавка не найдена"));

        return additiveMapper.toResponse(additive);
    }

    @Override
    @Transactional(readOnly = true)
    public AdditiveResponse getAdditiveByCode(String code) {
        Additive additive = additiveRepository.findByCode(code)
                .orElseThrow(() -> new AdditiveNotFoundException("Добавка не найдена"));

        return additiveMapper.toResponse(additive);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdditiveResponse> getAllAdditives(
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(offset, limit, Sort.by(direction, sortBy));

        Page<AdditiveResponse> responsePage = additiveRepository
                .findAll(pageable)
                .map(additiveMapper::toResponse);

        return pageResponseMapper.toResponse(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerAdditiveResponse> getAllWorkerAdditives() {
        return additiveRepository
                .findAll()
                .stream()
                .map(additiveMapper::toWorkerResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdditiveResponse> getFilteredAdditives(
            String category,
            DangerLevel dangerLevel,
            List<String> originTypes,
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection)
    {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(offset, limit, Sort.by(direction, sortBy));

        Specification<Additive> spec = Specification.where(AdditiveSpecifications.hasCategory(category))
                .and(AdditiveSpecifications.hasDangerLevel(dangerLevel))
                .and(AdditiveSpecifications.hasOrigins(originTypes));

        Page<AdditiveResponse> responsePage = additiveRepository
                .findAll(spec, pageable)
                .map(additiveMapper::toResponse);


        return pageResponseMapper.toResponse(responsePage);
    }

    @Override
    @Transactional
    public AdditiveResponse createAdditive(AdditiveRequest request) {
        if (additiveRepository.existsByCode(request.code())) {
            throw new AdditiveAlreadyExistsException("Добавка уже существует");
        }

        Additive additive = additiveMapper.toEntity(request);

        Set<Origin> origins = originService.getOriginsByIds(request.originIds());
        additive.setOrigins(origins);

        additiveRepository.save(additive);

        return additiveMapper.toResponse(additive);
    }

    @Override
    @Transactional
    public AdditiveResponse updateAdditive(Long id, AdditiveRequest request) {
        Additive additive = additiveRepository.findById(id)
                .orElseThrow(() -> new AdditiveNotFoundException("Добавка не найдена"));

        if (!additive.getCode().equals(request.code()) &&
                additiveRepository.existsByCode(request.code())) {
            throw new AdditiveAlreadyExistsException("Добавка с кодом " + request.code() + " уже существует");
        }

        additiveMapper.updateEntityFromRequest(request, additive);

        Set<Origin> origins = originService.getOriginsByIds(request.originIds());
        additive.setOrigins(origins);

        return additiveMapper.toResponse(additive);
    }

    @Override
    @Transactional
    public void deleteAdditiveById(Long id) {
        Additive additive = additiveRepository.findById(id)
                .orElseThrow(() -> new AdditiveNotFoundException("Добавка не найдена"));

        additiveRepository.delete(additive);
    }

}
