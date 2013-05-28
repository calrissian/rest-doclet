package restdoclet.model;

public class QueryParamDescriptor {

    private final String name;
    private final boolean required;
    private final String description;

    public QueryParamDescriptor(String name, boolean required, String description) {
        this.name = name;
        this.required = required;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "QueryParamDescriptor{" +
                "name='" + name + '\'' +
                ", required=" + required +
                ", description='" + description + '\'' +
                '}';
    }
}
