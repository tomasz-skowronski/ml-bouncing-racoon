package de.magicline.racoon.config.swagger;

import io.swagger.annotations.ApiModel;
import springfox.documentation.schema.AlternateTypeRule;

import java.time.LocalDateTime;

import com.fasterxml.classmate.TypeResolver;

public class LocalDateTimeTypeRule extends AlternateTypeRule {
    public LocalDateTimeTypeRule(TypeResolver typeResolver) {
        super(typeResolver.resolve(LocalDateTime.class), typeResolver.resolve(SwaggerLocalDate.class));
    }

    @ApiModel(value = "date-time")
    private static class SwaggerLocalDate {
    }
}
