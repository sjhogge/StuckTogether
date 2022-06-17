package com.brooklynotter.stucktogether.util;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public class CollectionHelper {

    @Nullable
    public static <T> T find(Collection<T> collection, Predicate<T> predicate) {
        for (T item : collection) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }

}
