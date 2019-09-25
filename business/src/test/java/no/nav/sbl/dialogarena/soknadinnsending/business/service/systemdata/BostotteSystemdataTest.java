package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.BostotteImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.SakerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.VedtakDto;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningSak;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.Bostotte.HUSBANKEN_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BostotteSystemdataTest {
    private static final String EIER = "12345678910";

    @Mock
    private BostotteImpl bostotte;

    @InjectMocks
    private BostotteSystemdata bostotteSystemdata;

    @Test
    public void updateSystemdata_soknadBlirOppdatertMedUtbetalingFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        String mottaker = "Ola Normann";
        BigDecimal belop = BigDecimal.valueOf(10000);
        LocalDate utbetalingsDato = LocalDate.now();
        BostotteDto bostotteDto = new BostotteDto().withUtbetaling(new UtbetalingerDto().with(mottaker, belop, utbetalingsDato));

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalinger).isNotEmpty();
        assertThat(utbetalinger).hasSize(1);
        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalinger.get(0);
        assertThatUtbetalingErKorrekt(mottaker, belop, utbetaling, utbetalingsDato);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    public void updateSystemdata_soknadBlirOppdatertMedToUtbetalingerFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        String mottaker = "Ola Normann";
        BigDecimal belop1 = BigDecimal.valueOf(10000);
        BigDecimal belop2 = BigDecimal.valueOf(20000);
        LocalDate utbetalingsDato = LocalDate.now();
        BostotteDto bostotteDto = new BostotteDto()
                .withUtbetaling(new UtbetalingerDto().with(mottaker, belop1, utbetalingsDato.minusMonths(1)))
                .withUtbetaling(new UtbetalingerDto().with(mottaker, belop2, utbetalingsDato));

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalinger).isNotEmpty();
        assertThat(utbetalinger).hasSize(2);
        JsonOkonomiOpplysningUtbetaling utbetaling1 = utbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling2 = utbetalinger.get(1);
        assertThatUtbetalingErKorrekt(mottaker, belop1, utbetaling1, utbetalingsDato.minusMonths(1));
        assertThatUtbetalingErKorrekt(mottaker, belop2, utbetaling2, utbetalingsDato);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    public void updateSystemdata_soknadBlirOppdatertMedSakFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        LocalDate saksDato = LocalDate.now().withDayOfMonth(1);
        String status = "UNDER_BEHANDLING";
        String rolle = "HOVEDPERSON";
        BostotteDto bostotteDto = new BostotteDto()
                .withSak(new SakerDto().with(saksDato.getMonthValue(), saksDato.getYear(), status, null, rolle));

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getSak();
        assertThat(saker).isNotEmpty();
        assertThat(saker).hasSize(1);
        JsonOkonomiOpplysningSak sak = saker.get(0);
        assertThat(sak.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(sak.getType()).isEqualTo(HUSBANKEN_TYPE);
        assertThat(sak.getDato()).isEqualTo(saksDato.toString());
        assertThat(sak.getStatus()).isEqualTo(status);
        assertThat(sak.getBeskrivelse()).isNull();
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    public void updateSystemdata_soknadBlirOppdatertMedToSakerFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        LocalDate saksDato1 = LocalDate.now().withDayOfMonth(1);
        LocalDate saksDato2 = LocalDate.now().withDayOfMonth(1);
        String status1 = "UNDER_BEHANDLING";
        String status2 = "VEDTATT";
        String rolle = "HOVEDPERSON";
        String beskrivelse2 = "Avslag - For høy inntekt";
        VedtakDto vedtakDto = new VedtakDto().with("V02", beskrivelse2);
        BostotteDto bostotteDto = new BostotteDto()
                .withSak(new SakerDto().with(saksDato1.getMonthValue(), saksDato1.getYear(), status1, null, rolle))
                .withSak(new SakerDto().with(saksDato2.getMonthValue(), saksDato2.getYear(), status2, vedtakDto, rolle));

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getSak();
        assertThat(saker).isNotEmpty();
        assertThat(saker).hasSize(2);
        JsonOkonomiOpplysningSak sak1 = saker.get(0);
        JsonOkonomiOpplysningSak sak2 = saker.get(1);
        assertThat(sak1.getDato()).isEqualTo(saksDato1.toString());
        assertThat(sak1.getStatus()).isEqualTo(status1);
        assertThat(sak1.getBeskrivelse()).isNull();
        assertThat(sak2.getDato()).isEqualTo(saksDato2.toString());
        assertThat(sak2.getStatus()).isEqualTo(status2);
        assertThat(sak2.getBeskrivelse()).isEqualTo(beskrivelse2);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    public void updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(null);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getSak();
        assertThat(saker).isEmpty();
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isTrue();
    }

    private void assertThatUtbetalingErKorrekt(String mottaker, BigDecimal belop, JsonOkonomiOpplysningUtbetaling utbetaling, LocalDate utbetalingsDato) {
        assertThat(utbetaling.getTittel()).endsWith(mottaker);
        assertThat(utbetaling.getType()).isEqualTo(HUSBANKEN_TYPE);
        assertThat(utbetaling.getUtbetalingsdato()).isEqualTo(utbetalingsDato.toString());
        assertThat(utbetaling.getBelop()).isEqualTo(belop.intValue());
        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(utbetaling.getOverstyrtAvBruker()).isFalse();
    }
}
