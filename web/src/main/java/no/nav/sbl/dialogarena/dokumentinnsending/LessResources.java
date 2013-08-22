package no.nav.sbl.dialogarena.dokumentinnsending;

import org.apache.wicket.request.resource.PackageResourceReference;

public enum LessResources {
    IKONER_LESS("ikoner"),
    INNSENDING_LESS("innsending"),
    BEKREFTELSE_LESS("bekreftelse"),
    HJELP_LESS("hjelp"),
    KVITTERING_INNSENDING_LESS("kvittering-innsending"),
    KVITTERING_SLETTET_LESS("kvittering-slettet"),
    FORTSETT_SENERE_LESS("fortsett-senere"),
    MODAL_PAGE("modal-page"),
    OPPLASTING("opplasting"),
    LEGG_TIL_VEDLEGG("legg-til-vedlegg"),
    SLETT_INNSENDING("slett-innsending"),
    TOOLTIP("tooltip"),
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
