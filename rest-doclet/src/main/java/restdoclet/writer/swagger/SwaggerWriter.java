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

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Map.Entry;
import static restdoclet.util.CommonUtils.*;
import static restdoclet.writer.swagger.TypeUtils.*;

public class SwaggerWriter implements Writer {
    public static final String OUTPUT_OPTION_NAME = "swagger";

    private static final String SWAGGER_DEFAULT_HTML = "swagger/index.html";
    private static final String SWAGGER_CALLABLE_HTML = "swagger/index-callable.html";
    private static final String SWAGGER_UI_ARTIFACT = "swagger/swagger-ui.zip";
    private static final String SWAGGER_VERSION = "1.2";
    private static final String RESOURCE_DOC = "./api-docs";
    private static final String API_DOC_DIR = "apis";
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public void write(Collection<ClassDescriptor> classDescriptors, Configuration config) {

        Map<String, Collection<Endpoint>> resources = new LinkedHashMap<String, Collection<Endpoint>>();
        for (ClassDescriptor classDescriptor : classDescriptors) {
            for (Endpoint endpoint : classDescriptor.getEndpoints()) {
                String resourceName = getResource(classDescriptor.getContextPath(), endpoint);
                if (resources.containsKey(resourceName)) {
                    resources.get(resourceName).add(endpoint);
                } else {
                    Collection<Endpoint> tmp = new ArrayList<Endpoint>();
                    tmp.add(endpoint);
                    resources.put(resourceName, tmp);
                }
            }
        }

        writeResource(resources, config);
        copyIndex(config);
        copySwagger();
    }

