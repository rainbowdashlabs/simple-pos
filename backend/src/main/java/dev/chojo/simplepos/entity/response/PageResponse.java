package dev.chojo.simplepos.entity.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * One page of a list: its entries, which page it is, how many entries a page holds, and how many entries and pages
 * the whole list has.
 */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
    }

    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return of(page.map(mapper::apply));
    }
}
