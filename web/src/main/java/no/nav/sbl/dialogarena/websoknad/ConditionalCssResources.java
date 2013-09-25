package no.nav.sbl.dialogarena.websoknad;

import no.nav.modig.frontend.ConditionalCssResource;
import org.apache.wicket.request.resource.PackageResourceReference;

public enum ConditionalCssResources {
    IE("ie", "screen", "IE");

    private static final String FOLDER = "css/";
    private static final String TYPE = ".css";
    private String filename;
    private String media;
    private String condition;


    ConditionalCssResources(String filename, String media, String condition) {
        this.filename = filename;
        this.media = media;
        this.condition = condition;
    }

    public ConditionalCssResource getResource(WicketApplication application) {
        PackageResourceReference resourceReference = new PackageResourceReference(this.getClass(), FOLDER + filename + TYPE);
        application.mountResource(resourceReference.getName(), resourceReference);
        return new ConditionalCssResource(resourceReference, media, condition);
    }
}
