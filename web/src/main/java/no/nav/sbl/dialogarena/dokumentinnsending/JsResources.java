package no.nav.sbl.dialogarena.dokumentinnsending;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public enum JsResources {
    LOCAL("dokumentinnsending"),
    PREVIEW_TOOLTIP("previewTooltip"),
    LOADING_INDICATOR("loadingIndicator"),
    VALIDATION("validation"),
    
    MUSTACHE("mustache/mustache"),
    SENDSOKNAD("sendsoknad");

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
