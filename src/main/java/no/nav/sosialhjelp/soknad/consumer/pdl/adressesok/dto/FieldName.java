package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;

public enum FieldName {

    ADRESSENAVN("adressenavn"),
    HUSNUMMER("husnummer"),
    HUSBOKSTAV("husbokstav"),
    POSTSTED("poststed");

    private final String name;

    FieldName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
