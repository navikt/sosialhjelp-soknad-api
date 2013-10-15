package no.nav.sbl.dialogarena.websoknad;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public enum JsResources {
    ALL("all");

    private static final String FOLDER = "js/";
    private static final String TYPE = ".js";
    private String resourcePath;

    JsResources(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public JavaScriptResourceReference getResource() {
        return new JavaScriptResourceReference(this.getClass(), FOLDER + resourcePath + TYPE);
    }
}
