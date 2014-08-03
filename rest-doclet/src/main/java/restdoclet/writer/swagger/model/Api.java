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

public class Api {
    private final String path;
    private final String description;
    private final Collection<Operation> operations;

    public Api(String path, String description, Collection<Operation> operations) {
        this.path = path;
        this.description = description;
        this.operations = (isEmpty(operations) ? null : operations);
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public Collection<Operation> getOperations() {
        return operations;
    }
}
