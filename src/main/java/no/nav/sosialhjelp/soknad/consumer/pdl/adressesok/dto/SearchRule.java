package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;

public enum SearchRule {

    EQUALS("equals"),
    CONTAINS("contains"),
    FUZZY("fuzzy");

    private final String name;

    SearchRule(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
