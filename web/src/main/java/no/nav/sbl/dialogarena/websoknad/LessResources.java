package no.nav.sbl.dialogarena.websoknad;

import org.apache.wicket.request.resource.PackageResourceReference;

public enum LessResources {
    ALL_LESS("all"),
    
    PERSONDATA("websoknad/persondata"),
    SOKNAD("websoknad/soknad"),
    TEST_SOKNAD("websoknad/testsoknad");

    private static final String FOLDER = "css/";
    private static final String TYPE = ".less";
    private String resourcePath;

    LessResources(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public PackageResourceReference getResource() {
        return new PackageResourceReference(this.getClass(), FOLDER + resourcePath + TYPE);
    }
}
