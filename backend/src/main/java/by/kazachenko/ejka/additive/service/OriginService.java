package by.kazachenko.ejka.additive.service;

import by.kazachenko.ejka.additive.dto.request.OriginRequest;
import by.kazachenko.ejka.additive.dto.response.OriginResponse;
import by.kazachenko.ejka.additive.model.Origin;
import by.kazachenko.ejka.common.dto.response.PageResponse;

import java.util.Set;

public interface OriginService {

    Set<Origin> getOriginsByIds(Set<Long> ids);

    OriginResponse getOriginById(Long id);
    PageResponse<OriginResponse> getAllOrigins(
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection
    );

    OriginResponse createOrigin(OriginRequest request);

    void deleteOriginById(Long id);


}
