package restdoclet.writer;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.Type;
import restdoclet.Configuration;
import restdoclet.model.ClassDescriptor;
import restdoclet.model.EndpointDescriptor;
import restdoclet.model.PathVariableDescriptor;
import restdoclet.model.QueryParamDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Map.Entry;
import static restdoclet.util.CommonUtils.*;

public class SwaggerWriter implements Writer{
    public static final String OUTPUT_OPTION_NAME = "swagger";
    private static final String SWAGGER_UI_ARTIFACT = "swagger-ui.zip";
    private static final String SWAGGER_VERSION = "1.2";
    private static final String RESOURCE_DOC = "./api-docs";
    private static final String API_DOC_DIR = "apis";
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public void write(Collection<ClassDescriptor> classDescriptors, Configuration config) {

        Map<String, Collection<ClassDescriptor>> apis = new LinkedHashMap<String, Collection<ClassDescriptor>>();
        for (ClassDescriptor classDescriptor : classDescriptors) {
            String api = longestCommonPrefix(classDescriptor);
            if (apis.containsKey(api)) {
                apis.get(api).add(classDescriptor);
            } else {
                Collection<ClassDescriptor> tmp = new ArrayList<ClassDescriptor>();
                tmp.add(classDescriptor);
                apis.put(api, tmp);
            }
        }
        writeResource(apis, config);
        copySwagger();
    }

