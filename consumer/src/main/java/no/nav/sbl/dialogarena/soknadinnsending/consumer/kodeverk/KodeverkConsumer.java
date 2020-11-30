package no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk;


import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.KodeverkDto;

public interface KodeverkConsumer {

    void ping();

    KodeverkDto hentPostnummer();

    KodeverkDto hentKommuner();

    KodeverkDto hentLandkoder();
}
