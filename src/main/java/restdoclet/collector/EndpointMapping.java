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
