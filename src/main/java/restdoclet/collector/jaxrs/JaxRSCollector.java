package restdoclet.collector.jaxrs;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import restdoclet.Configuration;
import restdoclet.collector.AbstractCollector;
import restdoclet.collector.EndpointMapping;
import restdoclet.model.EndpointDescriptor;
import restdoclet.model.PathVariableDescriptor;
import restdoclet.model.QueryParamDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static restdoclet.collector.jaxrs.JaxRSCollectorUtils.*;
import static restdoclet.util.AnnotationUtils.getAnnotationName;
import static restdoclet.util.CommonUtils.firstNonEmpty;
import static restdoclet.util.CommonUtils.isEmpty;
import static restdoclet.util.TagUtils.IGNORE_TAG;

public class JaxRSCollector extends AbstractCollector{
    @Override
    protected boolean shouldIgnoreClass(ClassDoc classDoc, Configuration config) {

        //Look for any JAXRS annotations in the class or the methods.  If found then don't ignore this class.
        for (AnnotationDesc classAnnotation : classDoc.annotations()) {
            String annotationName = getAnnotationName(classAnnotation);
            if (annotationName != null && annotationName.startsWith(ANNOTATION_PACKAGE))
                return false;

        }

        for (MethodDoc methodDoc : classDoc.methods(true)) {
            for (AnnotationDesc methodAnnotation : methodDoc.annotations()) {
                String annotationName = getAnnotationName(methodAnnotation);
                if (annotationName != null && annotationName.startsWith(ANNOTATION_PACKAGE))
                    return false;

            }
        }

        return true;
    }

    @Override
    protected Collection<EndpointDescriptor> getEndpoints(String contextPath, ClassDoc classDoc, Configuration config) {
        return getEndpoints(contextPath, classDoc, getEndpointMapping(classDoc));
    }

    protected Collection<EndpointDescriptor> getEndpoints(String contextPath, ClassDoc classDoc, EndpointMapping classMapping) {
        Collection<EndpointDescriptor> endpointDescriptors = new ArrayList<EndpointDescriptor>();

        for (MethodDoc method : classDoc.methods(true))
            endpointDescriptors.addAll(getSingleEndpoint(contextPath, classMapping, method));


        //Check super classes for inherited request mappings
        if (classDoc.superclass() != null)
            endpointDescriptors.addAll(getEndpoints(contextPath, classDoc.superclass(), classMapping));

        return endpointDescriptors;
    }

    protected Collection<EndpointDescriptor> getSingleEndpoint(String contextPath, EndpointMapping classMapping, MethodDoc method) {

        //If the ignore tag is present then simply return nothing for this endpoint.
        if (!isEmpty(method.tags(IGNORE_TAG)))
            return Collections.emptyList();

        Collection<EndpointDescriptor> endpointDescriptors = new ArrayList<EndpointDescriptor>();
        EndpointMapping methodMapping = getEndpointMapping(method);

        Collection<String> paths = generatePaths(contextPath, classMapping, methodMapping);
        Collection<String> httpMethods = methodMapping.getHttpMethods();
        Collection<String> consumes = firstNonEmpty(methodMapping.getConsumes(), classMapping.getConsumes());
        Collection<String> produces = firstNonEmpty(methodMapping.getProduces(), classMapping.getProduces());
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
}
