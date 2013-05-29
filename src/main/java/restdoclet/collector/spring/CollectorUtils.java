package restdoclet.collector.spring;

import com.sun.javadoc.*;
import restdoclet.model.PathVariableDescriptor;
import restdoclet.model.QueryParamDescriptor;

import java.util.*;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static restdoclet.util.AnnotationUtils.getAnnotationName;
import static restdoclet.util.AnnotationUtils.getElementValue;
import static restdoclet.util.CommonUtils.isEmpty;
import static restdoclet.util.TagUtils.*;

public class CollectorUtils {
    private CollectorUtils() {}

    protected static final String CONTROLLER_ANNOTATION = "org.springframework.stereotype.Controller";
    protected static final String MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping";
    protected static String PATHVAR_ANNOTATION = "org.springframework.web.bind.annotation.PathVariable";
    protected static String PARAM_ANNOTATION = "org.springframework.web.bind.annotation.RequestParam";

    protected static RequestMappingAnnotation getRequestMappingAnnotation(ProgramElementDoc doc) {

        //Look for a request mapping annotation
        for (AnnotationDesc annotation : doc.annotations()) {
            //If found then extract the value (paths) and the methods.
            if (MAPPING_ANNOTATION.equals(getAnnotationName(annotation))) {

                //Get paths from annotation
                Collection<String> paths = new LinkedHashSet<String>(getElementValue(annotation, "value"));

                //Get http methods from annotation
                Collection<String> httpMethods = new LinkedHashSet<String>();
                for (String value : getElementValue(annotation, "method"))
                    httpMethods.add(value.substring(value.lastIndexOf(".") + 1));

                return new RequestMappingAnnotation(paths, httpMethods);
            }
        }

        return null;
    }

    protected static Set<String> generatePaths(String contextPath, RequestMappingAnnotation classMapping, RequestMappingAnnotation methodMapping) {

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

    protected static Collection<String> getHttpMethods(RequestMappingAnnotation classMapping, RequestMappingAnnotation methodMapping) {

        //use methods from annotations.  If non specified use GET.
        Collection<String> methods = methodMapping.getHttpMethods();

        if (isEmpty(methods))
            methods = (isEmpty(classMapping.getHttpMethods()) ? asList("GET") : classMapping.getHttpMethods());

        return methods;
    }

    protected static Collection<PathVariableDescriptor> generatePathVars(MethodDoc methodDoc) {
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

    protected static Collection<QueryParamDescriptor> generateQueryParams(MethodDoc methodDoc) {
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

                    List<String> requiredVals = getElementValue(annotation, "required");

                    //With spring query params are required by default
                    boolean required = TRUE;
                    if(!requiredVals.isEmpty())
                        required = Boolean.parseBoolean(requiredVals.get(0));

                    //first check for special tag, then check regular param tag, finally default to empty string
                    String text = findParamText(tags, name);
                    if (text == null)
                        text = findParamText(paramTags, parameter.name());
                    if (text == null)
                        text = "";

                    retVal.add(new QueryParamDescriptor(name, required, text));
                }
            }
        }
        return retVal;
    }

    protected static class RequestMappingAnnotation{
        private final Collection<String> paths;
        private final Collection<String> httpMethods;

        protected RequestMappingAnnotation(Collection<String> paths, Collection<String> httpMethods) {
            this.paths = paths;
            this.httpMethods = httpMethods;
        }

        protected Collection<String> getPaths() {
            return paths;
        }

        protected Collection<String> getHttpMethods() {
            return httpMethods;
        }
    }}
