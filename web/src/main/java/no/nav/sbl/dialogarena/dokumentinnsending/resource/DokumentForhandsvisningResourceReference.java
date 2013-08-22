package no.nav.sbl.dialogarena.dokumentinnsending.resource;

import no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverter;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import javax.inject.Inject;


public class DokumentForhandsvisningResourceReference extends ResourceReference {

    @Inject
    private ThumbnailConverter converter;

    public DokumentForhandsvisningResourceReference() {
        super(DokumentForhandsvisningResourceReference.class, "dokumentForhandsvisning");
    }

    @Override
    public IResource getResource() {
        return new DokumentForhandsvisningResource(converter);
    }
}
