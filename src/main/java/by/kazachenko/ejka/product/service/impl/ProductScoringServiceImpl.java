package by.kazachenko.ejka.product.service.impl;

import by.kazachenko.ejka.additive.model.Additive;
import by.kazachenko.ejka.product.dto.response.ProductScoreResponse;
import by.kazachenko.ejka.product.dto.response.ProductScoreResponse.AdditiveDetail;
import by.kazachenko.ejka.product.dto.response.ProductScoreResponse.ImpactLevel;
import by.kazachenko.ejka.product.dto.response.ProductScoreResponse.MacroDetail;
import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.model.enums.ProductCategory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductScoringServiceImpl {

    public ProductScoreResponse calculateScoreDetails(Product product) {
        // Если категория не задана, используем общую логику
        ProductCategory category = product.getCategory() != null ? product.getCategory() : ProductCategory.GENERAL;

        List<MacroDetail> macros = new ArrayList<>();

        MacroDetail caloriesDetail = rateCalories(product.getCalories(), category);
        MacroDetail proteinsDetail = rateProteins(product.getProteins(), category);
        MacroDetail fatsDetail = rateFats(product.getFats(), category);
        MacroDetail carbsDetail = rateCarbs(product.getCarbohydrates(), category);

        macros.add(caloriesDetail);
        macros.add(proteinsDetail);
        macros.add(fatsDetail);
        macros.add(carbsDetail);

        // 1. Взвешенный средний балл (Калории - 40%, Белки - 30%, Жиры - 15%, Углеводы - 15%)
        double weightedScore = (caloriesDetail.getScore() * 0.40) +
                (proteinsDetail.getScore() * 0.30) +
                (fatsDetail.getScore() * 0.15) +
                (carbsDetail.getScore() * 0.15);
        int baseScore = (int) Math.round(weightedScore);

        // 2. Оцениваем добавки (твоя отличная логика оставлена без изменений)
        List<AdditiveDetail> additiveDetails = new ArrayList<>();
        int additivesPenalty = 0;
        int warningPenalty = 0;
        boolean hasDangerous = false;
        boolean hasBanned = false;

        if (product.getAdditives() != null) {
            for (Additive additive : product.getAdditives()) {
                additiveDetails.add(AdditiveDetail.builder()
                        .code(additive.getCode())
                        .name(additive.getNameRu())
                        .description(additive.getDescription())
                        .warningDescription(additive.getWarningDescription())
                        .dangerLevel(additive.getDangerLevel())
                        .build());

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
        }

        if (warningPenalty > 20) warningPenalty = 20;
        additivesPenalty += warningPenalty;

        // 3. Высчитываем итоговый балл
        int finalScore = baseScore - additivesPenalty;

        if (hasBanned) {
            finalScore = 0;
        } else if (hasDangerous && finalScore > 39) {
            finalScore = 39;
        }

        finalScore = Math.max(0, Math.min(finalScore, 100));

        return ProductScoreResponse.builder()
                .totalScore(finalScore)
                .macros(macros)
                .additives(additiveDetails)
                .build();
    }

    // --- Логика оценки макросов с учетом категорий ---

    private MacroDetail rateCalories(Integer calories, ProductCategory category) {
        int cal = (calories != null) ? calories : 0;
        int score;

        switch (category) {
            case FATS_AND_OILS, NUTS_AND_SEEDS -> score = 100; // Прощаем высокую калорийность
            case BEVERAGES -> {
                if (cal <= 20) score = 100;
                else if (cal <= 40) score = 75;
                else if (cal <= 60) score = 50;
                else score = 25;
            }
            default -> { // GENERAL и остальные
                if (cal <= 160) score = 100;
                else if (cal <= 360) score = 75;
                else if (cal <= 560) score = 50;
                else score = 25;
            }
        }

        return buildMacroDetail("Калорийность", cal + " ккал", score);
    }

    private MacroDetail rateProteins(BigDecimal proteins, ProductCategory category) {
        double p = (proteins != null) ? proteins.doubleValue() : 0.0;
        int score;

        switch (category) {
            case MEAT_AND_FISH -> {
                if (p >= 18.0) score = 100; // От мяса ждем много белка
                else if (p >= 12.0) score = 75;
                else if (p >= 8.0) score = 50;
                else score = 25; // Накачали водой или соей
            }
            default -> {
                if (p >= 12.0) score = 100;
                else if (p >= 6.0) score = 75;
                else if (p >= 3.0) score = 50;
                else score = 25;
            }
        }

        return buildMacroDetail("Белки", String.format("%.1f г", p), score);
    }

    private MacroDetail rateFats(BigDecimal fats, ProductCategory category) {
        double f = (fats != null) ? fats.doubleValue() : 0.0;
        int score;

        switch (category) {
            case FATS_AND_OILS, NUTS_AND_SEEDS -> score = 100; // Это их суть, жиры здесь полезные
            case BEVERAGES -> {
                if (f <= 1.0) score = 100; // В напитках жиров быть почти не должно (кроме молока)
                else if (f <= 3.0) score = 75;
                else score = 25;
            }
            default -> {
                if (f <= 5.0) score = 100;
                else if (f <= 15.0) score = 75;
                else if (f <= 30.0) score = 50;
                else score = 25;
            }
        }

        return buildMacroDetail("Жиры", String.format("%.1f г", f), score);
    }

    private MacroDetail rateCarbs(BigDecimal carbs, ProductCategory category) {
        double c = (carbs != null) ? carbs.doubleValue() : 0.0;
        int score;

        switch (category) {
            case CEREALS_AND_LEGUMES -> {
                if (c <= 75.0) score = 100; // Норма для круп
                else if (c <= 85.0) score = 75;
                else score = 50;
            }
            case BEVERAGES -> {
                if (c <= 2.0) score = 100; // Вода или чай без сахара
                else if (c <= 5.0) score = 75; // Легкий компот
                else if (c <= 8.0) score = 50;
                else score = 25; // Сладкие газировки и соки
            }
            case FATS_AND_OILS, MEAT_AND_FISH -> {
                if (c <= 3.0) score = 100; // В мясе и масле углеводов быть не должно
                else if (c <= 10.0) score = 50; // Панировка или маринад
                else score = 25; // Много крахмала или сахара
            }
            default -> {
                if (c <= 30.0) score = 100;
                else if (c <= 55.0) score = 75;
                else if (c <= 70.0) score = 50;
                else score = 25;
            }
        }

        return buildMacroDetail("Углеводы", String.format("%.1f г", c), score);
    }

    // --- Утилитарный метод для сборки MacroDetail ---

    private MacroDetail buildMacroDetail(String name, String value, int score) {
        ImpactLevel impact;
        if (score >= 100) impact = ImpactLevel.EXCELLENT;
        else if (score >= 75) impact = ImpactLevel.GOOD;
        else if (score >= 50) impact = ImpactLevel.POOR;
        else impact = ImpactLevel.BAD;

        return MacroDetail.builder()
                .name(name)
                .value(value)
                .score(score)
                .impact(impact)
                .build();
    }
}