package de.magicline.racoon.common;

import io.vavr.control.Try;

import java.beans.ConstructorProperties;
import java.util.Set;

import org.junit.Test;

import com.tngtech.archunit.base.HasDescription;
import com.tngtech.archunit.core.domain.JavaClassList;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.properties.HasSourceCodeLocation;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import static org.assertj.core.api.Assertions.assertThat;

public class DtosCorrectnessTest {

    @Test
    public void correctParameters() {
        JavaClasses classes = new ClassFileImporter().importPackages("de.magicline");

        ArchRule rule = ArchRuleDefinition.constructors()
                .that().areAnnotatedWith(ConstructorProperties.class)
                .should().bePublic()
                .andShould(haveCorrectConstructor())
                .andShould(haveCorrectFields());

        rule.check(classes);
    }

    private ArchCondition<JavaConstructor> haveCorrectFields() {
        return new ArchCondition<>("have correct fields") {
            @Override
            public void check(JavaConstructor item, ConditionEvents events) {
                Set<JavaField> fields = item.getOwner().getAllFields();
                String[] annotationParams = item.getAnnotationOfType(ConstructorProperties.class).value();
                addEvent(item, events, Try.run(() ->
                        assertThat(fields).extracting(JavaField::getName).containsExactlyInAnyOrder(annotationParams)));
            }
        };
    }

    private ArchCondition<JavaConstructor> haveCorrectConstructor() {
        return new ArchCondition<>("have correct constructor") {
            @Override
            public void check(JavaConstructor item, ConditionEvents events) {
                JavaClassList constructorParams = item.getRawParameterTypes();
                String[] annotationParams = item.getAnnotationOfType(ConstructorProperties.class).value();
                addEvent(item, events, Try.run(() ->
                        assertThat(constructorParams).hasSize(annotationParams.length)));
            }
        };
    }

    private void addEvent(JavaCodeUnit item, ConditionEvents events, Try<Void> assertion) {
        events.add(new SimpleConditionEvent(
                item.getOwner(),
                assertion.isSuccess(),
                createMessage(item.getOwner(), assertion.toString())));
    }

    private static <T extends HasDescription & HasSourceCodeLocation> String createMessage(T object, String message) {
        return object.getDescription() + " " + message + " in " + object.getSourceCodeLocation();
    }

}

