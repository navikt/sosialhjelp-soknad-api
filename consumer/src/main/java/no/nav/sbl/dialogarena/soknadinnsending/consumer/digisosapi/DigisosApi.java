package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

public interface DigisosApi {
    void ping();

   KommuneStatus kommuneInfo(String kommunenr);
}
