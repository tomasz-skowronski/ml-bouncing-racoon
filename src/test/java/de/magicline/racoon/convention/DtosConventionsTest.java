package de.magicline.racoon.convention;

import io.vavr.control.Try;

import java.beans.ConstructorProperties;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.base.HasDescription;
import com.tngtech.archunit.core.domain.JavaClassList;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.properties.HasSourceCodeLocation;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class DtosConventionsTest {

    @Test
    void correctParameters() {
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
                Set<String> fieldsNames = item.getOwner().getAllFields().stream()
                        .map(JavaMember::getName)
                        .collect(Collectors.toSet());
                String[] annotationParams = item.getAnnotationOfType(ConstructorProperties.class).value();
                addEvent(item, events, Try.run(() ->
                        assertThat(annotationParams).as("@ConstructorProperties names").containsExactlyInAnyOrderElementsOf(fieldsNames)));
                Set<String> fieldsNamesFinalOnly = item.getOwner().getAllFields().stream()
                        .filter(field -> Modifier.isFinal(field.reflect().getModifiers()))
                        .map(JavaMember::getName)
                        .collect(Collectors.toSet());
                addEvent(item, events, Try.run(() ->
                        assertThat(fieldsNamesFinalOnly).as("final fields").containsExactlyInAnyOrderElementsOf(fieldsNames)));
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
                        assertThat(annotationParams).as("@ConstructorProperties names").hasSize(constructorParams.size())));
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

