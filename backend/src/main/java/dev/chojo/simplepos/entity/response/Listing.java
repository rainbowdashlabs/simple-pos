package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Category;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Listing<T>(List<CategoryGroup<T>> categories) {

    /**
     * Groups entries by their category. Entries without a category are grouped under
     * {@link Category#UNCATEGORISED}, which comes last.
     */
    public static <V extends Comparable<V>> Listing<V> map(List<V> type, Function<V, Category> category) {
        Function<V, Category> grouping = entry -> Objects.requireNonNullElse(category.apply(entry), Category.UNCATEGORISED);
        List<CategoryGroup<V>> list = type.stream()
                .collect(Collectors.groupingBy(grouping))
                .entrySet()
                .stream()
                .map((e) -> new CategoryGroup<>(e.getKey(), e.getValue().stream().sorted().toList()))
                .sorted(Comparator.comparing((CategoryGroup<V> group) -> group.category() == Category.UNCATEGORISED)
                        .thenComparing(Comparator.naturalOrder()))
                .toList();
        return new Listing<>(list);
    }
}
