package by.kazachenko.ejka.subscription.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanResponse {
    private String id;
    private String name;
    private Double price;
    private String currency;
    private String interval;
}
