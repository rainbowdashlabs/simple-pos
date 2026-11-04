package dev.chojo.simplepos.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * What a client may set on a category.
 */
public record CategoryRequest(@NotBlank @Size(max = 100) String name) {
}
