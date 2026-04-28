package by.kazachenko.ejka.additive.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AllergenCategory {

    PEANUT("Арахис"),
    ASPARTAME_SULFITES("Аспартам и сульфиты"),
    MUSTARD("Горчица"),
    GLUTEN("Злаки (глютен)"),
    SESAME("Кунжут"),
    LUPIN("Люпин"),
    MOLLUSCS("Моллюски"),
    CRUSTACEANS("Ракообразные"),
    MILK("Молоко и лактоза"),
    NUTS("Орехи"),
    FISH("Рыба"),
    CELERY("Сельдерей"),
    SOY("Соя"),
    EGGS("Яйца");

    private final String displayName;

}
