package no.nav.sbl.dialogarena.dokumentinnsending.kodeverk;

import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;

/*
Henter koder for skjemaer og vedlegg fra lokalt kodeverk
 */
public interface KodeverkClient {

    String EKSTRA_VEDLEGG_PREFIX = "Annet";
    String EKSTRA_VEDLEGG_KODEVERKSID = "N6";
    String KVITTERING_KODEVERKSID = "L7";

    KodeverkSkjema hentKodeverkSkjemaForSkjemanummer(String skjemaId);
    KodeverkSkjema hentKodeverkSkjemaForVedleggsid(String vedleggsId);

    boolean isEgendefinert(String skjemaId);
}
