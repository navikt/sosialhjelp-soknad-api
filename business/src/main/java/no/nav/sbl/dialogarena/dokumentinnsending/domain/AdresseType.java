package no.nav.sbl.dialogarena.dokumentinnsending.domain;

public enum AdresseType {
    BOSTEDSADRESSE(false), MIDLERTIDIG_POSTADRESSE_UTLAND(true), MIDLERTIDIG_POSTADRESSE_NORGE(false), POSTADRESSE(false), POSTADRESSE_UTLAND(true), UKJENT(false), UKJENT_VERDI(false);

    private final boolean erUtlandsadresse;

    AdresseType(boolean erUtlandsadresse) {
        this.erUtlandsadresse = erUtlandsadresse;
    }

    public boolean erUtlandsadresse() {
        return erUtlandsadresse;
    }
}