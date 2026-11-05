package dev.chojo.simplepos.controller.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * How many entries one page of a listing may hold: at least 1 and at most {@value #MAX}.
 */
@Min(1)
@Max(PageSize.MAX)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageSize {
    int MAX = 500;

    String message() default "{simplepos.validation.page_size}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
