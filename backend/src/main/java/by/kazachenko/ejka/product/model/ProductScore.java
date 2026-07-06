package by.kazachenko.ejka.product.model;

import by.kazachenko.ejka.additive.model.enums.DangerLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductScore {
    private int totalScore;
    private List<MacroDetail> macros;
    private List<AdditiveDetail> additives;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditiveDetail {
        private String code;
        private String name;
        private DangerLevel dangerLevel;
        private String description;
        private String warningDescription;
    }

    public enum ImpactLevel {
        EXCELLENT,
        GOOD,
        POOR,
        BAD
    }
}