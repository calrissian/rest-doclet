package restdoclet.collector.spring;


import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import restdoclet.Configuration;
import restdoclet.collector.AbstractCollector;
import restdoclet.model.EndpointDescriptor;
import restdoclet.model.PathVariableDescriptor;
import restdoclet.model.QueryParamDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static restdoclet.collector.spring.CollectorUtils.*;
import static restdoclet.util.AnnotationUtil.getAnnotationName;
import static restdoclet.util.CommonUtils.isEmpty;
import static restdoclet.util.TagUtils.IGNORE_TAG;

public class SpringCollector extends AbstractCollector {

    @Override
    protected boolean shouldIgnoreClass(ClassDoc classDoc, Configuration config) {

        //If found a controller annotation then don't ignore this class.
        for (AnnotationDesc classAnnotation : classDoc.annotations())
            if (CONTROLLER_ANNOTATION.equals(getAnnotationName(classAnnotation)))
                return false;

        //If not found then ignore this class.
        return true;
    }

    @Override
    protected Collection<EndpointDescriptor> getEndpoints(String contextPath, ClassDoc classDoc, Configuration config) {
        return getEndpoints(contextPath, classDoc, getRequestMappingAnnotation(classDoc));
    }

    protected Collection<EndpointDescriptor> getEndpoints(String contextPath, ClassDoc classDoc, RequestMappingAnnotation classMapping) {
        Collection<EndpointDescriptor> endpointDescriptors = new ArrayList<EndpointDescriptor>();

        for (MethodDoc method : classDoc.methods(true)) {
            for (AnnotationDesc annotation : method.annotations())
                if (MAPPING_ANNOTATION.equals(getAnnotationName(annotation)))
                    endpointDescriptors.addAll(getSingleEndpoint(contextPath, classMapping, method));
        }

        //Check super classes for inherited request mappings
        if (classDoc.superclass() != null)
            endpointDescriptors.addAll(getEndpoints(contextPath, classDoc.superclass(), classMapping));

        return endpointDescriptors;
    }

    /**
     * This will return the set of endpoints that this particular method can point to.
     * @param contextPath
     * @param classMappings
     * @param method
     * @return
     */
    protected Collection<EndpointDescriptor> getSingleEndpoint(String contextPath, RequestMappingAnnotation classMappings, MethodDoc method) {

        //If the ignore tag is present then simply return nothing for this endpoint.
        if (!isEmpty(method.tags(IGNORE_TAG)))
            return Collections.emptyList();

        Collection<EndpointDescriptor> endpointDescriptors = new ArrayList<EndpointDescriptor>();
        RequestMappingAnnotation methodMapping = getRequestMappingAnnotation(method);


        Collection<String> paths = generatePaths(contextPath, classMappings, methodMapping);
        Collection<PathVariableDescriptor> pathVars = generatePathVars(method);
        Collection<QueryParamDescriptor> queryParams = generateQueryParams(method);

        for (String httpMethod : getHttpMethods(classMappings, methodMapping))
            for (String path : paths)
                endpointDescriptors.add(
                        new EndpointDescriptor(
                                path,
                                httpMethod,
                                queryParams,
                                pathVars,
                                method.commentText()
                        )
                );

        return endpointDescriptors;
    }










}
