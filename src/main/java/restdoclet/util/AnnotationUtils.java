/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package restdoclet.util;


import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationValue;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class AnnotationUtils {

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

        return emptyList();
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
