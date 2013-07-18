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

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;


public class TagUtils {

    public static final String IGNORE_TAG = "ignore";
    public static final String CONTEXT_TAG = "contextPath";
    public static final String NAME_TAG = "name";
    public static final String PATHVAR_TAG = "pathVar";
    public static final String QUERYPARAM_TAG = "queryParam";
    public static final String REQUESTBODY_TAG = "requestBody";

    public static String findParamText(Tag[] tags, String name) {
        for (Tag tag : tags)
            if (tag.text().trim().equals(name) || tag.text().trim().startsWith(name + " "))
                return tag.text().trim().substring(name.length()).trim();

        return null;
    }

    public static String firstSentence(Doc doc) {
        Tag[] tags = doc.firstSentenceTags();
        StringBuilder sb = new StringBuilder();
        if (!CommonUtils.isEmpty(tags)) {
            for (Tag tag : tags)
                sb.append(tag.text());
        }

        return sb.toString();
    }
}
