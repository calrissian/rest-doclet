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
