package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto;

public enum SearchRule {

    EQUALS("equals"),
    CONTAINS("contains"),
    FUZZY("fuzzy");

    private final String rule;

    SearchRule(String rule) {
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }
}
