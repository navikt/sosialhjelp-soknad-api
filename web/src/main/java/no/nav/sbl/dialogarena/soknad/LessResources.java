package no.nav.sbl.dialogarena.soknad;

import org.apache.wicket.request.resource.PackageResourceReference;

public enum LessResources {
    SOKNAD_LESS("soknad"),
    TESTSOKNAD_LESS("testsoknad");

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
