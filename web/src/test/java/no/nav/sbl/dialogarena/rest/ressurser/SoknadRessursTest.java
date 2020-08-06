package no.nav.sbl.dialogarena.rest.ressurser;


import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.rest.ressurser.SoknadRessurs.XSRF_TOKEN;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SoknadRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "Hans og Grete";

    @Mock
    SoknadService soknadService;

    @Mock
    SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private HenvendelseService henvendelseService;

    @Mock
    private SubjectHandler subjectHandler;

    @InjectMocks
    SoknadRessurs ressurs;

    @Before
    public void setUp() {
        when(subjectHandler.getUserId()).thenReturn("123");
    }

    @Test
    public void skalSetteXsrfToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        ressurs.hentXsrfCookie(BEHANDLINGSID, response);
        verify(response,times(2)).addCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo(XSRF_TOKEN + "-123");
    }

    @Test
    public void opprettingAvSoknadSkalSetteXsrfToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        ressurs.opprettSoknad(null, response, "");
        verify(response,times(2)).addCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo(XSRF_TOKEN + "-null");
    }

    @Test
    public void opprettSoknadUtenBehandlingsidSkalStarteNySoknad() {
        ressurs.opprettSoknad(null, mock(HttpServletResponse.class), "");
        verify(soknadService).startSoknad("");
    }

    @Test
    public void opprettSoknadMedBehandlingsidSomIkkeHarEttersendingSkalStarteNyEttersending() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(anyString(), anyString())).thenReturn(Optional.empty());
        ressurs.opprettSoknad(BEHANDLINGSID, mock(HttpServletResponse.class), "");
        verify(soknadService).startEttersending(eq(BEHANDLINGSID));
    }

    @Test
    public void opprettSoknadMedBehandlingsidSomHarEttersendingSkalIkkeStarteNyEttersending() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(eq(BEHANDLINGSID), anyString())).thenReturn(
                Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))));
        ressurs.opprettSoknad(BEHANDLINGSID, mock(HttpServletResponse.class), "");
        verify(soknadService, never()).startEttersending(eq(BEHANDLINGSID));
    }

    @Test
    public void oppdaterSamtykkerMedTomListaSkalIkkeForeTilNoenSamtykker() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(eq(BEHANDLINGSID), anyString())).thenReturn(
                Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))));
        List<BekreftelseRessurs> samtykkeListe = Lists.emptyList();
        String token = "token";

        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token);

        verify(soknadService, only()).oppdaterSamtykker(BEHANDLINGSID, false, false, token);
    }

    @Test
    public void oppdaterSamtykkerSkalGiSamtykkerFraLista() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(eq(BEHANDLINGSID), anyString())).thenReturn(
                Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))));
        BekreftelseRessurs bekreftelse1 = new BekreftelseRessurs().withType(BOSTOTTE_SAMTYKKE).withVerdi(true);
        BekreftelseRessurs bekreftelse2 = new BekreftelseRessurs().withType(UTBETALING_SKATTEETATEN_SAMTYKKE).withVerdi(true);
        List<BekreftelseRessurs> samtykkeListe = Lists.newArrayList(bekreftelse1, bekreftelse2);
        String token = "token";

        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token);

        verify(soknadService, only()).oppdaterSamtykker(BEHANDLINGSID, true, true, token);
    }

    @Test
    public void oppdaterSamtykkerSkalGiSamtykkerFraLista_menKunDersomVerdiErSann() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(eq(BEHANDLINGSID), anyString())).thenReturn(
                Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))));
        BekreftelseRessurs bekreftelse1 = new BekreftelseRessurs().withType(BOSTOTTE_SAMTYKKE).withVerdi(true);
        BekreftelseRessurs bekreftelse2 = new BekreftelseRessurs().withType(UTBETALING_SKATTEETATEN_SAMTYKKE).withVerdi(false);
        List<BekreftelseRessurs> samtykkeListe = Lists.newArrayList(bekreftelse1, bekreftelse2);
        String token = "token";

        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token);

        verify(soknadService, only()).oppdaterSamtykker(BEHANDLINGSID, true, false, token);
    }

    @Test
    public void hentSamtykker_skalReturnereTomListeNarViIkkeHarNoenSamtykker() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(eq(BEHANDLINGSID), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
        String token = "token";

        List<BekreftelseRessurs> bekreftelseRessurser = ressurs.hentSamtykker(BEHANDLINGSID, token);

        assertThat(bekreftelseRessurser).isEmpty();
    }

    @Test
    public void hentSamtykker_skalReturnereListeMedSamtykker() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        JsonInternalSoknad internalSoknad = createEmptyJsonInternalSoknad(EIER);
        JsonOkonomiopplysninger opplysninger = internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger();
        setBekreftelse(opplysninger, BOSTOTTE_SAMTYKKE, true, "Samtykke test tekst!");
        setBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE, true, "Samtykke test tekst!");

        when(soknadUnderArbeidRepository.hentSoknad(eq(BEHANDLINGSID), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(internalSoknad));

        String token = "token";
        List<BekreftelseRessurs> bekreftelseRessurser = ressurs.hentSamtykker(BEHANDLINGSID, token);
        assertThat(bekreftelseRessurser).hasSize(2);
        BekreftelseRessurs bekreftelse1 = bekreftelseRessurser.get(0);
        assertThat(bekreftelse1.type).isEqualTo(BOSTOTTE_SAMTYKKE);
        assertThat(bekreftelse1.verdi).isEqualTo(true);
        BekreftelseRessurs bekreftelse2 = bekreftelseRessurser.get(1);
        assertThat(bekreftelse2.type).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        assertThat(bekreftelse2.verdi).isEqualTo(true);
    }

    @Test
    public void hentSamtykker_skalReturnereListeMedSamtykker_tarBortDeUtenSattVerdi() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        JsonInternalSoknad internalSoknad = createEmptyJsonInternalSoknad(EIER);
        JsonOkonomiopplysninger opplysninger = internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger();
        setBekreftelse(opplysninger, BOSTOTTE_SAMTYKKE, false, "Samtykke test tekst!");
        setBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE, true, "Samtykke test tekst!");

        when(soknadUnderArbeidRepository.hentSoknad(eq(BEHANDLINGSID), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(internalSoknad));
        String token = "token";
        List<BekreftelseRessurs> bekreftelseRessurser = ressurs.hentSamtykker(BEHANDLINGSID, token);
        assertThat(bekreftelseRessurser).hasSize(1);
        BekreftelseRessurs bekreftelse1 = bekreftelseRessurser.get(0);
        assertThat(bekreftelse1.type).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        assertThat(bekreftelse1.verdi).isEqualTo(true);
    }
}