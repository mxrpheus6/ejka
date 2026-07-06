package by.kazachenko.ejka.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Food Scanner Backend API",
                version = "1.0",
                description = "API designed for product analysis"
        )
)
public class OpenApiConfiguration {

}
