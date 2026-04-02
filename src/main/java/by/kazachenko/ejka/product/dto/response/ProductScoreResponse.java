package by.kazachenko.ejka.product.dto.response;

import by.kazachenko.ejka.additive.model.enums.DangerLevel;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductScoreResponse {
    private int totalScore;
    private List<MacroDetail> macros;
    private List<AdditiveDetail> additives;

    @Data
    @Builder
    public static class MacroDetail {
        private String name;
        private String value;
        private int score;
        private ImpactLevel impact;
    }

    @Data
    @Builder
    public static class AdditiveDetail {
        private String code;
        private String name;
        private DangerLevel dangerLevel;
        private String description;
        private String warningDescription;
    }

    public enum ImpactLevel {
        EXCELLENT, // Зеленый
        GOOD,      // Салатовый/Желтый
        POOR,      // Оранжевый
        BAD        // Красный
    }
}