package restdoclet.util;

import com.sun.javadoc.Tag;


public class TagUtils {

    public static final String IGNORE_TAG = "ignore";
    public static final String CONTEXT_TAG = "contextPath";
    public static final String NAME_TAG = "name";
    public static final String PATHVAR_TAG = "pathVar";
    public static final String QUERYPARAM_TAG = "queryParam";

    public static String findParamText(Tag[] tags, String name) {
        for (Tag tag : tags)
            if (tag.text().trim().equals(name) || tag.text().trim().startsWith(name + " "))
                return tag.text().trim().substring(name.length()).trim();

        return null;
    }

}
