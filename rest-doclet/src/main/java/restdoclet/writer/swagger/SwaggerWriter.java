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
package restdoclet.writer.swagger;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import restdoclet.Configuration;
import restdoclet.model.*;
import restdoclet.writer.Writer;
import restdoclet.writer.swagger.model.*;

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
    private static ObjectMapper mapper = new ObjectMapper()
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public void write(Collection<ClassDescriptor> classDescriptors, Configuration config) throws IOException {

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



    private static void writeResource(Map<String, Collection<Endpoint>> resources, Configuration config) throws IOException {

        ResourceListing resourceListing = new ResourceListing(SWAGGER_VERSION, config.getApiVersion(), config.getDocumentTitle());
        for (Entry<String, Collection<Endpoint>> entry : resources.entrySet()) {
            resourceListing.addApi("/../" + API_DOC_DIR + entry.getKey(), "");
            writeApi(entry.getKey(), entry.getValue(), config);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(RESOURCE_DOC), resourceListing);

    }

    private static void writeApi(String resource, Collection<Endpoint> endpoints, Configuration config) throws IOException {
        Map<String, Collection<Endpoint>> pathGroups = groupPaths(endpoints);

        File apiFile = new File("./" + API_DOC_DIR , resource);
        apiFile.getParentFile().mkdirs();

        Collection<Api> apis = new ArrayList<Api>(pathGroups.size());
        for (Entry<String, Collection<Endpoint>> entry : pathGroups.entrySet())
            apis.add(new Api(entry.getKey(), "", getOperations(entry.getValue())));


        mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(apiFile),
                new ApiListing(SWAGGER_VERSION, config.getUrl(), resource, config.getApiVersion(), apis)
        );
    }

    private static Collection<Operation> getOperations(Collection<Endpoint> endpoints) {
        Collection<Operation> operations = new ArrayList<Operation>(endpoints.size());

        for (Endpoint endpoint : endpoints) {
            Collection<Parameter> params = new ArrayList<Parameter>();

            for (PathVar pathVar : endpoint.getPathVars())
                params.add(getParameter(pathVar));

            for (QueryParam queryParam : endpoint.getQueryParams())
                params.add(getParameter(queryParam));

            if (endpoint.getRequestBody() != null)
                params.add(getParameter(endpoint.getRequestBody()));

            operations.add(
                    new Operation(
                            endpoint.getHttpMethod(),
                            "nickname",
                            endpoint.getShortDescription(),
                            endpoint.getDescription(),
                            dataType(endpoint.getType()),
                            endpoint.getProduces(),
                            endpoint.getConsumes(),
                            params
                    )
            );
        }

        return operations;
    }

    private static Parameter getParameter(PathVar pathVar) {
        return new Parameter(
                "path",
                pathVar.getName(),
                pathVar.getDescription(),
                basicType(pathVar.getType()),
                null,
                true,
                false,
                allowableValues(pathVar.getType())
        );
    }

    private static Parameter getParameter(QueryParam queryParam) {
        //If it is a container type then allow multiple but use the underlying type.
        boolean container = isContainer(queryParam.getType());

        return new Parameter(
                "query",
                queryParam.getName(),
                queryParam.getDescription(),
                (container ? internalContainerType(queryParam.getType()) : basicType(queryParam.getType())),
                null,
                queryParam.isRequired(),
                container,
                allowableValues(queryParam.getType())
        );
    }

    private static Parameter getParameter(RequestBody requestBody) {
        return new Parameter(
                "body",
                requestBody.getName(),
                requestBody.getDescription(),
                dataType(requestBody.getType()),
                null,
                true,
                false,
                allowableValues(requestBody.getType())
        );
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

    private static void copyIndex(Configuration config) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {

            if (config.hasUrl())
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(SWAGGER_CALLABLE_HTML);
            else
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(SWAGGER_DEFAULT_HTML);

            out = new FileOutputStream(new File(".", "index.html"));
            copy(in, out);

        } finally {
            close(in, out);
        }
    }

    private static void copySwagger() throws IOException {
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
        } finally {
            close(swaggerZip, out);
        }
    }
}
