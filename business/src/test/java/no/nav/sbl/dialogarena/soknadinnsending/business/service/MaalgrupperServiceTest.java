package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.FinnMaalgruppeinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.FinnMaalgruppeinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.MaalgruppeinformasjonV1;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.informasjon.WSMaalgruppe;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.meldinger.WSFinnMaalgruppeinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.meldinger.WSFinnMaalgruppeinformasjonListeResponse;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

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
    public static final String FODSELSNUMMER = "00000000000";

    @Before
    public void setUp() throws Exception {
        when(webservice.finnMaalgruppeinformasjonListe(any(WSFinnMaalgruppeinformasjonListeRequest.class)))
                .thenReturn(new WSFinnMaalgruppeinformasjonListeResponse());
    }

    @Test
    public void finnMaalgruppeinformasjonListeKallesMedFodselsnummer() throws FinnMaalgruppeinformasjonListePersonIkkeFunnet, FinnMaalgruppeinformasjonListeSikkerhetsbegrensning {
        maalgrupperService.hentMaalgrupper(FODSELSNUMMER);
        verify(webservice).finnMaalgruppeinformasjonListe(argument.capture());
        assertThat(argument.getValue().getPersonident()).isEqualTo(FODSELSNUMMER);
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
        maalgrupperService.hentMaalgrupper(FODSELSNUMMER);
        verify(webservice).finnMaalgruppeinformasjonListe(argument.capture());
        assertThat(argument.getValue().getPeriode().getFom()).isEqualTo(new LocalDate("2015-01-01"));
    }

    @Test
    public void skalReturnereListeAvMaalgruppeFakta() throws Exception {
        WSMaalgruppe maalgruppe = lagMaalgruppe("", new LocalDate());
        WSMaalgruppe maalgruppe2 = lagMaalgruppe("", new LocalDate());
        when(webservice.finnMaalgruppeinformasjonListe(any(WSFinnMaalgruppeinformasjonListeRequest.class)))
                .thenReturn(lagMaalgruppeRequest(maalgruppe, maalgruppe2));

        List<Faktum> fakta = maalgrupperService.hentMaalgrupper(FODSELSNUMMER);
        assertThat(fakta).hasSize(2);
        for (Faktum faktum : fakta) {
            assertThat(faktum.getKey()).isEqualTo("maalgruppe");
        }
    }

    @Test
    public void skalReturnereListeAvFaktumMedRettInnhold() throws Exception {
        String arbeidssoker = "Arbeidssøker";
        String ensligForsorger = "Enslig forsørger arbeidssøker";
        WSMaalgruppe maalgruppe = lagMaalgruppe(arbeidssoker, new LocalDate("2015-02-02"));
        WSMaalgruppe maalgruppe2 = lagMaalgruppe(ensligForsorger, new LocalDate("2015-03-02"));

        when(webservice.finnMaalgruppeinformasjonListe(any(WSFinnMaalgruppeinformasjonListeRequest.class)))
                .thenReturn(lagMaalgruppeRequest(maalgruppe, maalgruppe2));

        List<Faktum> fakta = maalgrupperService.hentMaalgrupper(FODSELSNUMMER);
        assertThat(fakta.get(0).getProperties()).containsEntry("navn", arbeidssoker);
        assertThat(fakta.get(0).getProperties()).containsEntry("fom", "2015-02-02");

        assertThat(fakta.get(1).getProperties()).containsEntry("navn", ensligForsorger);
        assertThat(fakta.get(1).getProperties()).containsEntry("fom", "2015-03-02");
    }

    private WSFinnMaalgruppeinformasjonListeResponse lagMaalgruppeRequest(WSMaalgruppe... maalgrupper) {
        return new WSFinnMaalgruppeinformasjonListeResponse()
                .withMaalgruppeListe(maalgrupper);
    }

    private WSMaalgruppe lagMaalgruppe(String maalgruppenavn, LocalDate fom) {
        return new WSMaalgruppe()
                .withMaalgruppenavn(maalgruppenavn)
                .withGyldighetsperiode(new WSPeriode().withFom(fom));
    }
}
