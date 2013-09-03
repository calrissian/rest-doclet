package restdoclet.writer.swagger.model;

import java.util.Collection;

import static restdoclet.util.CommonUtils.isEmpty;

public class Operation {
    private final String httpMethod;
    private final String nickname;
    private final String notes;
    private final String summary;
    private final String responseClass;
    private final Collection<String> produces;
    private final Collection<String> consumes;
    private final Collection<Parameter> parameters;

    public Operation(String httpMethod,
                     String nickname,
                     String notes,
                     String summary,
                     String responseClass,
                     Collection<String> produces,
                     Collection<String> consumes,
                     Collection<Parameter> parameters) {
        this.httpMethod = httpMethod;
        this.nickname = nickname;
        this.notes = notes;
        this.summary = summary;
        this.responseClass = responseClass;
        this.produces = (isEmpty(produces) ? null : produces);
        this.consumes = (isEmpty(consumes) ? null : consumes);
        this.parameters = (isEmpty(parameters) ? null : parameters);
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getNickname() {
        return nickname;
    }

    public String getNotes() {
        return notes;
    }

    public String getSummary() {
        return summary;
    }

    public String getResponseClass() {
        return responseClass;
    }

    public Collection<String> getProduces() {
        return produces;
    }

    public Collection<String> getConsumes() {
        return consumes;
    }

    public Collection<Parameter> getParameters() {
        return parameters;
    }
}
