package no.nav.sosialhjelp.soknad.consumer.fiks.dto;

import java.io.InputStream;


public class FilOpplasting {

    public FilMetadata metadata;
    public InputStream data;

    public FilOpplasting(FilMetadata metadata, InputStream data) {
        this.metadata = metadata;
        this.data = data;
    }
}
