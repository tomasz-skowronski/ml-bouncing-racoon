package de.magicline.racoon.swagger;

import com.fasterxml.classmate.TypeResolver;
import io.swagger.annotations.ApiModel;
import springfox.documentation.schema.AlternateTypeRule;

import java.time.LocalDateTime;

public class LocalDateTimeTypeRule extends AlternateTypeRule {
    public LocalDateTimeTypeRule(TypeResolver typeResolver) {
        super(typeResolver.resolve(LocalDateTime.class), typeResolver.resolve(SwaggerLocalDate.class));
    }

    @ApiModel(value = "date-time")
    private static class SwaggerLocalDate {
    }
}
