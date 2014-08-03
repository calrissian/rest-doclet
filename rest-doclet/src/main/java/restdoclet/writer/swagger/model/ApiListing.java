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
package restdoclet.writer.swagger.model;

import java.util.Collection;

import static restdoclet.util.CommonUtils.isEmpty;

public class ApiListing {
    private final String swaggerVersion;
    private final String basePath;
    private final String resourcePath;
    private final String apiVersion;
    private final Collection<Api> apis;

    public ApiListing(String swaggerVersion, String basePath, String resourcePath, String apiVersion, Collection<Api> apis) {
        this.swaggerVersion = swaggerVersion;
        this.basePath = basePath;
        this.resourcePath = resourcePath;
        this.apiVersion = apiVersion;
        this.apis = (isEmpty(apis) ? null : apis);
    }

    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public Collection<Api> getApis() {
        return apis;
    }
}
