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
package restdoclet.collector.jaxrs;

import com.sun.javadoc.*;
import restdoclet.collector.AbstractCollector;
import restdoclet.collector.EndpointMapping;
import restdoclet.model.PathVariableDescriptor;
import restdoclet.model.QueryParamDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static restdoclet.util.AnnotationUtils.getAnnotationName;
import static restdoclet.util.AnnotationUtils.getElementValue;
import static restdoclet.util.TagUtils.*;

public class JaxRSCollector extends AbstractCollector {

    protected static final String ANNOTATION_PACKAGE = "javax.ws.rs.";

    protected static final String PATH_ANNOTATION = ANNOTATION_PACKAGE + "Path";

    protected static final String GET_ANNOTATION = ANNOTATION_PACKAGE + "GET";
    protected static final String POST_ANNOTATION = ANNOTATION_PACKAGE + "POST";
    protected static final String PUT_ANNOTATION = ANNOTATION_PACKAGE + "PUT";
    protected static final String DELETE_ANNOTATION = ANNOTATION_PACKAGE + "DELETE";
    protected static final String HEAD_ANNOTATION = ANNOTATION_PACKAGE + "HEAD";

    protected static final String CONSUMES_ANNOTATION = ANNOTATION_PACKAGE + "Consumes";
    protected static final String PRODUCES_ANNOTATION = ANNOTATION_PACKAGE + "Produces";

    protected static final String PATHVAR_ANNOTATION = ANNOTATION_PACKAGE + "PathParam";
    protected static final String PARAM_ANNOTATION = ANNOTATION_PACKAGE + "QueryParam";

    @Override
    protected boolean shouldIgnoreClass(ClassDoc classDoc) {

        //Look for any JAXRS annotations in the class or the methods.  If found then don't ignore this class.
        for (AnnotationDesc classAnnotation : classDoc.annotations()) {
            String annotationName = getAnnotationName(classAnnotation);
            if (annotationName != null && annotationName.startsWith(ANNOTATION_PACKAGE))
                return false;

        }

        for (MethodDoc methodDoc : classDoc.methods(true)) {
            if (!shouldIgnoreMethod(methodDoc))
                return false;
        }

        return true;
    }

    @Override
    protected boolean shouldIgnoreMethod(MethodDoc methodDoc) {

        //Jax RS methods need a method annotation inorder to be used, so simply look for them.
        for (AnnotationDesc methodAnnotation : methodDoc.annotations()) {
            String annotationName = getAnnotationName(methodAnnotation);
            if (GET_ANNOTATION.equals(annotationName) ||
                    POST_ANNOTATION.equals(annotationName) ||
                    PUT_ANNOTATION.equals(annotationName) ||
                    DELETE_ANNOTATION.equals(annotationName) ||
                    HEAD_ANNOTATION.equals(annotationName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected EndpointMapping getEndpointMapping(ProgramElementDoc doc) {
        Collection<String> paths = new LinkedHashSet<String>();
        Collection<String> httpMethods = new LinkedHashSet<String>();
        Collection<String> consumes = new LinkedHashSet<String>();
        Collection<String> produces = new LinkedHashSet<String>();

        //Look for a request mapping annotation
        for (AnnotationDesc annotation : doc.annotations()) {

            String annotationName = getAnnotationName(annotation);

            if (GET_ANNOTATION.equals(annotationName) ||
                    POST_ANNOTATION.equals(annotationName) ||
                    PUT_ANNOTATION.equals(annotationName) ||
                    DELETE_ANNOTATION.equals(annotationName) ||
                    HEAD_ANNOTATION.equals(annotationName)) {

                httpMethods.add(annotationName.replace(ANNOTATION_PACKAGE, ""));

            } else if (PATH_ANNOTATION.equals(annotationName)) {
                paths.addAll(getElementValue(annotation, "value"));
            } else if (CONSUMES_ANNOTATION.equals(annotationName)) {
                consumes.addAll(getElementValue(annotation, "value"));
            } else if (PRODUCES_ANNOTATION.equals(annotationName)) {
                produces.addAll(getElementValue(annotation, "value"));
            }
        }

        return new EndpointMapping(
                paths,
                httpMethods,
                consumes,
                produces
        );
    }

    @Override
    protected Collection<PathVariableDescriptor> generatePathVars(MethodDoc methodDoc) {
        Collection<PathVariableDescriptor> retVal = new ArrayList<PathVariableDescriptor>();

        Tag[] tags = methodDoc.tags(PATHVAR_TAG);
        ParamTag[] paramTags = methodDoc.paramTags();

        for (Parameter parameter : methodDoc.parameters()) {
            for (AnnotationDesc annotation : parameter.annotations()) {
                if (getAnnotationName(annotation).equals(PATHVAR_ANNOTATION)) {
                    String name = parameter.name();
                    List<String> values = getElementValue(annotation, "value");
                    if (!values.isEmpty())
                        name = values.get(0);

                    //first check for special tag, then check regular param tag, finally default to empty string
                    String text = findParamText(tags, name);
                    if (text == null)
                        text = findParamText(paramTags, parameter.name());
                    if (text == null)
                        text = "";

                    retVal.add(new PathVariableDescriptor(name, text));
                }
            }
        }

        return retVal;
    }

    @Override
    protected Collection<QueryParamDescriptor> generateQueryParams(MethodDoc methodDoc) {
        Collection<QueryParamDescriptor> retVal = new ArrayList<QueryParamDescriptor> ();

        Tag[] tags = methodDoc.tags(QUERYPARAM_TAG);
        ParamTag[] paramTags = methodDoc.paramTags();

        for (Parameter parameter : methodDoc.parameters()) {
            for (AnnotationDesc annotation : parameter.annotations()) {
                if (getAnnotationName(annotation).equals(PARAM_ANNOTATION)) {
                    String name = parameter.name();
                    List<String> values = getElementValue(annotation, "value");
                    if (!values.isEmpty())
                        name = values.get(0);

                    //first check for special tag, then check regular param tag, finally default to empty string
                    String text = findParamText(tags, name);
                    if (text == null)
                        text = findParamText(paramTags, parameter.name());
                    if (text == null)
                        text = "";

                    retVal.add(new QueryParamDescriptor(name, false, text));
                }
            }
        }
        return retVal;
    }

    @Override
    protected Collection<String> resolveHttpMethods(EndpointMapping classMapping, EndpointMapping methodMapping) {
        //Only methods should have http methods.
        return methodMapping.getHttpMethods();
    }
}
