package restdoclet.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.emptySet;

/**
 * Simple utilities to reduce the number of dependencies needed for the project
 */
public class CommonUtils {

    public static <T> boolean isEmpty(T[] items) {
        return items == null || items.length == 0;
    }

    public static <T> boolean isEmpty(Collection<T> items) {
        return items == null || items.isEmpty();
    }

    public static void closeQuietly(Closeable closeable) {

        if (closeable == null)
            return;

        try {
            closeable.close();
        } catch (IOException e) { /* do nothing */ }

    }

    public static <T> Collection<T> firstNonEmpty(Collection<T>... collections) {
        for (Collection<T> collection : collections)
            if (!isEmpty(collection))
                return collection;

        return emptySet();
    }
}
