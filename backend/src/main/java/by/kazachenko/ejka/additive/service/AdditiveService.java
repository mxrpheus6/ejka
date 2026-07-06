package by.kazachenko.ejka.additive.service;

import by.kazachenko.ejka.additive.dto.request.AdditiveRequest;
import by.kazachenko.ejka.additive.dto.response.AdditiveResponse;
import by.kazachenko.ejka.additive.dto.response.WorkerAdditiveResponse;
import by.kazachenko.ejka.additive.model.enums.DangerLevel;
import by.kazachenko.ejka.common.dto.response.PageResponse;

import java.util.List;

public interface AdditiveService {

    AdditiveResponse getAdditiveById(Long id);

    AdditiveResponse getAdditiveByCode(String code);

    List<AdditiveResponse> getAdditivesByIds(List<Long> ids);

    PageResponse<AdditiveResponse> getAllAdditives(Integer offset, Integer limit, String sortBy, String sortDirection);

    List<WorkerAdditiveResponse> getAllWorkerAdditives();

    PageResponse<AdditiveResponse> getFilteredAdditives(
            String searchQuery,
            String category,
            DangerLevel dangerLevel,
            List<String> originTypes,
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection);

    AdditiveResponse createAdditive(AdditiveRequest request);

    AdditiveResponse updateAdditive(Long id, AdditiveRequest request);

    void deleteAdditiveById(Long id);

}
