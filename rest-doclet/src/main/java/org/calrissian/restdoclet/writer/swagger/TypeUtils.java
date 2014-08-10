/*******************************************************************************
 * Copyright (C) 2014 The Calrissian Authors
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
package org.calrissian.restdoclet.writer.swagger;


import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.calrissian.restdoclet.util.CommonUtils.isEmpty;

class TypeUtils {

    /**
     * Will return a full data type for Swagger.
     * @param type
     * @return
     */
    public static String dataType(Type type) {
        if (type == null)
            return null;

        if (isContainer(type)) {
            //treat sets as sets
            if (isType(type.asClassDoc(), Set.class))
                return "Set[" + internalContainerType(type) + "]";

            return "List[" + internalContainerType(type) + "]";
        }

        //Treat as a basic type.
        return basicType(type);
    }

    /**
     * Checks if the type is an iterable or an array
     * @param type
     * @return
     */
    public static boolean isContainer(Type type) {

        //first check for arrays
        if (type.dimension() != null && !type.dimension().isEmpty())
            return true;

        //treat iterables as lists
        if (isType(type.asClassDoc(), Iterable.class))
            return true;

        return false;
    }

    /**
     * This will grab the internal type from an array or a parameterized container.
     * @param type
     * @return
     */
    public static String internalContainerType(Type type) {
        //treat arrays first
        if (type.dimension() != null && !type.dimension().isEmpty())
            return basicType(type);

        ParameterizedType pType = type.asParameterizedType();
        if (pType != null) {
            Type[] paramTypes = ((ParameterizedType)type).typeArguments();
            if (!isEmpty(paramTypes))
                return basicType(paramTypes[0]);
        }

        //TODO look into supporting models.
        return "Object";
    }

    /**
     * Returns the basic type.  If not one of the supported swagger basic types then it is treated as an Object.
     * @param type
     * @return
     */
    public static String basicType(Type type) {
        if (type == null)
            return "void";

        //next primitives
        if (type.isPrimitive())
            return type.qualifiedTypeName();

        String name = type.qualifiedTypeName();

        //Check the java.lang classes
        if (name.equals(String.class.getName()))
            return "string";

        if (name.equals(Boolean.class.getName()))
            return "boolean";

        if (name.equals(Integer.class.getName()))
            return "int";

        if (name.equals(Long.class.getName()))
            return "long";

        if (name.equals(Float.class.getName()))
            return "float";

        if (name.equals(Double.class.getName()))
            return "double";

        if (name.equals(Byte.class.getName()))
            return "byte";

        if (name.equals(Date.class.getName()))
            return "Date";

        //Process enums as strings.
        if (!isEmpty(type.asClassDoc().enumConstants()))
            return "string";

        //TODO look into supporting models.
        return "object";
    }

    /**
     * This will retrieve all known allowable values from an enum.
     * @param type
     * @return
     */
    public static Collection<String> allowableValues(Type type) {
        if (type == null || type.asClassDoc() == null)
            return emptyList();

        FieldDoc[] fields = type.asClassDoc().enumConstants();
        if (isEmpty(fields))
            return emptyList();

        Collection<String> values = new ArrayList<String>(fields.length);
        for (FieldDoc field : fields)
            values.add(field.name());

        return values;
    }

    /**
     * Checks the class doc to see if it is a type or subtype of the provided class or object.
     * @param classDoc
     * @param targetClazz
     * @param <T>
     * @return
     */
    private static <T> boolean isType(ClassDoc classDoc, Class<T> targetClazz) {
        if (classDoc == null)
            return false;

        if (classDoc.qualifiedTypeName().equals(targetClazz.getName()))
            return true;

        if (isType(classDoc.superclass(), targetClazz))
            return true;

        for (ClassDoc iface : classDoc.interfaces())
            if (isType(iface, targetClazz))
                return true;

        return false;
    }

}
