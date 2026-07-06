package by.kazachenko.ejka.additive.dto.response;

import by.kazachenko.ejka.additive.model.enums.DangerLevel;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditiveResponse {
    private Long id;
    private String code;
    private String nameRu;
    private String nameEn;
    private String category;
    private DangerLevel dangerLevel;
    private String warningDescription;
    private String description;
    private Set<OriginResponse> origins;
}
