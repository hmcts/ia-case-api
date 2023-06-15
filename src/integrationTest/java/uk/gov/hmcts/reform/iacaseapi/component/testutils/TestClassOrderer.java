package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import java.util.Comparator;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;
import org.junit.jupiter.api.MethodDescriptor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrdererContext;

public class TestClassOrderer implements ClassOrderer, MethodOrderer {
    @Override
    public void orderClasses(ClassOrdererContext context) {
        context.getClassDescriptors().sort(Comparator.comparing((ClassDescriptor d) -> d.getTestClass().getSimpleName()));
        System.out.println("Applied custom test class execution order. Execution order follows:");
        context.getClassDescriptors().forEach(descriptor -> System.out.println("    " + descriptor.getTestClass().getName()));
    }

    @Override
    public void orderMethods(MethodOrdererContext context) {
        context.getMethodDescriptors().sort(Comparator.comparing(MethodDescriptor::getDisplayName));
        System.out.println("Applied custom test method execution order for class " + context.getTestClass().getSimpleName() + ". Execution order follows:");
        context.getMethodDescriptors().forEach(descriptor -> System.out.println("    " + descriptor.getDisplayName()));
    }
}