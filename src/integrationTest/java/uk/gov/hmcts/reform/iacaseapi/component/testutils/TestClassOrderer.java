package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import java.util.Comparator;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

public class TestClassOrderer implements ClassOrderer {
    @Override
    public void orderClasses(ClassOrdererContext context) {
        context.getClassDescriptors().sort(Comparator.comparing((ClassDescriptor d) -> d.getTestClass().getSimpleName()));
        System.out.println("Applied custom test class execution order. Execution order follows:");
        context.getClassDescriptors().forEach(descriptor -> System.out.println("    " + descriptor.getTestClass().getName()));
    }
}