    private static void copySwagger() {

        ZipInputStream swaggerZip = null;
        FileOutputStream out = null;
        try{
            swaggerZip = new ZipInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(SWAGGER_UI_ARTIFACT));
            ZipEntry entry;
            while ((entry = swaggerZip.getNextEntry()) != null) {
                final File swaggerFile = new File("./", entry.getName());
                if (entry.isDirectory()) {
                    if (!swaggerFile.isDirectory() && !swaggerFile.mkdirs()) {
                        throw new RuntimeException("Unable to create directory: " + swaggerFile);
                    }
                } else {
                    copy(swaggerZip, new FileOutputStream(swaggerFile));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(swaggerZip);
            closeQuietly(out);
        }

    }

    private static void writeResource(Map<String, Collection<ClassDescriptor>> apis, Configuration config) {

        JsonGenerator generator = null;

        try {
            generator = mapper.getFactory().createGenerator(new FileOutputStream(RESOURCE_DOC)).useDefaultPrettyPrinter();
            generator.writeStartObject();
            generator.writeStringField("swaggerVersion", SWAGGER_VERSION);
            if (config.getApiVersion() != null)
                generator.writeStringField("apiVersion", config.getApiVersion());

            generator.writeArrayFieldStart("apis");
            for (Entry<String, Collection<ClassDescriptor>> entry : apis.entrySet()) {
                generator.writeStartObject();
                generator.writeStringField("path", "/../" + API_DOC_DIR + entry.getKey());
                generator.writeStringField("description", getDescription(entry.getValue()));
                generator.writeEndObject();

                writeApi(entry.getKey(), entry.getValue(), config);
            }
            generator.writeEndArray();
            generator.writeObjectFieldStart("info");
            generator.writeStringField("title", config.getDocumentTitle());
            generator.writeEndObject();
            generator.writeEndObject();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(generator);
        }
    }

    private static void writeApi(String path, Collection<ClassDescriptor> descriptors, Configuration config) {
        JsonGenerator generator = null;
        Map<String, Collection<EndpointDescriptor>> pathGroups = groupPaths(descriptors);

        try {
            new File("./" + API_DOC_DIR + path).getParentFile().mkdirs();
            generator = mapper.getFactory().createGenerator(new FileOutputStream("./" + API_DOC_DIR + path)).useDefaultPrettyPrinter();
            generator.writeStartObject();
            generator.writeStringField("swaggerVersion", SWAGGER_VERSION);
            generator.writeStringField("basePath", config.getBasePath());
            generator.writeStringField("resourcePath", path);
            if (config.getApiVersion() != null)
                generator.writeStringField("apiVersion", config.getApiVersion());

            generator.writeArrayFieldStart("apis");
            for (Entry<String, Collection<EndpointDescriptor>> entry : pathGroups.entrySet()) {
                generator.writeStartObject();
                generator.writeStringField("path", entry.getKey());
                generator.writeStringField("description", "");
                generator.writeArrayFieldStart("operations");
                for (EndpointDescriptor endpoint : entry.getValue()) {
                    writeEndpoint(endpoint, generator);
                }
                generator.writeEndArray();
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.writeEndObject();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(generator);
        }
    }

    private static void writeEndpoint(EndpointDescriptor endpoint, JsonGenerator generator) throws IOException {
        String returnType = dataType(endpoint.getType());

        generator.writeStartObject();
        generator.writeStringField("httpMethod", endpoint.getHttpMethod());
        generator.writeStringField("nickname", "nickname");
        generator.writeStringField("summary", endpoint.getDescription());
        generator.writeStringField("notes", endpoint.getDescription());
        if (returnType != null)
            generator.writeStringField("responseClass", returnType);

        generator.writeArrayFieldStart("produces");
        if (!isEmpty(endpoint.getProduces())) {
            for (String produces : endpoint.getProduces())
                generator.writeString(produces);

        } else {
            generator.writeString("text/plain");    //default to text/plain if we don't know the type.
        }
        generator.writeEndArray();

        if (!isEmpty(endpoint.getConsumes())) {
            generator.writeArrayFieldStart("consumes");
            for (String consumes : endpoint.getConsumes()) {
                generator.writeString(consumes);
            }
            generator.writeEndArray();
        }
        generator.writeArrayFieldStart("parameters");
        for (PathVariableDescriptor pathVar : endpoint.getPathVars()) {
            generator.writeStartObject();
            generator.writeStringField("paramType", "path");
            generator.writeStringField("name", pathVar.getName());
            generator.writeStringField("description", pathVar.getDescription());
            generator.writeStringField("dataType", basicType(pathVar.getType()));
            generator.writeBooleanField("required", true);
            generator.writeBooleanField("allowMultiple", false);
            generator.writeEndObject();
        }
        for (QueryParamDescriptor queryParam : endpoint.getQueryParams()) {
            //If it is a container type then allow multiple but use the underlying type.
            boolean container = isContainer(queryParam.getType());
            String type = (container ? internalContainerType(queryParam.getType()) : basicType(queryParam.getType()));

            generator.writeStartObject();
            generator.writeStringField("paramType", "query");
            generator.writeStringField("name", queryParam.getName());
            generator.writeStringField("description", queryParam.getDescription());
            generator.writeStringField("dataType", type);
            generator.writeBooleanField("required", queryParam.isRequired());
            generator.writeBooleanField("allowMultiple", container);
            generator.writeEndObject();
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    private static String getDescription(Collection<ClassDescriptor> descriptors) {
        StringBuilder sb = new StringBuilder();
        for (ClassDescriptor descriptor : descriptors)
            sb.append(descriptor.getDescription()).append(" ");

        return sb.toString().trim();
    }

    private static String longestCommonPrefix(ClassDescriptor classDescriptor) {

        if (isEmpty(classDescriptor.getEndpoints()))
            return "/";

        List<String> paths = new ArrayList<String>(classDescriptor.getEndpoints().size());
        for (EndpointDescriptor endpoint : classDescriptor.getEndpoints())
            paths.add(endpoint.getPath());

        Collections.sort(paths);
        String longest = paths.get(0);

        for (int i = 1;i< paths.size();i++) {
            for (int j = 0;j< longest.length();j++) {
                if (paths.get(i).charAt(j) != longest.charAt(j)) {
                    longest = longest.substring(0, j - 1);
                }
            }
        }

        return fixPath(longest);
    }

    private static Map<String, Collection<EndpointDescriptor>> groupPaths (Collection<ClassDescriptor> classDescriptors) {
        Map<String, Collection<EndpointDescriptor>> paths = new LinkedHashMap<String, Collection<EndpointDescriptor>>();
        for (ClassDescriptor classDescriptor : classDescriptors) {
            for (EndpointDescriptor endpoint : classDescriptor.getEndpoints()) {
                if (paths.containsKey(endpoint.getPath())) {
                    paths.get(endpoint.getPath()).add(endpoint);
                } else {
                    Collection<EndpointDescriptor> tmp = new ArrayList<EndpointDescriptor>();
                    tmp.add(endpoint);
                    paths.put(endpoint.getPath(), tmp);
                }
            }
        }

        return paths;
    }

    private static String dataType(Type type) {
        if (type == null)
            return null;

        if (isContainer(type)) {
            //treat sets as sets
            if (isSub(type.asClassDoc(), Set.class))
                return "Set[" + internalContainerType(type) + "]";

            return "List[" + internalContainerType(type) + "]";
        }

        //Treat as a basic type.
        return basicType(type);
    }

    private static boolean isContainer(Type type) {

        //first check for arrays
        if (type.dimension() != null && !type.dimension().isEmpty())
            return true;

        //treat iterables as lists
        if (isSub(type.asClassDoc(), Iterable.class))
            return true;

        return false;
    }

    private static String internalContainerType(Type type) {
        //treat arrays first
        if (type.dimension() != null && !type.dimension().isEmpty())
            return basicType(type);

        ParameterizedType pType = type.asParameterizedType();
        if (pType != null) {
            Type[] paramTypes = ((ParameterizedType)type).typeArguments();
            if (!isEmpty(paramTypes))
                return basicType(paramTypes[0]);
        }

        return "Object";
    }

    private static String basicType(Type type) {
        if (type == null)
            return "void";

        //next primitives
        if (type.isPrimitive())
            return type.qualifiedTypeName();

        String name = type.qualifiedTypeName();

        if (name.equals(Byte.class.getName()))
            return "byte";

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

        if (name.equals(String.class.getName()))
            return "string";

        if (name.equals(Date.class.getName()))
            return "Date";

        return "Object";
    }

    private static <T> boolean isSub (ClassDoc classDoc, Class<T> targetClazz) {
        if (classDoc == null)
            return false;

        if (classDoc.qualifiedTypeName().equals(targetClazz.getName()))
            return true;

        if (isSub(classDoc.superclass(), targetClazz))
            return true;

        for (ClassDoc iface : classDoc.interfaces())
            if (isSub(iface, targetClazz))
                return true;


        return false;
    }
}
