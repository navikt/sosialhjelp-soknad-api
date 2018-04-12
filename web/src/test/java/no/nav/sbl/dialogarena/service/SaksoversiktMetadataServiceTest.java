package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendtSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SaksoversiktMetadataServiceTest {

    @Mock
    SoknadMetadataRepository soknadMetadataRepository;

    @Mock
    NavMessageSource navMessageSource;

    @InjectMocks
    SaksoversiktMetadataService saksoversiktMetadataService;

    @Before
    public void setUp() {
        Properties props = mock(Properties.class);
        when(props.getProperty(anyString())).then(new ReturnsArgumentAt(0));

        when(navMessageSource.getBundleFor(anyString(), any())).thenReturn(props);
    }

    @Test
    public void henterForBruker() throws ParseException {
        SoknadMetadata soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = "12345";
        soknadMetadata.behandlingsId = "beh123";
        soknadMetadata.innsendtDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0);

        VedleggMetadata v = new VedleggMetadata();
        v.skjema = "skjema1";
        v.tillegg = "tillegg1";
        v.status = Status.LastetOpp;
        VedleggMetadata v2 = new VedleggMetadata();
        v2.skjema = "skjema1";
        v2.tillegg = "tillegg1";
        v2.status = Status.LastetOpp;
        VedleggMetadata v3 = new VedleggMetadata();
        v3.skjema = "skjema2";
        v3.tillegg = "tillegg1";
        v3.status = Status.SendesSenere;

        List<VedleggMetadata> vedleggListe = soknadMetadata.vedlegg.vedleggListe;
        vedleggListe.add(v);
        vedleggListe.add(v2);
        vedleggListe.add(v3);

        when(soknadMetadataRepository.hentSoknaderMedStatusForBruker("12345", SoknadInnsendingStatus.FERDIG))
                .thenReturn(asList(soknadMetadata));

        List<InnsendtSoknad> resultat = saksoversiktMetadataService.hentInnsendteSoknaderForFnr("12345");

        assertEquals(1, resultat.size());
        InnsendtSoknad soknad = resultat.get(0);
        assertEquals("beh123", soknad.getBehandlingsId());
        assertEquals(1, soknad.getVedlegg().size());
        assertEquals("vedlegg.skjema1.tillegg1.tittel", soknad.getVedlegg().get(0).getTittel());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-04-11 13:30:00"), soknad.getInnsendtDato());
    }

}