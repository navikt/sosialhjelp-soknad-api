package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.FinnMaalgruppeinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.FinnMaalgruppeinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.MaalgruppeV1;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSMaalgruppe;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSMaalgruppetyper;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.meldinger.WSFinnMaalgruppeinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.meldinger.WSFinnMaalgruppeinformasjonListeResponse;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MaalgrupperServiceTest {

    @Mock
    private MaalgruppeV1 webservice;

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
        assertThat(maalgrupperService.hentMaalgrupper("")).isEmpty();
    }

    @Test
    public void finnMaalgruppeinformasjonListeKallesMedPeriode() throws Exception {
        maalgrupperService.hentMaalgrupper(FODSELSNUMMER);
        verify(webservice).finnMaalgruppeinformasjonListe(argument.capture());
        assertThat(argument.getValue().getPeriode().getFom()).isEqualTo(LocalDate.now().minusMonths(6));
        assertThat(argument.getValue().getPeriode().getTom()).isEqualTo(LocalDate.now().plusMonths(2));
    }

    @Test
    public void skalReturnereListeAvMaalgruppeFakta() throws Exception {
        WSMaalgruppe maalgruppe = lagMaalgruppe("", "", new LocalDate(), new LocalDate());
        WSMaalgruppe maalgruppe2 = lagMaalgruppe("", "", new LocalDate(), new LocalDate());
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
        String arbeidssokerKodeverkVerdi = "ARBSOKERE";
        String fomArbeidssoker = "2015-02-02";
        String tomArbeidssoker = "2015-07-20";
        WSMaalgruppe arbeidssokerMaalgruppe = lagMaalgruppe(arbeidssoker, arbeidssokerKodeverkVerdi, fomArbeidssoker, tomArbeidssoker);

        String ensligForsorger = "Enslig forsørger arbeidssøker";
        String ensligForsorgerKodeverVerdi = "ENSFORARBS";
        String fomEnsligForsorger = "2015-03-02";
        String tomEnsligForsorger = "2015-07-22";
        WSMaalgruppe ensligForsorgerMaalgruppe = lagMaalgruppe(ensligForsorger, ensligForsorgerKodeverVerdi, fomEnsligForsorger, tomEnsligForsorger);

        when(webservice.finnMaalgruppeinformasjonListe(any(WSFinnMaalgruppeinformasjonListeRequest.class)))
                .thenReturn(lagMaalgruppeRequest(arbeidssokerMaalgruppe, ensligForsorgerMaalgruppe));

        List<Faktum> fakta = maalgrupperService.hentMaalgrupper(FODSELSNUMMER);
        Map<String, String> arbeidssokerProperties = fakta.get(0).getProperties();
        assertThat(arbeidssokerProperties).containsEntry("navn", arbeidssoker);
        assertThat(arbeidssokerProperties).containsEntry("fom", fomArbeidssoker);
        assertThat(arbeidssokerProperties).containsEntry("tom", tomArbeidssoker);
        assertThat(arbeidssokerProperties).containsEntry("kodeverkVerdi", arbeidssokerKodeverkVerdi);

        Map<String, String> ensligForsorgerProperties = fakta.get(1).getProperties();
        assertThat(ensligForsorgerProperties).containsEntry("navn", ensligForsorger);
        assertThat(ensligForsorgerProperties).containsEntry("fom", fomEnsligForsorger);
        assertThat(ensligForsorgerProperties).containsEntry("tom", tomEnsligForsorger);
        assertThat(ensligForsorgerProperties).containsEntry("kodeverkVerdi", ensligForsorgerKodeverVerdi);
    }

    @Test
    public void trengerIkkeTilOgMedDatoForMaalgruppe() throws Exception {
        WSMaalgruppe maalgruppe = lagMaalgruppe("", "", new LocalDate());
        when(webservice.finnMaalgruppeinformasjonListe(any(WSFinnMaalgruppeinformasjonListeRequest.class)))
                .thenReturn(lagMaalgruppeRequest(maalgruppe));

        List<Faktum> fakta = maalgrupperService.hentMaalgrupper(FODSELSNUMMER);
        assertThat(fakta.get(0).getProperties()).containsEntry("tom", "");
    }

    private WSFinnMaalgruppeinformasjonListeResponse lagMaalgruppeRequest(WSMaalgruppe... maalgrupper) {
        return new WSFinnMaalgruppeinformasjonListeResponse()
                .withMaalgruppeListe(maalgrupper);
    }

    private WSMaalgruppe lagMaalgruppe(String maalgruppenavn, String kodeverkVerdi, String fom, String tom) {
        LocalDate fomDate = new LocalDate(fom);
        LocalDate tomDate = new LocalDate(tom);
        return lagMaalgruppe(maalgruppenavn, kodeverkVerdi, fomDate, tomDate);
    }

    private WSMaalgruppe lagMaalgruppe(String maalgruppenavn, String kodeverkVerdi, LocalDate fomDate, LocalDate tomDate) {
        return new WSMaalgruppe()
                .withMaalgruppenavn(maalgruppenavn)
                .withGyldighetsperiode(new WSPeriode().withFom(fomDate).withTom(tomDate))
                .withMaalgruppetype(new WSMaalgruppetyper().withValue(kodeverkVerdi));
    }

    private WSMaalgruppe lagMaalgruppe(String maalgruppenavn, String kodeverkVerdi, LocalDate fomDate) {
        return new WSMaalgruppe()
                .withMaalgruppenavn(maalgruppenavn)
                .withGyldighetsperiode(new WSPeriode().withFom(fomDate))
                .withMaalgruppetype(new WSMaalgruppetyper().withValue(kodeverkVerdi));
    }
}
