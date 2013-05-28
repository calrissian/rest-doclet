package restdoclet.model;

import java.util.Collection;

public class ClassDescriptor {

    private final String name;
    private final Collection<EndpointDescriptor> endpoints;
    private final String description;

    public ClassDescriptor(String name, Collection<EndpointDescriptor> endpoints, String description) {
        this.name = name;
        this.endpoints = endpoints;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Collection<EndpointDescriptor> getEndpoints() {
        return endpoints;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ClassDescriptor{" +
                "name='" + name + '\'' +
                ", endpoints=" + endpoints +
                ", description='" + description + '\'' +
                '}';
    }
}
