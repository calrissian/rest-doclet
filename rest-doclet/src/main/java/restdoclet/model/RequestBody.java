package restdoclet.model;


import com.sun.javadoc.Type;

public class RequestBody {

    private final String name;
    private final String description;
    private final Type type;

    public RequestBody(String name, String description, Type type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "RequestBody{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
