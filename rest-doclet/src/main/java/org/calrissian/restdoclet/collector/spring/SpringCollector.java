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
package org.calrissian.restdoclet.collector.spring;


import com.sun.javadoc.*;
import org.calrissian.restdoclet.collector.AbstractCollector;
import org.calrissian.restdoclet.collector.EndpointMapping;
import org.calrissian.restdoclet.model.PathVar;
import org.calrissian.restdoclet.model.QueryParam;
import org.calrissian.restdoclet.model.RequestBody;

import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static org.calrissian.restdoclet.util.AnnotationUtils.getAnnotationName;
import static org.calrissian.restdoclet.util.AnnotationUtils.getElementValue;
import static org.calrissian.restdoclet.util.CommonUtils.firstNonEmpty;
import static org.calrissian.restdoclet.util.CommonUtils.isEmpty;
import static org.calrissian.restdoclet.util.TagUtils.*;

public class SpringCollector extends AbstractCollector {

    protected static final List<String> CONTROLLER_ANNOTATION = Arrays.asList("org.springframework.stereotype.Controller",
                                                                        "org.springframework.web.bind.annotation.RestController");
    protected static final String MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping";
    protected static final String PATHVAR_ANNOTATION = "org.springframework.web.bind.annotation.PathVariable";
    protected static final String PARAM_ANNOTATION = "org.springframework.web.bind.annotation.RequestParam";
    protected static final String REQUESTBODY_ANNOTATION = "org.springframework.web.bind.annotation.RequestBody";

    @Override
    protected boolean shouldIgnoreClass(ClassDoc classDoc) {
        //If found a controller annotation then don't ignore this class.
        for (AnnotationDesc classAnnotation : classDoc.annotations())
            if (CONTROLLER_ANNOTATION.contains(getAnnotationName(classAnnotation)))
                return false;

        //If not found then ignore this class.
        return true;
    }

    @Override
    protected boolean shouldIgnoreMethod(MethodDoc methodDoc) {
        //If found a mapping annotation then don't ignore this class.
        for (AnnotationDesc classAnnotation : methodDoc.annotations())
            if (MAPPING_ANNOTATION.equals(getAnnotationName(classAnnotation)))
                return false;

        //If not found then ignore this class.
        return true;
    }

    @Override
    protected EndpointMapping getEndpointMapping(ProgramElementDoc doc) {
        //Look for a request mapping annotation
        for (AnnotationDesc annotation : doc.annotations()) {
            //If found then extract the value (paths) and the methods.
            if (MAPPING_ANNOTATION.equals(getAnnotationName(annotation))) {

                //Get http methods from annotation
                Collection<String> httpMethods = new LinkedHashSet<String>();
                for (String value : getElementValue(annotation, "method"))
                    httpMethods.add(value.substring(value.lastIndexOf(".") + 1));

                return new EndpointMapping(
                        new LinkedHashSet<String>(getElementValue(annotation, "value")),
                        httpMethods,
                        new LinkedHashSet<String>(getElementValue(annotation, "consumes")),
                        new LinkedHashSet<String>(getElementValue(annotation, "produces"))
                );
            }
        }

        //Simply return an empty grouping if no request mapping was found.
        return new EndpointMapping(
                Collections.<String>emptySet(),
                Collections.<String>emptySet(),
                Collections.<String>emptySet(),
                Collections.<String>emptySet()
        );
    }

    @Override
    protected Collection<PathVar> generatePathVars(MethodDoc methodDoc) {
        Collection<PathVar> retVal = new ArrayList<PathVar>();

        Tag[] tags = methodDoc.tags(PATHVAR_TAG);
        ParamTag[] paramTags = methodDoc.paramTags();

        for (Parameter parameter : methodDoc.parameters()) {
            for (AnnotationDesc annotation : parameter.annotations()) {
                if (getAnnotationName(annotation).equals(PATHVAR_ANNOTATION)) {
                    String name = parameter.name();
                    Collection<String> values = getElementValue(annotation, "value");
                    if (!values.isEmpty())
                        name = values.iterator().next();

                    //first check for special tag, then check regular param tag, finally default to empty string
                    String text = findParamText(tags, name);
                    if (text == null)
                        text = findParamText(paramTags, parameter.name());
                    if (text == null)
                        text = "";

                    retVal.add(new PathVar(name, text, parameter.type()));
                }
            }
        }

        return retVal;
    }

    @Override
    protected Collection<QueryParam> generateQueryParams(MethodDoc methodDoc) {
        Collection<QueryParam> retVal = new ArrayList<QueryParam> ();

        Tag[] tags = methodDoc.tags(QUERYPARAM_TAG);
        ParamTag[] paramTags = methodDoc.paramTags();

        for (Parameter parameter : methodDoc.parameters()) {
            for (AnnotationDesc annotation : parameter.annotations()) {
                if (getAnnotationName(annotation).equals(PARAM_ANNOTATION)) {
                    String name = parameter.name();
                    List<String> values = getElementValue(annotation, "value");
                    if (!values.isEmpty())
                        name = values.get(0);

                    List<String> requiredVals = getElementValue(annotation, "required");

                    //With spring query params are required by default
                    boolean required = TRUE;
                    if(!requiredVals.isEmpty())
                        required = Boolean.parseBoolean(requiredVals.get(0));

                    //With spring, if defaultValue is provided then "required" is set to false automatically
                    List<String> defaultVals = getElementValue(annotation, "defaultValue");

                    if (!defaultVals.isEmpty()) 
                        required = FALSE;

                    //first check for special tag, then check regular param tag, finally default to empty string
                    String text = findParamText(tags, name);
                    if (text == null)
                        text = findParamText(paramTags, parameter.name());
                    if (text == null)
                        text = "";

                    retVal.add(new QueryParam(name, required, text, parameter.type()));
                }
            }
        }
        return retVal;
    }

    @Override
    protected RequestBody generateRequestBody(MethodDoc methodDoc) {

        Tag[] tags = methodDoc.tags(REQUESTBODY_TAG);
        ParamTag[] paramTags = methodDoc.paramTags();

        for (Parameter parameter : methodDoc.parameters()) {
            for (AnnotationDesc annotation : parameter.annotations()) {
                if (getAnnotationName(annotation).equals(REQUESTBODY_ANNOTATION)) {

                    //first check for special tag, then check regular param tag, finally default to empty string
                    String text = (isEmpty(tags) ? null : tags[0].text());
                    if (text == null)
                        text = findParamText(paramTags, parameter.name());
                    if (text == null)
                        text = "";

                    return new RequestBody(parameter.name(), text, parameter.type());
                }
            }
        }
        return null;
    }

    @Override
    protected Collection<String> resolveHttpMethods(EndpointMapping classMapping, EndpointMapping methodMapping) {
        //If there are no http methods defined simply use GET
        return firstNonEmpty(super.resolveHttpMethods(classMapping, methodMapping), asList("GET"));
    }
}
