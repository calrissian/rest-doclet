/*
 * Copyright (C) 2013 The Calrissian Authors
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
 */
package restdoclet.model;

import com.sun.javadoc.Type;

import java.util.Collection;

public class Endpoint {

    private final String path;
    private final String httpMethod;
    private final Collection<QueryParam> queryParams;
    private final Collection<PathVar> pathVars;
    private final RequestBody requestBody;
    private final Collection<String> consumes;
    private final Collection<String> produces;
    private final String shortDescription;
    private final String description;
    private final Type type;

    public Endpoint(
            String path,
            String httpMethod,
            Collection<QueryParam> queryParams,
            Collection<PathVar> pathVars,
            RequestBody requestBody,
            Collection<String> consumes,
            Collection<String> produces,
            String shortDescription,
            String description,
            Type type) {

        this.path = path;
        this.httpMethod = httpMethod;
        this.queryParams = queryParams;
        this.pathVars = pathVars;
        this.requestBody = requestBody;
        this.consumes = consumes;
        this.produces = produces;
        this.shortDescription = shortDescription;
        this.description = description;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Collection<QueryParam> getQueryParams() {
        return queryParams;
    }

    public Collection<PathVar> getPathVars() {
        return pathVars;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public Collection<String> getConsumes() {
        return consumes;
    }

    public Collection<String> getProduces() {
        return produces;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "path='" + path + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", queryParams=" + queryParams +
                ", pathVars=" + pathVars +
                ", requestBody=" + requestBody +
                ", consumes=" + consumes +
                ", produces=" + produces +
                ", shortDescription='" + shortDescription + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
