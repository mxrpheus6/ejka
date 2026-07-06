package by.kazachenko.ejka.additive.service.impl;

import by.kazachenko.ejka.additive.dto.request.AdditiveRequest;
import by.kazachenko.ejka.additive.dto.response.AdditiveResponse;
import by.kazachenko.ejka.additive.dto.response.WorkerAdditiveResponse;
import by.kazachenko.ejka.additive.mapper.AdditiveMapper;
import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.additive.model.Origin;
import by.kazachenko.ejka.additive.model.enums.DangerLevel;
import by.kazachenko.ejka.additive.rabbitmq.ActionType;
import by.kazachenko.ejka.additive.rabbitmq.AdditivePublisher;
import by.kazachenko.ejka.additive.rabbitmq.AdditiveUpdateEvent;
import by.kazachenko.ejka.additive.repository.AdditiveRepository;
import by.kazachenko.ejka.additive.service.AdditiveService;
import by.kazachenko.ejka.additive.service.OriginService;
import by.kazachenko.ejka.additive.specification.AdditiveSpecifications;
import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.common.exception.custom.AdditiveAlreadyExistsException;
import by.kazachenko.ejka.common.exception.custom.AdditiveNotFoundException;
import by.kazachenko.ejka.common.mapper.PageResponseMapper;

import by.kazachenko.ejka.common.security.SecurityUtils;
import by.kazachenko.ejka.product.repository.ProductRepository;
import by.kazachenko.ejka.user.model.enums.Role;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdditiveServiceImpl implements AdditiveService {

    private final AdditiveRepository additiveRepository;
    private final AdditiveMapper additiveMapper;

    private final ProductRepository productRepository;

    private final OriginService originService;

    private final PageResponseMapper pageResponseMapper;
    private final SecurityUtils securityUtils;

    private final AdditivePublisher additivePublisher;


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "additivesById", key = "#id")
    public AdditiveResponse getAdditiveById(Long id) {
        Additive additive = additiveRepository.findById(id)
                .orElseThrow(() -> new AdditiveNotFoundException("Добавка не найдена"));

        return additiveMapper.toResponse(additive);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "additivesByCode", key = "#code")
    public AdditiveResponse getAdditiveByCode(String code) {
        Additive additive = additiveRepository.findByCode(code)
                .orElseThrow(() -> new AdditiveNotFoundException("Добавка не найдена"));

        return additiveMapper.toResponse(additive);
    }

    @Override
    public List<AdditiveResponse> getAdditivesByIds(List<Long> ids) {
        return additiveRepository
                .findAllById(ids)
                .stream()
                .map(additiveMapper::toResponse)
                .toList();
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
            String searchQuery,
            String category,
            DangerLevel dangerLevel,
            List<String> originTypes,
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection)
    {

        Sort sort;

        if (searchQuery != null && !searchQuery.isBlank() && "code".equalsIgnoreCase(sortBy)) {
            sort = Sort.unsorted();
        } else {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;

            if ("code".equalsIgnoreCase(sortBy)) {
                sort = Sort.by(direction, "virtualNumericCode").and(Sort.by(direction, "code"));
            } else {
                sort = Sort.by(direction, sortBy);
            }
        }

        Pageable pageable = PageRequest.of(offset, limit, sort);

        Specification<Additive> spec = Specification.where(AdditiveSpecifications.hasCategory(category))
                .and(AdditiveSpecifications.textSimilarTo(searchQuery, 0.25))
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
        Role userRole = securityUtils.getLoggedUserRole();
        if (userRole != Role.ROLE_MODERATOR) {
            throw new AccessDeniedException("Добавлять добавки может только модератор.");
        }

        String normalizedCode = request.code()
                .toUpperCase()
                .replaceAll("\\s+", "")
                .replace('Е', 'E');

        if (additiveRepository.existsByCode(normalizedCode)) {
            throw new AdditiveAlreadyExistsException("Добавка уже существует");
        }

        Additive additive = additiveMapper.toEntity(request);
        additive.setCode(normalizedCode);

        Set<Origin> origins = originService.getOriginsByIds(request.originIds());
        additive.setOrigins(origins);

        additiveRepository.save(additive);

        additivePublisher.sendAdditiveUpdate(
                new AdditiveUpdateEvent(additive.getId(), additive.getCode(), ActionType.CREATED)
        );

        return additiveMapper.toResponse(additive);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "additivesById", key = "#id"),
            @CacheEvict(value = "additivesByCode", allEntries = true)
    })
    public AdditiveResponse updateAdditive(Long id, AdditiveRequest request) {
        Role userRole = securityUtils.getLoggedUserRole();
        if (userRole != Role.ROLE_MODERATOR) {
            throw new AccessDeniedException("Изменять добавки может только модератор.");
        }

        Additive additive = additiveRepository.findById(id)
                .orElseThrow(() -> new AdditiveNotFoundException("Добавка не найдена"));

        String normalizedCode = request.code()
                .toUpperCase()
                .replaceAll("\\s+", "")
                .replace('Е', 'E');

        if (!additive.getCode().equals(normalizedCode) &&
                additiveRepository.existsByCode(normalizedCode)) {
            throw new AdditiveAlreadyExistsException("Добавка с кодом " + normalizedCode + " уже существует");
        }

        additiveMapper.updateEntityFromRequest(request, additive);

        additive.setCode(normalizedCode);

        Set<Origin> origins = originService.getOriginsByIds(request.originIds());
        additive.setOrigins(origins);

        additiveRepository.save(additive);

        additivePublisher.sendAdditiveUpdate(
                new AdditiveUpdateEvent(additive.getId(), additive.getCode(), ActionType.UPDATED)
        );

        return additiveMapper.toResponse(additive);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "additivesById", key = "#id"),
            @CacheEvict(value = "additivesByCode", allEntries = true)
    })
    public void deleteAdditiveById(Long id) {
        Additive additive = additiveRepository.findById(id)
                .orElseThrow(() -> new AdditiveNotFoundException("Добавка не найдена"));

        productRepository.deleteAdditiveLinks(id);
        additiveRepository.delete(additive);

        additivePublisher.sendAdditiveUpdate(
                new AdditiveUpdateEvent(additive.getId(), additive.getCode(), ActionType.DELETED)
        );
    }

}
