package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import java.io.InputStream;


public class FilOpplasting {

    public FilMetadata metadata;
    public InputStream data;

    public FilOpplasting(FilMetadata metadata, InputStream data) {
        this.metadata = metadata;
        this.data = data;
    }

    FilOpplasting metadata(FilMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    FilOpplasting data(InputStream data) {
        this.data = data;
        return this;
    }

}
