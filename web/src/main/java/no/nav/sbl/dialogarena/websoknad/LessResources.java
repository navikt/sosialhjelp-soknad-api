package no.nav.sbl.dialogarena.websoknad;

import org.apache.wicket.request.resource.PackageResourceReference;

public enum LessResources {

    ALL_LESS("all"),
    INFORMASJONSSIDE_LESS("informasjonsside"),
    OPPLASTING_LESS("opplasting"),
    OPPSUMMERING_LESS("oppsummering"),
    FORTSETT_SENERE_LESS("fortsettSenere"),
    KVITTERING_LESS("kvittering"),
    ARBEIDSFORHOLD_LESS("arbeidsforhold");

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
