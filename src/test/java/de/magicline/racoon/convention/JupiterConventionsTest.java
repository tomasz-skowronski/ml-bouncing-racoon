package de.magicline.racoon.convention;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

class JupiterConventionsTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("de.magicline");

    @Test
    void noJunit4Annotations() {
        ArchRule rule = ArchRuleDefinition.noMethods()
                .should().beAnnotatedWith(org.junit.Test.class)
                .orShould().beAnnotatedWith(Ignore.class);

        rule.check(classes);
    }

    @Test
    void noPublicMethodsInJupiterTests() {
        ArchRule rule = ArchRuleDefinition.methods()
                .that().areAnnotatedWith(Test.class)
                .should().bePackagePrivate();

        rule.check(classes);
    }

    @Test
    void noPublicClassesInJupiterTests() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Test")
                .or().haveSimpleNameEndingWith("IT")
                .should().bePackagePrivate();

        rule.check(classes);
    }

}

