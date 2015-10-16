package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.FinnAktivitetsinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.FinnAktivitetsinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.SakOgAktivitetInformasjonV1;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.informasjon.WSAktivitet;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.assertj.core.data.MapEntry;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
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
    SakOgAktivitetInformasjonV1 webservice;

    @Captor
    ArgumentCaptor <WSFinnAktivitetsinformasjonListeRequest> argument;

    @Test
    public void skalKallePaWebService() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fodselnummer = "01010111111";

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(new WSFinnAktivitetsinformasjonListeResponse());
        aktiviteterService.hentAktiviteter(fodselnummer);
        verify(webservice).finnAktivitetsinformasjonListe(argument.capture());
        assertThat(argument.getValue().getPersonident()).isEqualTo(fodselnummer);
    }

    @Test
    public void skalReturnereFaktumVedUthenting() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fodselnummer = "01010111111";
        String aktivitetsnavn = "aktivitetsnavn";
        String id = "9999";
        String fom = "2015-02-15";
        String tom = "2015-02-28";

        WSPeriode periode = new WSPeriode().withFom(new LocalDate(fom)).withTom(new LocalDate(tom));
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withAktivitetsnavn(aktivitetsnavn).withAktivitetId(id).withPeriode(periode));

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
    public void skalReturnereFaktumUtenTom() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        String fom = "2015-02-15";

        WSPeriode periode = new WSPeriode().withFom(new LocalDate(fom));
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withPeriode(periode));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);

        List<Faktum> fakta = aktiviteterService.hentAktiviteter("01010111111");

        Faktum faktum = fakta.get(0);
        assertThat(faktum.getProperties()).containsEntry("fom", fom);
        assertThat(faktum.getProperties()).containsEntry("tom", "");
    }

    @Test
    public void skalReturnereFaktumUtenNoenPeriodedatoer() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
        response.withAktivitetListe(new WSAktivitet().withPeriode(new WSPeriode()));

        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);

        List<Faktum> fakta = aktiviteterService.hentAktiviteter("01010111111");

        Faktum faktum = fakta.get(0);
        assertThat(faktum.getProperties()).containsEntry("fom", "");
    }

    @Test(expected = RuntimeException.class)
    public void skalKasteRuntimeExceptionVedWsFeil() throws FinnAktivitetsinformasjonListePersonIkkeFunnet, FinnAktivitetsinformasjonListeSikkerhetsbegrensning {
        when(webservice.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenThrow(new FinnAktivitetsinformasjonListeSikkerhetsbegrensning());

        aktiviteterService.hentAktiviteter("");
    }

}