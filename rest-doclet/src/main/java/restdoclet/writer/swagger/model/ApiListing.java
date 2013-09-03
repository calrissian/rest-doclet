package restdoclet.writer.swagger.model;

import java.util.Collection;

import static restdoclet.util.CommonUtils.isEmpty;

public class ApiListing {
    private final String swaggerVersion;
    private final String basePath;
    private final String resourcePath;
    private final String apiVersion;
    private final Collection<Api> apis;

    public ApiListing(String swaggerVersion, String basePath, String resourcePath, String apiVersion, Collection<Api> apis) {
        this.swaggerVersion = swaggerVersion;
        this.basePath = basePath;
        this.resourcePath = resourcePath;
        this.apiVersion = apiVersion;
        this.apis = (isEmpty(apis) ? null : apis);
    }

    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public Collection<Api> getApis() {
        return apis;
    }
}
