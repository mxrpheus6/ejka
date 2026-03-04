package by.kazachenko.ejka.additive.service.impl;

import by.kazachenko.ejka.additive.dto.request.OriginRequest;
import by.kazachenko.ejka.additive.dto.response.OriginResponse;
import by.kazachenko.ejka.additive.mapper.OriginMapper;
import by.kazachenko.ejka.additive.model.Origin;
import by.kazachenko.ejka.additive.repository.OriginRepository;
import by.kazachenko.ejka.additive.service.OriginService;
import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.common.exception.cutom.OriginAlreadyExistsException;
import by.kazachenko.ejka.common.exception.cutom.OriginNotFoundException;
import by.kazachenko.ejka.common.mapper.PageResponseMapper;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OriginServiceImpl implements OriginService {

    private final OriginRepository originRepository;
    private final OriginMapper originMapper;

    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public Set<Origin> getOriginsByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Origin> origins = new HashSet<>(originRepository.findAllById(ids));

        if (origins.size() != ids.size()) {
            throw new OriginNotFoundException("Один или несколько типов происхождений не найдены");
        }

        return origins;
    }

    @Override
    @Transactional(readOnly = true)
    public OriginResponse getOriginById(Long id) {
        Origin origin = originRepository.findById(id)
                .orElseThrow(() -> new OriginNotFoundException("Тип происхождения не найден"));

        return originMapper.toResponse(origin);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OriginResponse> getAllOrigins(
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(offset, limit, Sort.by(direction, sortBy));

        Page<OriginResponse> responsePage = originRepository
                .findAll(pageable)
                .map(originMapper::toResponse);

        return pageResponseMapper.toResponse(responsePage);
    }

    @Override
    @Transactional
    public OriginResponse createOrigin(OriginRequest request) {
        if (originRepository.existsByType(request.type())) {
            throw new OriginAlreadyExistsException("Тип происхождения уже существует");
        }

        Origin origin = originMapper.toEntity(request);

        originRepository.save(origin);

        return originMapper.toResponse(origin);
    }

    @Override
    @Transactional
    public void deleteOriginById(Long id) {
        Origin origin = originRepository.findById(id)
                .orElseThrow(() -> new OriginNotFoundException("Тип происхождения не найден"));

        originRepository.delete(origin);
    }

}
