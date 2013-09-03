package restdoclet.writer.swagger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

import static restdoclet.util.CommonUtils.isEmpty;

public class Parameter {

    private final String paramType;
    private final String name;
    private final String description;
    private final String type;
    private final String format;
    private final boolean required;
    private final boolean allowMultiple;
    @JsonProperty("enum") private final Collection<String> allowableValues;

    public Parameter(String paramType, String name, String description, String type, String format, boolean required, boolean allowMultiple, Collection<String> allowableValues) {
        this.paramType = paramType;
        this.name = name;
        this.description = description;
        this.type = type;
        this.format = format;
        this.required = required;
        this.allowMultiple = allowMultiple;
        this.allowableValues = (isEmpty(allowableValues) ? null : allowableValues);
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

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public Collection<String> getAllowableValues() {
        return allowableValues;
    }
}
