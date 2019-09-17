package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class DokumentInfo {

    @JsonCreator
    public DokumentInfo(@JsonProperty("filnavn")  String filnavn, @JsonProperty("dokumentlagerDokumentId") UUID dokumentlagerDokumentId, @JsonProperty("storrelse")  Long storrelse) {
        this.filnavn = filnavn;
        this.dokumentlagerDokumentId = dokumentlagerDokumentId;
        this.storrelse = storrelse;
    }


    public String filnavn;


    public UUID dokumentlagerDokumentId;


    public Long storrelse;

}