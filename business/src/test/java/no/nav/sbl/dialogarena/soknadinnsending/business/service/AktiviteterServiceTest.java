package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetsinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetsinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSAktivitet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AktiviteterServiceTest {

    @InjectMocks
    private AktiviteterService aktiviteterService;

    @Mock
    SakOgAktivitetV1 webservice;

    @Captor
    ArgumentCaptor<WSFinnAktivitetsinformasjonListeRequest> argument;

    @Test
    public void skalKallePaWebService() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fodselnummer = "***REMOVED***";

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(new WSFinnAktivitetsinformasjonListeResponse());
        aktiviteterService.hentAktiviteter(fodselnummer);
        verify(webservice).finnAktivitetsinformasjonListe(argument.capture());
        assertThat(argument.getValue().getPersonident()).isEqualTo(fodselnummer);
    }

    @Test
    public void skalReturnereFaktumVedUthenting() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fodselnummer = "***REMOVED***";
        String aktivitetsnavn = "aktivitetsnavn";
        String id = "9999";
        String fom = "2015-02-15";
        String tom = "2015-02-28";

        WSPeriode periode = new WSPeriode().withFom(new LocalDate(fom)).withTom(new LocalDate(tom));
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withAktivitetsnavn(aktivitetsnavn).withAktivitetId(id).withPeriode(periode).withErStoenadsberettigetAktivitet(true));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);

        List<Faktum> fakta = aktiviteterService.hentAktiviteter(fodselnummer);

        assertThat(fakta).hasSize(1);
        Faktum faktum = fakta.get(0);
        assertThat(faktum.getKey()).isEqualTo("aktivitet");
        assertThat(faktum.getProperties()).containsEntry("id", id);
        assertThat(faktum.getProperties()).containsEntry("navn", aktivitetsnavn);
        assertThat(faktum.getProperties()).containsEntry("fom", fom);
        assertThat(faktum.getProperties()).containsEntry("tom", tom);
    }

    @Test
    public void skalFiltrereBortAktiviteterSomIkkeErStonadsberettiget() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fodselnummer = "***REMOVED***";
        String aktivitetsnavn = "aktivitetsnavn";
        String id = "9999";
        String fom = "2015-02-15";
        String tom = "2015-02-28";

        WSPeriode periode = new WSPeriode().withFom(new LocalDate(fom)).withTom(new LocalDate(tom));
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withAktivitetsnavn(aktivitetsnavn).withAktivitetId(id).withPeriode(periode).withErStoenadsberettigetAktivitet(true),
                new WSAktivitet().withAktivitetsnavn(aktivitetsnavn).withAktivitetId("8888").withPeriode(periode).withErStoenadsberettigetAktivitet(false));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);
        List<Faktum> fakta = aktiviteterService.hentAktiviteter(fodselnummer);
        assertThat(fakta).hasSize(1);
        assertThat(fakta.get(0).getProperties().get("id")).isEqualToIgnoringCase("9999");

    }

    @Test
    public void skalReturnereFaktumUtenTom() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fom = "2015-02-15";

        WSPeriode periode = new WSPeriode().withFom(new LocalDate(fom));
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withPeriode(periode).withErStoenadsberettigetAktivitet(true));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);

        List<Faktum> fakta = aktiviteterService.hentAktiviteter("***REMOVED***");

        Faktum faktum = fakta.get(0);
        assertThat(faktum.getProperties()).containsEntry("fom", fom);
        assertThat(faktum.getProperties()).containsEntry("tom", "");
    }

    @Test
    public void skalReturnereFaktumUtenNoenPeriodedatoer() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withPeriode(new WSPeriode()).withErStoenadsberettigetAktivitet(true));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);

        List<Faktum> fakta = aktiviteterService.hentAktiviteter("***REMOVED***");

        Faktum faktum = fakta.get(0);
        assertThat(faktum.getProperties()).containsEntry("fom", "");
    }
    @Test
    public void skalGodtaNullListe() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(null);

        List<Faktum> fakta = aktiviteterService.hentAktiviteter("***REMOVED***");
        assertThat(fakta).isEmpty();
    }

    @Test(expected = RuntimeException.class)
    public void skalKasteRuntimeExceptionVedWsFeil() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenThrow(new FinnAktivitetsinformasjonListeSikkerhetsbegrensning());

        aktiviteterService.hentAktiviteter("");
    }

}