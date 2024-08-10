package dev.chojo.simplepos.entity.response;

import dev.chojo.simplepos.entity.Category;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Listing<T>(List<CategoryGroup<T>> categories) {

    public static <V extends Comparable<V>> Listing<V> map(List<V> type, Function<V, Category> category) {
        List<CategoryGroup<V>> list = type.stream()
                .collect(Collectors.groupingBy(category))
                .entrySet()
                .stream()
                .map((e) -> new CategoryGroup<>(e.getKey(), e.getValue().stream().sorted().toList()))
                .sorted()
                .toList();
        return new Listing<>(list);
    }
}
