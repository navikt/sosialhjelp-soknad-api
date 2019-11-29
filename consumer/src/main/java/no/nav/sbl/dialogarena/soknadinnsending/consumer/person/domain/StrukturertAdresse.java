package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.WRAPPER_OBJECT, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(name="Gateadresse", value=Gateadresse.class)
})
public class StrukturertAdresse {


    private String landkode;
    private String tilleggsadresse;
    private String postnummer;
    private String poststed;

    public String getLandkode() {
        return landkode;
    }

    public String getTilleggsadresse() { return tilleggsadresse; }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public StrukturertAdresse withLandkode(String landkode) {
        this.landkode = landkode;
        return this;
    }

    public StrukturertAdresse withTilleggsadresse(String tilleggsadresse) {
        this.tilleggsadresse = tilleggsadresse;
        return this;
    }

    public StrukturertAdresse withPoststed(String poststed) {
        this.poststed = poststed;
        return this;
    }

    public StrukturertAdresse withPostnummer(String postnummer) {
        this.postnummer = postnummer;
        return this;
    }



}
