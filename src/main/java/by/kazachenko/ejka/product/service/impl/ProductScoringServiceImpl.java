package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.product.model.ProductScore;
import by.kazachenko.ejka.product.model.ProductScore.AdditiveDetail;
import by.kazachenko.ejka.product.model.ProductScore.ImpactLevel;
import by.kazachenko.ejka.product.model.ProductScore.MacroDetail;
import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.enums.ProductCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductScoringServiceImpl {

    public ProductScore calculateScoreDetails(Product product) {
        ProductCategory category = product.getCategory() != null ? product.getCategory() : ProductCategory.GENERAL;

        // 1. Оценка макронутриентов
        MacroDetail caloriesDetail = rateCalories(product.getCalories(), category);
        MacroDetail proteinsDetail = rateProteins(product.getProteins());
        MacroDetail fatsDetail = rateFats(product.getFats(), category);
        MacroDetail carbsDetail = rateCarbs(product.getCarbohydrates(), category);

        List<MacroDetail> macros = List.of(caloriesDetail, proteinsDetail, fatsDetail, carbsDetail);

        // 2. Базовый нутриентный балл
        double baseScore = (caloriesDetail.getScore() * 0.40) +
                (proteinsDetail.getScore() * 0.30) +
                (fatsDetail.getScore() * 0.15) +
                (carbsDetail.getScore() * 0.15);

        // 3. Оценка добавок
        int additivesPenalty = 0;
        boolean hasBanned = false;
        boolean hasDangerous = false;

        List<AdditiveDetail> additiveDetails = List.of();
        if (product.getAdditives() != null && !product.getAdditives().isEmpty()) {
            additiveDetails = product.getAdditives().stream().map(additive ->
                    AdditiveDetail.builder()
                            .code(additive.getCode())
                            .name(additive.getNameRu())
                            .description(additive.getDescription())
                            .warningDescription(additive.getWarningDescription())
                            .dangerLevel(additive.getDangerLevel())
                            .build()
            ).collect(Collectors.toList());

            int warningPenalty = 0;
            for (Additive additive : product.getAdditives()) {
                switch (additive.getDangerLevel()) {
                    case BANNED -> hasBanned = true;
                    case DANGEROUS -> {
                        additivesPenalty += 30;
                        hasDangerous = true;
                    }
                    case WARNING -> warningPenalty += 10;
                    case SAFE -> {}
                }
            }
            additivesPenalty += Math.min(warningPenalty, 20);
        }

        // 4. Итоговый расчет
        int finalScore = (int) Math.round(baseScore) - additivesPenalty;

        if (hasBanned) {
            finalScore = 0;
        } else if (hasDangerous && finalScore > 39) {
            finalScore = 39;
        }

        finalScore = Math.max(0, Math.min(finalScore, 100));

        return ProductScore.builder()
                .totalScore(finalScore)
                .macros(macros)
                .additives(additiveDetails)
                .build();
    }


    private MacroDetail rateCalories(Integer calories, ProductCategory category) {
        double val = (calories != null) ? calories.doubleValue() : 0.0;
        double[] thresholds;
        int score;

        switch (category) {
            case BEVERAGES -> thresholds = new double[]{20.0, 40.0, 60.0};
            case FATS_AND_OILS, NUTS_AND_SEEDS, CEREALS_AND_LEGUMES -> thresholds = new double[]{400.0, 600.0, 800.0};
            default -> thresholds = new double[]{160.0, 360.0, 560.0};
        }

        if (val <= thresholds[0]) score = 100;
        else if (val <= thresholds[1]) score = 75;
        else if (val <= thresholds[2]) score = 50;
        else score = 25;

        return buildMacroDetail("kcal", val, "ккал", score, thresholds, false);
    }

    private MacroDetail rateProteins(BigDecimal proteins) {
        double val = (proteins != null) ? proteins.doubleValue() : 0.0;
        // Для белков шкала перевернута: [Отлично >= 8.0, Норма >= 0.1, Плохо < 0.1]
        double[] thresholds = new double[]{8.0, 0.1, 0.0};
        int score;

        if (val >= thresholds[0]) score = 100;
        else if (val >= thresholds[1]) score = 75;
        else score = 50;

        return buildMacroDetail("proteins", val, "г", score, thresholds, true);
    }

    private MacroDetail rateFats(BigDecimal fats, ProductCategory category) {
        double val = (fats != null) ? fats.doubleValue() : 0.0;
        double[] thresholds;
        int score = -1;

        switch (category) {
            case FATS_AND_OILS -> {
                thresholds = new double[]{100.0, 100.0, 100.0};
                score = 100;
            }
            case NUTS_AND_SEEDS -> thresholds = new double[]{60.0, 70.0, 80.0};
            case BEVERAGES -> thresholds = new double[]{1.0, 3.0, 5.0};
            case SNACKS_AND_SWEETS, SAUCES -> thresholds = new double[]{3.0, 10.0, 17.5};
            default -> thresholds = new double[]{3.0, 17.5, 21.0};
        }

        if (score == -1) {
            if (val <= thresholds[0]) score = 100;
            else if (val <= thresholds[1]) score = 75;
            else if (val <= thresholds[2]) score = 50;
            else score = 25;
        }

        return buildMacroDetail("fats", val, "г", score, thresholds, false);
    }

    private MacroDetail rateCarbs(BigDecimal carbs, ProductCategory category) {
        double val = (carbs != null) ? carbs.doubleValue() : 0.0;
        double[] thresholds;
        int score;

        switch (category) {
            case CEREALS_AND_LEGUMES -> thresholds = new double[]{75.0, 85.0, 100.0};
            case MEAT_AND_FISH -> thresholds = new double[]{1.0, 5.0, 10.0};
            case BEVERAGES -> thresholds = new double[]{2.0, 5.0, 8.0};
            case DAIRY -> thresholds = new double[]{6.0, 10.0, 15.0};
            case SNACKS_AND_SWEETS, SAUCES -> thresholds = new double[]{5.0, 15.0, 30.0};
            default -> thresholds = new double[]{15.0, 30.0, 50.0};
        }

        if (val <= thresholds[0]) score = 100;
        else if (val <= thresholds[1]) score = 75;
        else if (val <= thresholds[2]) score = 50;
        else score = 25;

        return buildMacroDetail("carbs", val, "г", score, thresholds, false);
    }

    private MacroDetail buildMacroDetail(String name, double numericValue, String unit, int score, double[] thresholds, boolean higherBetter) {
        ImpactLevel impact;
        if (score == 100) impact = ImpactLevel.EXCELLENT;
        else if (score == 75) impact = ImpactLevel.GOOD;
        else if (score == 50) impact = ImpactLevel.POOR;
        else impact = ImpactLevel.BAD;

        // Форматируем текст: для калорий целое число, для БЖУ - с одной запятой
        String formattedValue = name.equals("kcal") ?
                String.format("%d %s", (int) numericValue, unit) :
                String.format("%.1f %s", numericValue, unit);

        return MacroDetail.builder()
                .name(name)
                .value(formattedValue)
                .numericValue(numericValue)
                .thresholds(List.of(thresholds[0], thresholds[1], thresholds[2]))
                .higherBetter(higherBetter)
                .score(score)
                .impact(impact)
                .build();
    }
}