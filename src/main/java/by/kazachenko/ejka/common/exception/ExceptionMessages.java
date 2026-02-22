package by.kazachenko.ejka.common.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionMessages {

    public static final String TOKEN_INVALID = "Версия токена не действительна. Авторизуйтесь еще раз";


    public static final String USER_NOT_FOUND = "Пользователь не найден";
    public static final String USER_USERNAME_ALREADY_EXISTS  = "Пользователь с таким именем уже существует";
    public static final String USER_EMAIL_ALREADY_EXISTS  = "Пользователь с таким email уже существует";


    public static final String PRODUCT_NOT_FOUND = "Продукт не найден";
    public static final String PRODUCT_BARCODE_ALREADY_EXISTS  = "Продукт с таким штрих-кодом уже существует";


}
