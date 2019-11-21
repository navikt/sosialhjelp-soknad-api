package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain;

public enum Diskresjonskoder {
    STRENGT_FORTROLIG_ADRESSE("SPSF", "6"), FORTROLIG_ADRESSE("SPFO", "7");

    public final String kodeverkVerdi;
    public final String tallVerdi;

    Diskresjonskoder(String kodeverkVerdi, String tallVerdi) {
        this.kodeverkVerdi = kodeverkVerdi;
        this.tallVerdi= tallVerdi;
    }
}
