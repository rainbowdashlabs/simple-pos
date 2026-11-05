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
 * The index of a page of a listing, starting at 0, and small enough that the offset of the largest page still fits
 * an int.
 */
@Min(0)
@Max(Integer.MAX_VALUE / PageSize.MAX)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageNumber {
    String message() default "{simplepos.validation.page_number}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