    private static void copyIndex(Configuration config) {
        InputStream in = null;
        OutputStream out = null;
        try {

            if (config.hasUrl())
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(SWAGGER_CALLABLE_HTML);
            else
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(SWAGGER_DEFAULT_HTML);

            out = new FileOutputStream(new File(".", "index.html"));
            copy(in, out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    private static void copySwagger() {
        ZipInputStream swaggerZip = null;
        FileOutputStream out = null;
        try{
            swaggerZip = new ZipInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(SWAGGER_UI_ARTIFACT));
            ZipEntry entry;
            while ((entry = swaggerZip.getNextEntry()) != null) {
                final File swaggerFile = new File(".", entry.getName());
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

    private static void writeResource(Map<String, Collection<Endpoint>> resources, Configuration config) {

        JsonGenerator gen = null;

        try {
            gen = mapper.getFactory().createGenerator(new FileOutputStream(RESOURCE_DOC)).useDefaultPrettyPrinter();
            gen.writeStartObject();
            gen.writeStringField("swaggerVersion", SWAGGER_VERSION);
            if (config.getApiVersion() != null)
                gen.writeStringField("apiVersion", config.getApiVersion());

            gen.writeArrayFieldStart("apis");
            for (Entry<String, Collection<Endpoint>> entry : resources.entrySet()) {
                gen.writeStartObject();
                gen.writeStringField("path", "/../" + API_DOC_DIR + entry.getKey());
                gen.writeStringField("description", "");
                gen.writeEndObject();

                writeApi(entry.getKey(), entry.getValue(), config);
            }
            gen.writeEndArray();
            gen.writeObjectFieldStart("info");
            gen.writeStringField("title", config.getDocumentTitle());
            gen.writeEndObject();
            gen.writeEndObject();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(gen);
        }
    }

    private static void writeApi(String resource, Collection<Endpoint> endpoints, Configuration config) {
        JsonGenerator gen = null;
        Map<String, Collection<Endpoint>> pathGroups = groupPaths(endpoints);

        try {
            File apiFile = new File("./" + API_DOC_DIR , resource);
            apiFile.getParentFile().mkdirs();
            gen = mapper.getFactory().createGenerator(new FileOutputStream(apiFile)).useDefaultPrettyPrinter();
            gen.writeStartObject();
            gen.writeStringField("swaggerVersion", SWAGGER_VERSION);
            gen.writeStringField("basePath", config.getUrl());
            gen.writeStringField("resourcePath", resource);
            if (config.getApiVersion() != null)
                gen.writeStringField("apiVersion", config.getApiVersion());

            gen.writeArrayFieldStart("apis");
            for (Entry<String, Collection<Endpoint>> entry : pathGroups.entrySet()) {
                gen.writeStartObject();
                gen.writeStringField("path", entry.getKey());
                gen.writeStringField("description", "");
                gen.writeArrayFieldStart("operations");
                for (Endpoint endpoint : entry.getValue()) {
                    writeEndpoint(endpoint, gen);
                }
                gen.writeEndArray();
                gen.writeEndObject();
            }
            gen.writeEndArray();
            gen.writeEndObject();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(gen);
        }
    }

    private static void writeEndpoint(Endpoint endpoint, JsonGenerator gen) throws IOException {
        String returnType = dataType(endpoint.getType());

        gen.writeStartObject();
        gen.writeStringField("httpMethod", endpoint.getHttpMethod());
        gen.writeStringField("nickname", "nickname");
        gen.writeStringField("notes", endpoint.getShortDescription());
        gen.writeStringField("summary", endpoint.getDescription());
        if (returnType != null)
            gen.writeStringField("responseClass", returnType);

        if (!isEmpty(endpoint.getProduces())) {
            gen.writeArrayFieldStart("produces");
            for (String produces : endpoint.getProduces())
                gen.writeString(produces);

            gen.writeEndArray();
        }
        if (!isEmpty(endpoint.getConsumes())) {
            gen.writeArrayFieldStart("consumes");
            for (String consumes : endpoint.getConsumes()) {
                gen.writeString(consumes);
            }
            gen.writeEndArray();
        }

        gen.writeArrayFieldStart("parameters");
        for (PathVar pathVar : endpoint.getPathVars()) {
            gen.writeStartObject();
            gen.writeStringField("paramType", "path");
            gen.writeStringField("name", pathVar.getName());
            gen.writeStringField("description", pathVar.getDescription());
            gen.writeStringField("dataType", basicType(pathVar.getType()));
            gen.writeBooleanField("required", true);
            gen.writeBooleanField("allowMultiple", false);

            writeAllowableValues(pathVar.getType(), gen);

            gen.writeEndObject();
        }
        for (QueryParam queryParam : endpoint.getQueryParams()) {
            //If it is a container type then allow multiple but use the underlying type.
            boolean container = isContainer(queryParam.getType());
            String type = (container ? internalContainerType(queryParam.getType()) : basicType(queryParam.getType()));

            gen.writeStartObject();
            gen.writeStringField("paramType", "query");
            gen.writeStringField("name", queryParam.getName());
            gen.writeStringField("description", queryParam.getDescription());
            gen.writeStringField("dataType", type);
            gen.writeBooleanField("required", queryParam.isRequired());
            gen.writeBooleanField("allowMultiple", container);

            writeAllowableValues(queryParam.getType(), gen);

            gen.writeEndObject();
        }
        if (endpoint.getRequestBody() != null) {
            gen.writeStartObject();
            gen.writeStringField("paramType", "body");
            gen.writeStringField("name", endpoint.getRequestBody().getName());
            gen.writeStringField("description", endpoint.getRequestBody().getDescription());
            gen.writeStringField("dataType", dataType(endpoint.getRequestBody().getType()));
            gen.writeBooleanField("required", true);
            gen.writeBooleanField("allowMultiple", false);

            writeAllowableValues(endpoint.getRequestBody().getType(), gen);

            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private static void writeAllowableValues(Type type, JsonGenerator gen) throws IOException {

        Collection<String> values = allowableValues(type);
        if (!isEmpty(values)) {
            gen.writeObjectFieldStart("allowableValues");
            gen.writeStringField("valueType", "List");
            gen.writeArrayFieldStart("values");
            for (String field : values) {
                gen.writeString(field);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }

    private static Map<String, Collection<Endpoint>> groupPaths (Collection<Endpoint> endpoints) {
        Map<String, Collection<Endpoint>> paths = new LinkedHashMap<String, Collection<Endpoint>>();
        for (Endpoint endpoint : endpoints) {
            if (paths.containsKey(endpoint.getPath())) {
                paths.get(endpoint.getPath()).add(endpoint);
            } else {
                Collection<Endpoint> tmp = new ArrayList<Endpoint>();
                tmp.add(endpoint);
                paths.put(endpoint.getPath(), tmp);
            }
        }

        return paths;
    }

    /**
     * Will get the first path segment that follows the context path.  Will return the partial path as the resource id.
     */
    private static String getResource(String contextPath, Endpoint endpoint) {
        if (endpoint == null || isEmpty(endpoint.getPath()))
            return "/";

        //Shouldn't need to do this, but being safe.
        String tmp = fixPath(endpoint.getPath());


        //First normalize the path then, if not part of the path then simply ignore it.
        contextPath = fixPath(contextPath);
        contextPath = (!tmp.startsWith(contextPath) ? "" : contextPath);

        //remove the context path for evaluation
        tmp = tmp.substring(contextPath.length());

        if (tmp.indexOf("/", 1) > 0)
            tmp = tmp.substring(0, tmp.indexOf("/", 1));

        return contextPath + tmp;
    }
}
