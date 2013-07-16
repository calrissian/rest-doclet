/*******************************************************************************
 * Copyright (c) 2013 Edward Wagner. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package restdoclet.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

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

    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        int len;

        while ((len = input.read(buffer)) > 0 ) {
            output.write(buffer, 0, len);
        }
    }

    public static String fixPath(String path) {

        //remove duplicates path seperators
        int len = 0;
        while (path.length() != len) {
            len = path.length();
            path = path.replaceAll("//", "/");
        }

        if (path.length() > 1 && path.endsWith("/"))
            path = path.substring(0, path.length() - 2);

        if (!path.startsWith("/"))
            path = "/" + path;

        return path;
    }
}
