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
