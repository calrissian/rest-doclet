package restdoclet.writer.swagger.model;

import java.util.Collection;

import static restdoclet.util.CommonUtils.isEmpty;

public class AllowableValues {
    private final String valueType;
    private final Collection<String> values;

    public AllowableValues(String valueType, Collection<String> values) {
        this.valueType = valueType;
        this.values = (isEmpty(values) ? null : values);
    }

    public String getValueType() {
        return valueType;
    }

    public Collection<String> getValues() {
        return values;
    }
}
