package restdoclet.writer.swagger.model;

public class Parameter {

    private final String paramType;
    private final String name;
    private final String description;
    private final String dataType;
    private final boolean required;
    private final boolean allowMultiple;
    private final AllowableValues allowableValues;

    public Parameter(String paramType, String name, String description, String dataType, boolean required, boolean allowMultiple, AllowableValues allowableValues) {
        this.paramType = paramType;
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.required = required;
        this.allowMultiple = allowMultiple;
        this.allowableValues = allowableValues;
    }

    public String getParamType() {
        return paramType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public AllowableValues getAllowableValues() {
        return allowableValues;
    }
}
