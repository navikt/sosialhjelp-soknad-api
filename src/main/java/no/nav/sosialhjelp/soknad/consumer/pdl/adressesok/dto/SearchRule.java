package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;

public enum SearchRule {

    EQUALS("equals"),
    CONTAINS("contains"),
    FUZZY("fuzzy");

    private final String field;

    SearchRule(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
