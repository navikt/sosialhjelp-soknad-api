package no.nav.sosialhjelp.soknad.web.rest.ressurser;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.web.rest.ressurser.SoknadRessurs.XSRF_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoknadRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "Hans og Grete";

    @Mock
    private SoknadService soknadService;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Mock
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private HenvendelseService henvendelseService;

    @InjectMocks
    private SoknadRessurs ressurs;

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void skalSetteXsrfToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        ressurs.hentXsrfCookie(BEHANDLINGSID, response);
        verify(response,times(2)).addCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo(XSRF_TOKEN + "-123");
    }

    @Test
    void opprettingAvSoknadSkalSetteXsrfToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        ressurs.opprettSoknad(null, response, "");
        verify(response,times(2)).addCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo(XSRF_TOKEN + "-null");
    }

    @Test
    void opprettSoknadUtenBehandlingsidSkalStarteNySoknad() {
        ressurs.opprettSoknad(null, mock(HttpServletResponse.class), "");
        verify(soknadService).startSoknad("");
    }

    @Test
    void opprettSoknadMedBehandlingsidSomIkkeHarEttersendingSkalStarteNyEttersending() {
        when(soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(anyString(), anyString())).thenReturn(Optional.empty());
        ressurs.opprettSoknad(BEHANDLINGSID, mock(HttpServletResponse.class), "");
        verify(soknadService).startEttersending(eq(BEHANDLINGSID));
    }

    @Test
    void opprettSoknadMedBehandlingsidSomHarEttersendingSkalIkkeStarteNyEttersending() {
        when(soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(eq(BEHANDLINGSID), anyString())).thenReturn(
                Optional.of(new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))));
        ressurs.opprettSoknad(BEHANDLINGSID, mock(HttpServletResponse.class), "");
        verify(soknadService, never()).startEttersending(eq(BEHANDLINGSID));
    }

    @Test
    void oppdaterSamtykkerMedTomListaSkalIkkeForeTilNoenSamtykker() {
        List<BekreftelseRessurs> samtykkeListe = Lists.emptyList();
        String token = "token";

        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token);

        verify(soknadService, only()).oppdaterSamtykker(BEHANDLINGSID, false, false, token);
    }

    @Test
    void oppdaterSamtykkerSkalGiSamtykkerFraLista() {
        BekreftelseRessurs bekreftelse1 = new BekreftelseRessurs().withType(BOSTOTTE_SAMTYKKE).withVerdi(true);
        BekreftelseRessurs bekreftelse2 = new BekreftelseRessurs().withType(UTBETALING_SKATTEETATEN_SAMTYKKE).withVerdi(true);
        List<BekreftelseRessurs> samtykkeListe = Lists.newArrayList(bekreftelse1, bekreftelse2);
        String token = "token";

        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token);

        verify(soknadService, only()).oppdaterSamtykker(BEHANDLINGSID, true, true, token);
    }

    @Test
    void oppdaterSamtykkerSkalGiSamtykkerFraLista_menKunDersomVerdiErSann() {
        BekreftelseRessurs bekreftelse1 = new BekreftelseRessurs().withType(BOSTOTTE_SAMTYKKE).withVerdi(true);
        BekreftelseRessurs bekreftelse2 = new BekreftelseRessurs().withType(UTBETALING_SKATTEETATEN_SAMTYKKE).withVerdi(false);
        List<BekreftelseRessurs> samtykkeListe = Lists.newArrayList(bekreftelse1, bekreftelse2);
        String token = "token";

        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token);

        verify(soknadService, only()).oppdaterSamtykker(BEHANDLINGSID, true, false, token);
    }

    @Test
    void hentSamtykker_skalReturnereTomListeNarViIkkeHarNoenSamtykker() {
        when(soknadUnderArbeidRepository.hentSoknad(eq(BEHANDLINGSID), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
        String token = "token";

        List<BekreftelseRessurs> bekreftelseRessurser = ressurs.hentSamtykker(BEHANDLINGSID, token);

        assertThat(bekreftelseRessurser).isEmpty();
    }

    @Test
    void hentSamtykker_skalReturnereListeMedSamtykker() {
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
        assertThat(bekreftelse1.verdi).isTrue();
        BekreftelseRessurs bekreftelse2 = bekreftelseRessurser.get(1);
        assertThat(bekreftelse2.type).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        assertThat(bekreftelse2.verdi).isTrue();
    }

    @Test
    void hentSamtykker_skalReturnereListeMedSamtykker_tarBortDeUtenSattVerdi() {
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
        assertThat(bekreftelse1.verdi).isTrue();
    }

    @Test
    void getXsrfCookieSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserBrukerHarTilgangTilSoknad(BEHANDLINGSID);

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> ressurs.hentXsrfCookie(BEHANDLINGSID, response));

        verifyNoInteractions(henvendelseService);
    }

    @Test
    void getErSystemdataEndretSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> ressurs.sjekkOmSystemdataErEndret(BEHANDLINGSID, "token"));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    void oppdaterSamtykkerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> ressurs.oppdaterSamtykker(BEHANDLINGSID, Collections.emptyList(), "token"));

        verifyNoInteractions(soknadService);
    }

    @Test
    void getSamtykkerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> ressurs.hentSamtykker(BEHANDLINGSID, "token"));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    void opprettSoknadSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> ressurs.opprettSoknad(BEHANDLINGSID, response, "token"));

        verifyNoInteractions(soknadService);
    }
}