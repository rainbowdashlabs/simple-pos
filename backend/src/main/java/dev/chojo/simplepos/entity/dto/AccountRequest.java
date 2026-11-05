package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The name a client gives an account when creating or renaming it.
 */
public record AccountRequest(@NotBlank
                             @Size(max = 100)
                             @Pattern(regexp = "\\P{Cc}*", message = "must not contain control characters")
                             String name) {
}
