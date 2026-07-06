package by.kazachenko.ejka.common.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Component;

@Component
public class SqlFunctionsContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {

        functionContributions.getFunctionRegistry()
                .registerPattern(
                        "word_similarity",
                        "word_similarity(?1, ?2)",
                        functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.DOUBLE)
                );

        functionContributions.getFunctionRegistry()
                .registerPattern(
                        "chr",
                        "chr(?1)",
                        functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.STRING)
                );
    }
}
