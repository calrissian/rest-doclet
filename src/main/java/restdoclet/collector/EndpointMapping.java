/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package restdoclet.collector;


import java.util.Collection;

public class EndpointMapping {
    private final Collection<String> paths;
    private final Collection<String> httpMethods;
    private final Collection<String> consumes;
    private final Collection<String> produces;

    public EndpointMapping(
            Collection<String> paths,
            Collection<String> httpMethods,
            Collection<String> consumes,
            Collection<String> produces) {

        this.paths = paths;
        this.httpMethods = httpMethods;
        this.consumes = consumes;
        this.produces = produces;
    }

    public Collection<String> getPaths() {
        return paths;
    }

    public Collection<String> getHttpMethods() {
        return httpMethods;
    }

    public Collection<String> getConsumes() {
        return consumes;
    }

    public Collection<String> getProduces() {
        return produces;
    }
}
