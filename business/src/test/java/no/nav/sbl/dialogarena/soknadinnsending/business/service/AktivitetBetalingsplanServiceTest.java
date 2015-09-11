package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.AktivitetServiceTest.lagAktivitetOgVedtak;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.AktivitetServiceTest.lagBetalingsplan;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.AktivitetServiceTest.lagVedtak;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AktivitetBetalingsplanServiceTest {
    @Mock
    SakOgAktivitetV1 webservice;
    @InjectMocks
    private AktivitetBetalingsplanService aktivitetService;

    @Test
    public void skalReturnererBetalingsplaner() throws FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet, FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning {
        WSFinnAktivitetOgVedtakDagligReiseListeResponse response = new WSFinnAktivitetOgVedtakDagligReiseListeResponse();
        response.withAktivitetOgVedtakListe(
                lagAktivitetOgVedtak("100", "navn på aktivitet",
                        lagVedtak(new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31), "1000", 100, true, 555.0,
                                lagBetalingsplan("321123", new LocalDate(2015, 1, 1), new LocalDate(2015, 1, 7), "1232312323"),
                                lagBetalingsplan("321124", new LocalDate(2015, 1, 7), new LocalDate(2015, 1, 14), null),
                                lagBetalingsplan("321125", new LocalDate(2015, 1, 14), new LocalDate(2015, 1, 21), null)
                        ),
                        lagVedtak(new LocalDate(2015, 4, 1), new LocalDate(2015, 5, 31), "1001", 101, true, 556.0,
                                lagBetalingsplan("321126", new LocalDate(2015, 1, 14), new LocalDate(2015, 1, 21), null)
                        )
                ),
                lagAktivitetOgVedtak("101", "navn på aktivitet2",
                        lagVedtak(new LocalDate(2015, 1, 1), new LocalDate(2015, 3, 31), "1000", null, false, 555.0)
                ));
        when(webservice.finnAktivitetOgVedtakDagligReiseListe(any(WSFinnAktivitetOgVedtakDagligReiseListeRequest.class))).thenReturn(response);

        List<Faktum> faktums = aktivitetService.hentBetalingsplanerForVedtak(10L, "12312312345", "100", "1000");
        assertThat(faktums).hasSize(3);
        assertThat(faktums).contains(new Faktum()
                        .medSoknadId(10L)
                        .medKey("vedtak.betalingsplan")
                        .medProperty("uniqueKey", "id")
                        .medProperty("id", "321123")
                        .medProperty("fom", "2015-01-01")
                        .medProperty("tom", "2015-01-07")
                        .medProperty("alleredeSokt", "true")
                        .medProperty("sokerForPeriode", "false")
        );
        assertThat(faktums).contains(new Faktum()
                        .medSoknadId(10L)
                        .medKey("vedtak.betalingsplan")
                        .medProperty("uniqueKey", "id")
                        .medProperty("id", "321124")
                        .medProperty("fom", "2015-01-07")
                        .medProperty("tom", "2015-01-14")
                        .medProperty("alleredeSokt", "false")
        );
        assertThat(faktums).contains(new Faktum()
                        .medSoknadId(10L)
                        .medKey("vedtak.betalingsplan")
                        .medProperty("uniqueKey", "id")
                        .medProperty("id", "321125")
                        .medProperty("fom", "2015-01-14")
                        .medProperty("tom", "2015-01-21")
                        .medProperty("alleredeSokt", "false")
        );

    }

}