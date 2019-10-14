package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.BostotteImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.*;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
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
        BostotteMottaker mottaker = BostotteMottaker.HUSSTAND;
        BigDecimal belop = BigDecimal.valueOf(10000);
        LocalDate utbetalingsDato = LocalDate.now();
        BostotteDto bostotteDto = new BostotteDto().withUtbetaling(new UtbetalingerDto().with(mottaker, belop, utbetalingsDato, BostotteRolle.HOVEDPERSON));

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
        BostotteMottaker mottaker = BostotteMottaker.HUSSTAND;
        BigDecimal belop1 = BigDecimal.valueOf(10000);
        BigDecimal belop2 = BigDecimal.valueOf(20000);
        LocalDate utbetalingsDato = LocalDate.now();
        BostotteDto bostotteDto = new BostotteDto()
                .withUtbetaling(new UtbetalingerDto().with(mottaker, belop1, utbetalingsDato.minusMonths(1), BostotteRolle.HOVEDPERSON))
                .withUtbetaling(new UtbetalingerDto().with(mottaker, belop2, utbetalingsDato, BostotteRolle.HOVEDPERSON));

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalinger).isNotEmpty();
        assertThat(utbetalinger).hasSize(1);
        JsonOkonomiOpplysningUtbetaling utbetaling1 = utbetalinger.get(0);
        assertThatUtbetalingErKorrekt(mottaker, belop2, utbetaling1, utbetalingsDato);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    public void updateSystemdata_soknadBlirOppdatertMedSakFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        SakerDto sakerDto = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null);
        BostotteDto bostotteDto = new BostotteDto()
                .withSak(sakerDto);

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getSak();
        assertThat(saker).isNotEmpty();
        assertThat(saker).hasSize(1);
        JsonOkonomiOpplysningSak sak = saker.get(0);
        assertThat(sak.getKilde()).isEqualTo(JsonKildeSystem.SYSTEM);
        assertThat(sak.getType()).isEqualTo(HUSBANKEN_TYPE);
        assertThat(sak.getDato()).isEqualTo(sakerDto.getDato().toString());
        assertThat(sak.getStatus()).isEqualToIgnoringCase(sakerDto.getStatus().toString());
        assertThat(sak.getBeskrivelse()).isNull();
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    public void updateSystemdata_soknadBlirOppdatertMedToSakerFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        SakerDto sakerDto1 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null);
        SakerDto sakerDto2 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.VEDTATT, BostotteRolle.HOVEDPERSON, "V02", "Avslag - For høy inntekt");
        BostotteDto bostotteDto = new BostotteDto()
                .withSak(sakerDto1)
                .withSak(sakerDto2);

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getSak();
        assertThat(saker).isNotEmpty();
        assertThat(saker).hasSize(2);
        JsonOkonomiOpplysningSak sak1 = saker.get(0);
        JsonOkonomiOpplysningSak sak2 = saker.get(1);
        assertThat(sak1.getDato()).isEqualTo(sakerDto1.getDato().toString());
        assertThat(sak1.getStatus()).isEqualToIgnoringCase(sakerDto1.getStatus().toString());
        assertThat(sak1.getBeskrivelse()).isNull();
        assertThat(sak2.getDato()).isEqualTo(sakerDto2.getDato().toString());
        assertThat(sak2.getStatus()).isEqualToIgnoringCase(sakerDto2.status.toString());
        assertThat(sak2.getBeskrivelse()).isEqualTo(sakerDto2.getVedtak().getBeskrivelse());
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

    @Test
    public void updateSystemdata_saker_bipersonerBlirFiltrertBort() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        SakerDto sakerDto1 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null);
        SakerDto sakerDto2 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.VEDTATT, BostotteRolle.BIPERSON, "V02", "Avslag - For høy inntekt");
        BostotteDto bostotteDto = new BostotteDto()
                .withSak(sakerDto1)
                .withSak(sakerDto2);

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getSak();
        assertThat(saker).isNotEmpty();
        assertThat(saker).hasSize(1);
        JsonOkonomiOpplysningSak sak1 = saker.get(0);
        assertThat(sak1.getStatus()).isEqualToIgnoringCase(sakerDto1.getStatus().toString());
    }

    @Test
    public void updateSystemdata_utbetalinger_bipersonerBlirFiltrertBort() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        UtbetalingerDto utbetalingerDto1 = new UtbetalingerDto().with(BostotteMottaker.KOMMUNE, BigDecimal.valueOf(10000), LocalDate.now().minusMonths(1), BostotteRolle.HOVEDPERSON);
        UtbetalingerDto utbetalingerDto2 = new UtbetalingerDto().with(BostotteMottaker.HUSSTAND, BigDecimal.valueOf(20000), LocalDate.now().minusMonths(1), BostotteRolle.BIPERSON);
        BostotteDto bostotteDto = new BostotteDto()
                .withUtbetaling(utbetalingerDto1)
                .withUtbetaling(utbetalingerDto2);

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalinger).isNotEmpty();
        assertThat(utbetalinger).hasSize(1);
        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalinger.get(0);
        assertThat(utbetaling.getBelop()).isEqualTo(utbetalingerDto1.getBelop().longValue());
    }

    @Test
    public void updateSystemdata_bareDataFraSisteManedBlirVist() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        SakerDto sakerDto1 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null);
        SakerDto sakerDto2 = lagSak(LocalDate.now().withDayOfMonth(1).minusMonths(1), BostotteStatus.VEDTATT, BostotteRolle.HOVEDPERSON, "V02", "Avslag - For høy inntekt");
        BostotteDto bostotteDto = new BostotteDto()
                .withSak(sakerDto1)
                .withSak(sakerDto2);

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getSak();
        assertThat(saker).isNotEmpty();
        assertThat(saker).hasSize(1);
        JsonOkonomiOpplysningSak sak1 = saker.get(0);
        assertThat(sak1.getStatus()).isEqualToIgnoringCase(sakerDto1.getStatus().toString());
    }

    @Test
    public void updateSystemdata_dataFraDeSisteToManederBlirVistNarSisteManedErTom() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        SakerDto sakerDto2 = lagSak(LocalDate.now().withDayOfMonth(1).minusMonths(1), BostotteStatus.VEDTATT, BostotteRolle.HOVEDPERSON, "V02", "Avslag - For høy inntekt");
        BostotteDto bostotteDto = new BostotteDto()
                .withSak(sakerDto2);

        // Mock:
        when(bostotte.hentBostotte(any(), any(), any(), any())).thenReturn(bostotteDto);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getSak();
        assertThat(saker).isNotEmpty();
        assertThat(saker).hasSize(1);
        JsonOkonomiOpplysningSak sak1 = saker.get(0);
        assertThat(sak1.getStatus()).isEqualToIgnoringCase(sakerDto2.getStatus().toString());
    }

    private SakerDto lagSak(LocalDate saksDato, BostotteStatus status, BostotteRolle rolle, String kode, String beskrivelse) {
        VedtakDto vedtakDto = null;
        if(kode != null) {
            vedtakDto = new VedtakDto().with(kode, beskrivelse);
        }
        return new SakerDto().with(saksDato.getMonthValue(), saksDato.getYear(), status, vedtakDto, rolle);
    }

    private void assertThatUtbetalingErKorrekt(BostotteMottaker mottaker, BigDecimal belop, JsonOkonomiOpplysningUtbetaling utbetaling, LocalDate utbetalingsDato) {
        assertThat(utbetaling.getTittel()).isEqualToIgnoringCase("Statlig bostotte");
        assertThat(utbetaling.getMottaker()).isEqualToIgnoringCase(mottaker.toString());
        assertThat(utbetaling.getType()).isEqualTo(HUSBANKEN_TYPE);
        assertThat(utbetaling.getUtbetalingsdato()).isEqualTo(utbetalingsDato.toString());
        assertThat(utbetaling.getBelop()).isEqualTo(belop.intValue());
        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(utbetaling.getOverstyrtAvBruker()).isFalse();
    }
}
