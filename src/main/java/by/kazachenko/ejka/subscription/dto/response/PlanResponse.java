package by.kazachenko.ejka.subscription.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanResponse {
    private String id;          // ID цены в Stripe (например, price_12345)
    private String name;        // Название продукта (например, "Premium E-Scanner")
    private Double price;       // Цена в нормальном виде (например, 5.00)
    private String currency;    // Валюта (например, "USD" или "BYN")
    private String interval;    // Период (например, "month")
}
