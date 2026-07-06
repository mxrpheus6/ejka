package by.kazachenko.ejka.product.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductScoreResponse {
    private List<MacroDetail> macros;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MacroDetail {
        private String name;
        private String value;

        private Double numericValue;
        private List<Double> thresholds;
        private boolean higherBetter;

        private int score;
        private ImpactLevel impact;
    }

    public enum ImpactLevel {
        EXCELLENT,
        GOOD,
        POOR,
        BAD
    }
}
