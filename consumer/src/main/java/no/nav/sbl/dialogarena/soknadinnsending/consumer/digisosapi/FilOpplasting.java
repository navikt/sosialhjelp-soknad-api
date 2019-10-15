package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

import java.io.InputStream;


public class FilOpplasting {

    public FilMetadata metadata;
    public InputStream data;

    public FilOpplasting(FilMetadata metadata, InputStream data) {
        this.metadata = metadata;
        this.data = data;
    }
}
