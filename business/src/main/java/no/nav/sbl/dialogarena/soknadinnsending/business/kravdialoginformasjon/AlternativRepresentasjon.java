package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

public interface AlternativRepresentasjon {
    String getFilnavn();

    String getMimetype();

    String getUuid();

    byte[] getContent();
}
