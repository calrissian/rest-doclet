package restdoclet.collector.jaxrs;

import com.sun.javadoc.*;
import restdoclet.collector.EndpointMapping;
import restdoclet.model.PathVariableDescriptor;
import restdoclet.model.QueryParamDescriptor;

import java.util.*;

import static restdoclet.util.AnnotationUtils.getAnnotationName;
import static restdoclet.util.AnnotationUtils.getElementValue;
import static restdoclet.util.TagUtils.*;

public class JaxRSCollectorUtils {
    private JaxRSCollectorUtils() {}

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

    protected static EndpointMapping getEndpointMapping(ProgramElementDoc doc) {

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
}
