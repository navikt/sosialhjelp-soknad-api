package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;

public enum FieldName {

    ADRESSENAVN("adressenavn"),
    HUSNUMMER("husnummer"),
    HUSBOKSTAV("husbokstav"),
    POSTSTED("poststed");

    private final String field;

    FieldName(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

}
