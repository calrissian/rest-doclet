package restdoclet.writer.swagger;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javadoc.Type;
import restdoclet.Configuration;
import restdoclet.model.ClassDescriptor;
import restdoclet.model.Endpoint;
import restdoclet.model.PathVar;
import restdoclet.model.QueryParam;
import restdoclet.writer.Writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Map.Entry;
import static restdoclet.util.CommonUtils.*;
import static restdoclet.writer.swagger.TypeUtils.*;

public class SwaggerWriter implements Writer {
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
        Map<String, Collection<Endpoint>> pathGroups = groupPaths(descriptors);

        try {
            new File("./" + API_DOC_DIR + path).getParentFile().mkdirs();
            generator = mapper.getFactory().createGenerator(new FileOutputStream("./" + API_DOC_DIR + path)).useDefaultPrettyPrinter();
            generator.writeStartObject();
            generator.writeStringField("swaggerVersion", SWAGGER_VERSION);
            generator.writeStringField("basePath", config.getBaseUrl());
            generator.writeStringField("resourcePath", path);
            if (config.getApiVersion() != null)
                generator.writeStringField("apiVersion", config.getApiVersion());

            generator.writeArrayFieldStart("apis");
            for (Entry<String, Collection<Endpoint>> entry : pathGroups.entrySet()) {
                generator.writeStartObject();
                generator.writeStringField("path", entry.getKey());
                generator.writeStringField("description", "");
                generator.writeArrayFieldStart("operations");
                for (Endpoint endpoint : entry.getValue()) {
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

    private static void writeEndpoint(Endpoint endpoint, JsonGenerator generator) throws IOException {
        String returnType = dataType(endpoint.getType());

        generator.writeStartObject();
        generator.writeStringField("httpMethod", endpoint.getHttpMethod());
        generator.writeStringField("nickname", "nickname");
        generator.writeStringField("summary", endpoint.getDescription());
        generator.writeStringField("notes", endpoint.getDescription());
        if (returnType != null)
            generator.writeStringField("responseClass", returnType);

        if (!isEmpty(endpoint.getProduces())) {
            generator.writeArrayFieldStart("produces");
            for (String produces : endpoint.getProduces())
                generator.writeString(produces);

            generator.writeEndArray();
        }
        if (!isEmpty(endpoint.getConsumes())) {
            generator.writeArrayFieldStart("consumes");
            for (String consumes : endpoint.getConsumes()) {
                generator.writeString(consumes);
            }
            generator.writeEndArray();
        }

        generator.writeArrayFieldStart("parameters");
        for (PathVar pathVar : endpoint.getPathVars()) {
            generator.writeStartObject();
            generator.writeStringField("paramType", "path");
            generator.writeStringField("name", pathVar.getName());
            generator.writeStringField("description", pathVar.getDescription());
            generator.writeStringField("dataType", basicType(pathVar.getType()));
            generator.writeBooleanField("required", true);
            generator.writeBooleanField("allowMultiple", false);

            writeAllowableValues(pathVar.getType(), generator);

            generator.writeEndObject();
        }
        for (QueryParam queryParam : endpoint.getQueryParams()) {
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

            writeAllowableValues(queryParam.getType(), generator);

            generator.writeEndObject();
        }
        if (endpoint.getRequestBody() != null) {
            generator.writeStartObject();
            generator.writeStringField("paramType", "body");
            generator.writeStringField("name", endpoint.getRequestBody().getName());
            generator.writeStringField("description", endpoint.getRequestBody().getDescription());
            generator.writeStringField("dataType", dataType(endpoint.getRequestBody().getType()));
            generator.writeBooleanField("required", true);
            generator.writeBooleanField("allowMultiple", false);

            writeAllowableValues(endpoint.getRequestBody().getType(), generator);

            generator.writeEndObject();
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    private static void writeAllowableValues(Type type, JsonGenerator generator) throws IOException {

        Collection<String> values = allowableValues(type);
        if (!isEmpty(values)) {
            generator.writeObjectFieldStart("allowableValues");
            generator.writeStringField("valueType" , "List");
            generator.writeArrayFieldStart("values");
            for (String field : values) {
                generator.writeString(field);
            }
            generator.writeEndArray();
            generator.writeEndObject();
        }
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
        for (Endpoint endpoint : classDescriptor.getEndpoints())
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

    private static Map<String, Collection<Endpoint>> groupPaths (Collection<ClassDescriptor> classDescriptors) {
        Map<String, Collection<Endpoint>> paths = new LinkedHashMap<String, Collection<Endpoint>>();
        for (ClassDescriptor classDescriptor : classDescriptors) {
            for (Endpoint endpoint : classDescriptor.getEndpoints()) {
                if (paths.containsKey(endpoint.getPath())) {
                    paths.get(endpoint.getPath()).add(endpoint);
                } else {
                    Collection<Endpoint> tmp = new ArrayList<Endpoint>();
                    tmp.add(endpoint);
                    paths.put(endpoint.getPath(), tmp);
                }
            }
        }

        return paths;
    }


}
