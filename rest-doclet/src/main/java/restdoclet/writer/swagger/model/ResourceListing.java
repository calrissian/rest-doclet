package restdoclet.writer.swagger.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static restdoclet.util.CommonUtils.isEmpty;

public class ResourceListing {
    private final String swaggerVersion;
    private final String apiVersion;
    private Collection<Map<String, String>> apis;
    private Map<String, String> info;

    public ResourceListing(String swaggerVersion, String apiVersion, String title) {
        this.swaggerVersion = swaggerVersion;
        this.apiVersion = apiVersion;
        this.apis = new ArrayList<Map<String, String>>();
        this.info = new LinkedHashMap<String, String>(1);
        if (!isEmpty(title)) {

            this.info.put("title", title);
        }
    }

    public void addApi(String path, String description) {
        if (isEmpty(path))
            return;

        Map<String, String> api = new LinkedHashMap<String, String>(2);
        api.put("path", path);
        api.put("description", (isEmpty(description) ? "" : description));

        apis.add(api);
    }

    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public Collection<Map<String, String>> getApis() {
        return apis;
    }

    public Map<String, String> getInfo() {
        return info;
    }
}
