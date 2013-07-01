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
package restdoclet.model;

import java.util.Collection;

public class EndpointDescriptor {

    private final String path;
    private final String httpMethod;
    private final Collection<QueryParamDescriptor> queryParams;
    private final Collection<PathVariableDescriptor> pathVars;
    private final Collection<String> consumes;
    private final Collection<String> produces;
    private final String description;

    public EndpointDescriptor(
            String path,
            String httpMethod,
            Collection<QueryParamDescriptor> queryParams,
            Collection<PathVariableDescriptor> pathVars,
            Collection<String> consumes,
            Collection<String> produces,
            String description) {

        this.path = path;
        this.httpMethod = httpMethod;
        this.queryParams = queryParams;
        this.pathVars = pathVars;
        this.consumes = consumes;
        this.produces = produces;
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Collection<QueryParamDescriptor> getQueryParams() {
        return queryParams;
    }

    public Collection<PathVariableDescriptor> getPathVars() {
        return pathVars;
    }

    public Collection<String> getConsumes() {
        return consumes;
    }

    public Collection<String> getProduces() {
        return produces;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "EndpointDescriptor{" +
                "path='" + path + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", queryParams=" + queryParams +
                ", pathVars=" + pathVars +
                ", consumes=" + consumes +
                ", produces=" + produces +
                ", description='" + description + '\'' +
                '}';
    }
}
