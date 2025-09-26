package com.db.assetstore.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T emptyIfNullOrEmpty(T input) {
        if (input == null) {
            return (T) emptyForType(null);
        }

        if (input instanceof Collection) {
            var col = (Collection<?>) input;
            if (col.isEmpty()) {
                return (T) emptyForType(col);
            }
        } else if (input instanceof Map) {
            var map = (Map<?, ?>) input;
            if (map.isEmpty()) {
                return (T) emptyForType(map);
            }
        }

        return input;
    }

    private static Object emptyForType(Object sample) {
        if (sample instanceof List || sample == null) {
            return List.of();
        }
        if (sample instanceof Set) {
            return Set.of();
        }
        if (sample instanceof Map) {
            return Map.of();
        }
        if (sample instanceof Collection) {
            return Collections.emptyList();
        }
        throw new IllegalArgumentException("Unsupported type: " + (sample != null ? sample.getClass() : "null"));
    }
}
