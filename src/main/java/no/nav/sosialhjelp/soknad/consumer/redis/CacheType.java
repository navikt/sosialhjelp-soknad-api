package no.nav.sosialhjelp.soknad.consumer.redis;

public enum CacheType {
    DKIF("dkif-"),
    KONTONUMMER("kontonummer-"),
    NAVUTBETALINGER("navutbetalinger-"),
    HENT_GEOGRAFISKTILKNYTNING("hent-geografisktilknytning-"),
    HENTPERSON_ADRESSEBESKYTTELSE("hentperson-adressebeskyttelse-"),
    HENTPERSON("hentperson-person-"),
    HENTPERSON_BARN("hentperson-barn-"),
    HENTPERSON_EKTEFELLE("hentperson-ektefelle-");

    private final String prefix;

    CacheType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
