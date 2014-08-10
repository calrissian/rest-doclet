/*******************************************************************************
 * Copyright (C) 2014 The Calrissian Authors
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
package org.calrissian.restdoclet.model;

import java.util.Collection;

public class ClassDescriptor {

    private final String name;
    private final String contextPath;
    private final Collection<Endpoint> endpoints;
    private final String description;

    public ClassDescriptor(String name, String contextPath, Collection<Endpoint> endpoints, String description) {
        this.name = name;
        this.contextPath = contextPath;
        this.endpoints = endpoints;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Collection<Endpoint> getEndpoints() {
        return endpoints;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ClassDescriptor{" +
                "name='" + name + '\'' +
                ", contextPath='" + contextPath + '\'' +
                ", endpoints=" + endpoints +
                ", description='" + description + '\'' +
                '}';
    }
}
