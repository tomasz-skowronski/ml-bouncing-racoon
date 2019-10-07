package de.magicline.racoon.config;

import com.fasterxml.classmate.TypeResolver;
import de.magicline.racoon.swagger.LocalDateTimeTypeRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//@Configuration
//@EnableSwagger2
public class SwaggerConfig {

    private static final String CONTROLLER_BASE_PACKAGE = "de.magicline.racoon.api";

    private final TypeResolver typeResolver;

    public SwaggerConfig(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(CONTROLLER_BASE_PACKAGE))
                .build()
                .alternateTypeRules(new LocalDateTimeTypeRule(typeResolver));
    }
}
