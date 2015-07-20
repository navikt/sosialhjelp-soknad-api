package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.FinnMaalgruppeinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.FinnMaalgruppeinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.MaalgruppeinformasjonV1;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.meldinger.WSFinnMaalgruppeinformasjonListeRequest;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MaalgrupperServiceTest {

    @Mock
    private MaalgruppeinformasjonV1 webservice;

    @InjectMocks
    private MaalgrupperService maalgrupperService;

    @Captor
    private ArgumentCaptor<WSFinnMaalgruppeinformasjonListeRequest> argument;

    @Test
    public void finnMaalgruppeinformasjonListeKallesMedFodselsnummer() throws FinnMaalgruppeinformasjonListePersonIkkeFunnet, FinnMaalgruppeinformasjonListeSikkerhetsbegrensning {
        String fodselsnummer = "00000000000";
        maalgrupperService.hentMaalgrupper(fodselsnummer);
        verify(webservice).finnMaalgruppeinformasjonListe(argument.capture());
        assertThat(argument.getValue().getPersonident()).isEqualTo(fodselsnummer);
    }

    @Test(expected = RuntimeException.class)
    public void skalSendeVidereRuntimeExceptionVedSikkerhetsbegrensning() throws Exception {
        when(webservice.finnMaalgruppeinformasjonListe(any(WSFinnMaalgruppeinformasjonListeRequest.class)))
                .thenThrow(new FinnMaalgruppeinformasjonListeSikkerhetsbegrensning());
        maalgrupperService.hentMaalgrupper("");
    }

    @Test
    public void skalSendeVidereRuntimeExceptionVedPersonIkkeFunnet() throws Exception {
        when(webservice.finnMaalgruppeinformasjonListe(any(WSFinnMaalgruppeinformasjonListeRequest.class)))
                .thenThrow(new FinnMaalgruppeinformasjonListePersonIkkeFunnet());
        try {
            maalgrupperService.hentMaalgrupper("");
            fail("Forventet exception");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause()).isInstanceOf(FinnMaalgruppeinformasjonListePersonIkkeFunnet.class);
        }
    }

    @Test
    public void finnMaalgruppeinformasjonListeKallesMedPeriode() throws Exception {
        String fodselsnummer = "00000000000";
        maalgrupperService.hentMaalgrupper(fodselsnummer);
        verify(webservice).finnMaalgruppeinformasjonListe(argument.capture());
        assertThat(argument.getValue().getPeriode().getFom()).isEqualTo(new LocalDate("2015-01-01"));
    }
}
