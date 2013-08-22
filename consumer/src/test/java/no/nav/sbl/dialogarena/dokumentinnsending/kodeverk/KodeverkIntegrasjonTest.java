package no.nav.sbl.dialogarena.dokumentinnsending.kodeverk;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.config.ConsumerConfigTest;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ConsumerConfigTest.class)
public class KodeverkIntegrasjonTest {

    @Inject
    private KodeverkClient kodeverkClient;

    @Test
    public void skjemaIdForDagPengerSkalFinnesIKodeverk() {
        String skjemaIdKravOmDagpenger = "NAV 04-01.03";
        assertNotNull(kodeverkClient.hentKodeverkSkjemaForSkjemanummer(skjemaIdKravOmDagpenger));
    }

    @Test
    public void skjemaIdForDagPengerSkalFinnesIKodeverkOgSkalHaAlleAttributterSatt() {
        String skjemaIdKravOmDagpenger = "NAV 04-01.03";
        KodeverkSkjema kodeverkSkjemaKravOmDagpenger = kodeverkClient.hentKodeverkSkjemaForSkjemanummer(skjemaIdKravOmDagpenger);
        assertEquals("Krav om dagpenger", kodeverkSkjemaKravOmDagpenger.getTittel());
        assertEquals("196002", kodeverkSkjemaKravOmDagpenger.getGosysId());
        assertEquals("232553", kodeverkSkjemaKravOmDagpenger.getBeskrivelse());
        assertEquals("DAG", kodeverkSkjemaKravOmDagpenger.getTema());
        assertEquals(" https://www-t8.nav.no:443/skjema/Skjemaer/Alle+skjemaer+JSON/_attachment/232535?_ts=135cd358820&download=true", kodeverkSkjemaKravOmDagpenger.getUrl());
        assertEquals(" ", kodeverkSkjemaKravOmDagpenger.getUrlengelsk());
        assertEquals("NAV 04-01.03", kodeverkSkjemaKravOmDagpenger.getSkjemanummer());
        assertEquals("", kodeverkSkjemaKravOmDagpenger.getVedleggsid());
    }

    @Test(expected = ApplicationException.class)
    public void skjemaIdSkalIkkeFinnesIKodeverk() {
        kodeverkClient.hentKodeverkSkjemaForSkjemanummer("tull");
    }

    @Test
    public void skjemaIdErLikSkjemaIdForEgendefinert() {
        String skjemaIdAnnet = "N6";
        assertTrue(kodeverkClient.isEgendefinert(skjemaIdAnnet));
    }

    @Test
    public void skjemaIdSkalIkkeVaereEgendefinert() {
        kodeverkClient.isEgendefinert("tull");
    }
}