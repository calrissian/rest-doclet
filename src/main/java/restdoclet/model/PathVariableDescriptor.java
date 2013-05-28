package restdoclet.model;

public class PathVariableDescriptor {

    private final String name;
    private final String description;

    public PathVariableDescriptor(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "PathVariableDescriptor{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
