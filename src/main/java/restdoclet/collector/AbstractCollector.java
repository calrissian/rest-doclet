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
package restdoclet.collector;


import com.sun.javadoc.*;
import restdoclet.model.ClassDescriptor;
import restdoclet.model.EndpointDescriptor;
import restdoclet.model.PathVariableDescriptor;
import restdoclet.model.QueryParamDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import static java.util.Collections.emptyList;
import static restdoclet.util.CommonUtils.firstNonEmpty;
import static restdoclet.util.CommonUtils.isEmpty;
import static restdoclet.util.TagUtils.*;

public abstract class AbstractCollector implements Collector {

    protected abstract boolean shouldIgnoreClass(ClassDoc classDoc);
    protected abstract boolean shouldIgnoreMethod(MethodDoc methodDoc);
    protected abstract EndpointMapping getEndpointMapping(ProgramElementDoc doc);
    protected abstract Collection<PathVariableDescriptor> generatePathVars(MethodDoc methodDoc);
    protected abstract Collection<QueryParamDescriptor> generateQueryParams(MethodDoc methodDoc);

    /**
     * Will generate and aggregate all the rest endpoint class descriptors.
     * @param rootDoc
     * @return
     */
    @Override
    public Collection<ClassDescriptor> getDescriptors(RootDoc rootDoc) {
        Collection<ClassDescriptor> classDescriptors = new ArrayList<ClassDescriptor>();

        //Loop through all of the classes and if it contains endpoints then add it to the set of descriptors.
        for (ClassDoc classDoc : rootDoc.classes()) {
            ClassDescriptor descriptor = getClassDescriptor(classDoc);
            if (descriptor != null && !isEmpty(descriptor.getEndpoints()))
                classDescriptors.add(descriptor);
        }

        return classDescriptors;
    }

    /**
     * Will generate a single class descriptor and all the endpoints for that class.
     *
     * If any class contains the special javadoc tag {@link restdoclet.util.TagUtils.IGNORE_TAG} it will be excluded.
     * @param classDoc
     * @return
     */
    protected ClassDescriptor getClassDescriptor(ClassDoc classDoc) {

        //If the ignore tag is present or this type of class should be ignored then simply ignore this class
        if (!isEmpty(classDoc.tags(IGNORE_TAG)) || shouldIgnoreClass(classDoc))
            return null;

        Collection<EndpointDescriptor> endpoints = getAllEndpointDescriptors(getContextPath(classDoc), classDoc, getEndpointMapping(classDoc));

        //If there are no endpoints then no use in providing documentation.
        if (isEmpty(endpoints))
            return null;

        String name = getClassName(classDoc);
        String description = getClassDescription(classDoc);

        return new ClassDescriptor(
                (name == null ? "" : name),
                endpoints,
                (description == null ? "" : description)
        );
    }

    /**
     * Retrieves all the end point descriptors provided in the specified class doc.
     * @param contextPath
     * @param classDoc
     * @param classMapping
     * @return
     */
    protected Collection<EndpointDescriptor> getAllEndpointDescriptors(String contextPath, ClassDoc classDoc, EndpointMapping classMapping) {
        Collection<EndpointDescriptor> endpointDescriptors = new ArrayList<EndpointDescriptor>();

        for (MethodDoc method : classDoc.methods(true))
            endpointDescriptors.addAll(getEndpointDescriptors(contextPath, classMapping, method));

        //Check super classes for inherited methods
        if (classDoc.superclass() != null)
            endpointDescriptors.addAll(getAllEndpointDescriptors(contextPath, classDoc.superclass(), classMapping));

        return endpointDescriptors;
    }

