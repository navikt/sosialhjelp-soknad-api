package no.nav.sosialhjelp.soknad.domain.model.digisosapi;

import java.io.InputStream;


public class FilOpplasting {

    public FilMetadata metadata;
    public InputStream data;

    public FilOpplasting(FilMetadata metadata, InputStream data) {
        this.metadata = metadata;
        this.data = data;
    }
}
