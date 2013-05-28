package restdoclet.util;


import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationValue;

import java.util.ArrayList;
import java.util.List;

public class AnnotationUtil {

    public static String getAnnotationName(AnnotationDesc annotation) {
        try{
            return annotation.annotationType().toString();
        } catch (ClassCastException e)
        {
            return null;
        }
    }


    public static List<String> getElementValue(AnnotationDesc annotation, String key) {
        for (AnnotationDesc.ElementValuePair element : annotation.elementValues())
            if (element.element().name().equals(key)) {
                return resolveAnnotationValue(element.value());
            }

        return new ArrayList<String>();
    }

    private static List<String> resolveAnnotationValue(AnnotationValue value) {
        List<String> retVal = new ArrayList<String>();
        /**
         * TODO using recursion here is probably flawed.
         */
        if (value.value() instanceof AnnotationValue[])
            for (AnnotationValue annotationValue : (AnnotationValue[])value.value())
                retVal.addAll(resolveAnnotationValue(annotationValue));
        else {
            retVal.add(value.value().toString());

        }

        return retVal;
    }

}