    /**
     * Retrieves the endpoint descriptors for a single method.
     *
     * If any method contains the special javadoc tag {@link restdoclet.util.TagUtils.IGNORE_TAG} it will be excluded.
     * @param contextPath
     * @param classMapping
     * @param method
     * @return
     */
    protected Collection<EndpointDescriptor> getEndpointDescriptors(String contextPath, EndpointMapping classMapping, MethodDoc method) {

        //If the ignore tag is present then simply return nothing for this endpoint.
        if (!isEmpty(method.tags(IGNORE_TAG)) || shouldIgnoreMethod(method))
            return emptyList();

        Collection<EndpointDescriptor> endpointDescriptors = new ArrayList<EndpointDescriptor>();
        EndpointMapping methodMapping = getEndpointMapping(method);

        Collection<String> paths = resolvePaths(contextPath, classMapping, methodMapping);
        Collection<String> httpMethods = resolveHttpMethods(classMapping, methodMapping);
        Collection<String> consumes = resolveConsumesInfo(classMapping, methodMapping);
        Collection<String> produces = resolvesProducesInfo(classMapping, methodMapping);
        Collection<PathVariableDescriptor> pathVars = generatePathVars(method);
        Collection<QueryParamDescriptor> queryParams = generateQueryParams(method);

        for (String httpMethod : httpMethods)
            for (String path : paths)
                endpointDescriptors.add(
                        new EndpointDescriptor(
                                path,
                                httpMethod,
                                queryParams,
                                pathVars,
                                consumes,
                                produces,
                                method.commentText()
                        )
                );

        return endpointDescriptors;
    }

    /**
     * Will get the initial context path to use for all rest endpoint.
     *
     * This looks for the value in a special javadoc tag {@link restdoclet.util.TagUtils.CONTEXT_TAG}
     *
     * @param classDoc
     * @return
     */
    protected String getContextPath(ClassDoc classDoc) {
        if(!isEmpty(classDoc.tags(CONTEXT_TAG))) {
            return classDoc.tags(CONTEXT_TAG)[0].text();
        }
        return "";
    }

    /**
     * Will get the display name for the class.
     *
     * This looks for the value in a special javadoc tag {@link restdoclet.util.TagUtils.NAME_TAG}
     *
     * @param classDoc
     * @return
     */
    protected String getClassName(ClassDoc classDoc) {
        Tag[] tags = classDoc.tags(NAME_TAG);
        String name;
        if (tags != null && tags.length > 0)
            return tags[0].text();
        else
            return classDoc.typeName();
    }

    /**
     * Will get the description for the class.
     * @param classDoc
     * @return
     */
    protected String getClassDescription(ClassDoc classDoc) {
        return classDoc.commentText();
    }

    /**
     * Will generate all the paths specified in the class and method mappings.
     * Each path should start with the context path, followed by one of the class paths,
     * then finally the method path.
     *
     * @param contextPath
     * @param classMapping
     * @param methodMapping
     * @return
     */
    protected Collection<String> resolvePaths(String contextPath, EndpointMapping classMapping, EndpointMapping methodMapping) {

        contextPath = (contextPath == null ? "" : contextPath);

        //Build all the paths based on the class level, plus the method extensions.
        LinkedHashSet<String> paths = new LinkedHashSet<String>();

        if (isEmpty(classMapping.getPaths())) {

            for (String path : methodMapping.getPaths())
                paths.add(contextPath + path);

        } else if (isEmpty(methodMapping.getPaths())) {

            for (String path : classMapping.getPaths())
                paths.add(contextPath + path);

        } else {

            for (String defaultPath : classMapping.getPaths())
                for (String path : methodMapping.getPaths())
                    paths.add(contextPath + defaultPath + path);

        }

        return paths;
    }

    /**
     * Will use the method's mapped information if it is not empty, otherwise it will use the class mapping information
     * to retrieve all the https methods.
     * @param classMapping
     * @param methodMapping
     * @return
     */
    protected Collection<String> resolveHttpMethods(EndpointMapping classMapping, EndpointMapping methodMapping) {
        return firstNonEmpty(
                methodMapping.getHttpMethods(),
                classMapping.getHttpMethods()
        );
    }

    /**
     * Will use the method's mapped information if it is not empty, otherwise it will use the class mapping information
     * to retrieve all the consumeable information.
     * @param classMapping
     * @param methodMapping
     * @return
     */
    protected Collection<String> resolveConsumesInfo(EndpointMapping classMapping, EndpointMapping methodMapping) {
        return firstNonEmpty(
                methodMapping.getConsumes(),
                classMapping.getConsumes()
        );
    }

    /**
     * Will use the method's mapped information if it is not empty, otherwise it will use the class mapping information
     * to retrieve all the produceable information.
     * @param classMapping
     * @param methodMapping
     * @return
     */
    protected Collection<String> resolvesProducesInfo(EndpointMapping classMapping, EndpointMapping methodMapping) {
        return firstNonEmpty(
                methodMapping.getProduces(),
                classMapping.getProduces()
        );
    }
}
