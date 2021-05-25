package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;

public enum FieldName {

    VEGADRESSE_ADRESSENAVN("vegadresse.adressenavn"),
    VEGADRESSE_HUSNUMMER("vegadresse.husnummer"),
    VEGADRESSE_HUSBOKSTAV("vegadresse.husbokstav"),
    VEGADRESSE_POSTNUMMER("vegadresse.postnummer"),
    VEGADRESSE_POSTSTED("vegadresse.poststed");

    private final String name;

    FieldName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
