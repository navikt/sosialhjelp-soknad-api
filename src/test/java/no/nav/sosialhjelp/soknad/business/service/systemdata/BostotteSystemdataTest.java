package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.SakDto;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.UtbetalingDto;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.VedtakDto;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteMottaker;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteRolle;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteStatus;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak.Vedtaksstatus.AVSLAG;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class BostotteSystemdataTest {
    private static final String EIER = "12345678910";

    @Mock
    private HusbankenClient husbankenClient;

    @Mock
    private TextService textService;

    @InjectMocks
    private BostotteSystemdata bostotteSystemdata;

    @Test
    void updateSystemdata_soknadBlirOppdatertMedUtbetalingFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);
        BostotteMottaker mottaker = BostotteMottaker.HUSSTAND;
        BigDecimal netto = BigDecimal.valueOf(10000.5);
        LocalDate utbetalingsDato = LocalDate.now();
        BostotteDto bostotteDto = new BostotteDto(emptyList(), List.of(new UtbetalingDto(utbetalingsDato, netto, mottaker, BostotteRolle.HOVEDPERSON)));

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalinger).hasSize(1);
        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalinger.get(0);
        assertThatUtbetalingErKorrekt(mottaker, netto, utbetaling, utbetalingsDato);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    void updateSystemdata_soknadBlirOppdatertMedToUtbetalingerFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);
        BostotteMottaker mottaker = BostotteMottaker.HUSSTAND;
        BigDecimal netto1 = BigDecimal.valueOf(10000);
        BigDecimal netto2 = BigDecimal.valueOf(20000);
        LocalDate utbetalingsDato = LocalDate.now();
        BostotteDto bostotteDto = new BostotteDto(
                emptyList(),
                List.of(
                        new UtbetalingDto(utbetalingsDato.minusDays(32), netto1, mottaker, BostotteRolle.HOVEDPERSON), 
                        new UtbetalingDto(utbetalingsDato, netto2, mottaker, BostotteRolle.HOVEDPERSON)
                )
        );

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalinger).hasSize(1);
        JsonOkonomiOpplysningUtbetaling utbetaling1 = utbetalinger.get(0);
        assertThatUtbetalingErKorrekt(mottaker, netto2, utbetaling1, utbetalingsDato);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    void updateSystemdata_soknadBlirOppdatertMedSakFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);
        SakDto sakDto = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null, null);
        BostotteDto bostotteDto = new BostotteDto(List.of(sakDto), emptyList());

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonBostotteSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker).hasSize(1);
        JsonBostotteSak sak = saker.get(0);
        assertThat(sak.getKilde()).isEqualTo(JsonKildeSystem.SYSTEM);
        assertThat(sak.getType()).isEqualTo(UTBETALING_HUSBANKEN);
        assertThat(sak.getDato()).isEqualTo(LocalDate.of(sakDto.getAr(), sakDto.getMnd(), 1).toString());
        assertThat(sak.getStatus()).isEqualToIgnoringCase(sakDto.getStatus().toString());
        assertThat(sak.getBeskrivelse()).isNull();
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    void updateSystemdata_soknadBlirOppdatertMedToSakerFraHusbanken() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);
        SakDto sakDto1 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null, null);
        SakDto sakDto2 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.VEDTATT, BostotteRolle.HOVEDPERSON, "V02", "Avslag - For høy inntekt", AVSLAG);
        BostotteDto bostotteDto = new BostotteDto(List.of(sakDto1, sakDto2), emptyList());

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonBostotteSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker).hasSize(2);
        JsonBostotteSak sak1 = saker.get(0);
        JsonBostotteSak sak2 = saker.get(1);
        assertThat(sak1.getDato()).isEqualTo(LocalDate.of(sakDto1.getAr(), sakDto1.getMnd(), 1).toString());
        assertThat(sak1.getStatus()).isEqualToIgnoringCase(sakDto1.getStatus().toString());
        assertThat(sak1.getBeskrivelse()).isNull();
        assertThat(sak2.getDato()).isEqualTo(LocalDate.of(sakDto2.getAr(), sakDto2.getMnd(), 1).toString());
        assertThat(sak2.getStatus()).isEqualToIgnoringCase(sakDto2.getStatus().toString());
        assertThat(sak2.getBeskrivelse()).isEqualTo(sakDto2.getVedtak().getBeskrivelse());
        assertThat(sak2.getVedtaksstatus().value()).isEqualTo(sakDto2.getVedtak().getType());
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    void updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.empty());

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonBostotteSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker).isEmpty();
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isTrue();
    }

    @Test
    void updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil_ogBeholderGamleData() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        JsonOkonomiopplysninger opplysninger =
                soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        opplysninger.getBostotte().getSaker().add(
                new JsonBostotteSak()
                        .withType(UTBETALING_HUSBANKEN)
                        .withKilde(JsonKildeSystem.SYSTEM)
                        .withStatus(BostotteStatus.UNDER_BEHANDLING.toString()));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.empty());

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonBostotteSak> saker = opplysninger.getBostotte().getSaker();
        assertThat(saker).hasSize(1);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isTrue();
    }

    @Test
    void updateSystemdata_saker_henterIkkeBostotteUtenSamtykke() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), false);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonBostotteSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker).isEmpty();
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    void updateSystemdata_saker_fjernerGammelBostotteNarViIkkeHarSamtykke() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid1 = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid1.getJsonInternalSoknad(), true);
        SakDto sakDto = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null, null);
        BostotteDto bostotteDto = new BostotteDto(List.of(sakDto), emptyList());

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid1, "");

        List<JsonBostotteSak> saker1 = soknadUnderArbeid1.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker1).hasSize(1);

        // Kjøring:
        SoknadUnderArbeid soknadUnderArbeid2 = soknadUnderArbeid1;
        settBostotteSamtykkePaSoknad(soknadUnderArbeid2.getJsonInternalSoknad(), false);
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid2, "");

        List<JsonBostotteSak> saker2 = soknadUnderArbeid2.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker2).isEmpty();
    }

    @Test
    void updateSystemdata_saker_bipersonerBlirFiltrertBort() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);
        SakDto sakDto1 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null, null);
        SakDto sakDto2 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.VEDTATT, BostotteRolle.BIPERSON, "V02", "Avslag - For høy inntekt", AVSLAG);
        BostotteDto bostotteDto = new BostotteDto(List.of(sakDto1, sakDto2), emptyList());

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonBostotteSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker).hasSize(1);
        JsonBostotteSak sak1 = saker.get(0);
        assertThat(sak1.getStatus()).isEqualToIgnoringCase(sakDto1.getStatus().toString());
    }

    @Test
    void updateSystemdata_utbetalinger_bipersonerBlirFiltrertBort() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);
        UtbetalingDto utbetalingDto1 = new UtbetalingDto(LocalDate.now().minusDays(32), BigDecimal.valueOf(10000), BostotteMottaker.KOMMUNE, BostotteRolle.HOVEDPERSON);
        UtbetalingDto utbetalingDto2 = new UtbetalingDto(LocalDate.now().minusDays(32), BigDecimal.valueOf(20000), BostotteMottaker.HUSSTAND, BostotteRolle.BIPERSON);
        BostotteDto bostotteDto = new BostotteDto(emptyList(), List.of(utbetalingDto1, utbetalingDto2));

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalinger).hasSize(1);
        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalinger.get(0);
        assertThat(utbetaling.getNetto()).isEqualTo(utbetalingDto1.getBelop().longValue());
    }

    @Test
    void updateSystemdata_bareDataFraSisteManedBlirVist() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);
        SakDto sakDto1 = lagSak(LocalDate.now().withDayOfMonth(1), BostotteStatus.UNDER_BEHANDLING, BostotteRolle.HOVEDPERSON, null, null, null);
        SakDto sakDto2 = lagSak(LocalDate.now().withDayOfMonth(1).minusDays(32), BostotteStatus.VEDTATT, BostotteRolle.HOVEDPERSON, "V02", "Avslag - For høy inntekt", AVSLAG);
        BostotteDto bostotteDto = new BostotteDto(List.of(sakDto1, sakDto2), emptyList());

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonBostotteSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker).hasSize(1);
        JsonBostotteSak sak1 = saker.get(0);
        assertThat(sak1.getStatus()).isEqualToIgnoringCase(sakDto1.getStatus().toString());
    }

    @Test
    void updateSystemdata_dataFraDeSisteToManederBlirVistNarSisteManedErTom() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), true);
        LocalDate testDate;
        if (LocalDate.now().getDayOfMonth() >= 30) {
            testDate = LocalDate.now().withDayOfMonth(1);
        } else {
            testDate = LocalDate.now().withDayOfMonth(1).minusMonths(1);
        }
        SakDto sakDto2 = lagSak(testDate, BostotteStatus.VEDTATT, BostotteRolle.HOVEDPERSON, "V02", "Avslag - For høy inntekt", AVSLAG);
        BostotteDto bostotteDto = new BostotteDto(List.of(sakDto2), emptyList());

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonBostotteSak> saker = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBostotte().getSaker();
        assertThat(saker).hasSize(1);
        JsonBostotteSak sak1 = saker.get(0);
        assertThat(sak1.getStatus()).isEqualToIgnoringCase(sakDto2.getStatus().toString());
    }

    @Test
    void updateSystemdata_utbetalinger_henterIkkeBostotteUtenSamtykke() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.getJsonInternalSoknad(), false);

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad()
                .getData().getOkonomi().getOpplysninger().getUtbetaling().stream()
                .filter(utbetaling -> utbetaling.getKilde().equals(JsonKilde.SYSTEM))
                .collect(Collectors.toList());
        assertThat(utbetalinger).isEmpty();
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()).isFalse();
    }

    @Test
    void updateSystemdata_utbetalinger_fjernerGammelBostotteNarViIkkeHarSamtykke() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid1 = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        settBostotteSamtykkePaSoknad(soknadUnderArbeid1.getJsonInternalSoknad(), true);
        UtbetalingDto utbetalingDto = new UtbetalingDto(LocalDate.now().minusDays(32), BigDecimal.valueOf(10000), BostotteMottaker.KOMMUNE, BostotteRolle.HOVEDPERSON);
        BostotteDto bostotteDto = new BostotteDto(emptyList(), List.of(utbetalingDto));

        // Mock:
        when(husbankenClient.hentBostotte(any(), any(), any())).thenReturn(Optional.of(bostotteDto));

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid1, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger1 = soknadUnderArbeid1.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalinger1).hasSize(1);

        // Kjøring:
        SoknadUnderArbeid soknadUnderArbeid2 = soknadUnderArbeid1;
        settBostotteSamtykkePaSoknad(soknadUnderArbeid2.getJsonInternalSoknad(), false);
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid2, "");

        List<JsonOkonomiOpplysningUtbetaling> utbetalinger2 = soknadUnderArbeid1.getJsonInternalSoknad().getSoknad()
                .getData().getOkonomi().getOpplysninger().getUtbetaling().stream()
                .filter(utbetaling -> utbetaling.getKilde().equals(JsonKilde.SYSTEM))
                .collect(Collectors.toList());
        assertThat(utbetalinger2).isEmpty();
    }

    private SakDto lagSak(LocalDate saksDato, BostotteStatus status, BostotteRolle rolle, String kode, String beskrivelse, JsonBostotteSak.Vedtaksstatus vedtaksstatus) {
        VedtakDto vedtakDto = null;
        if(kode != null) {
            vedtakDto = new VedtakDto(kode, beskrivelse, vedtaksstatus.toString());
        }
        return new SakDto(saksDato.getMonthValue(), saksDato.getYear(), status, vedtakDto, rolle);
    }

    private void settBostotteSamtykkePaSoknad(JsonInternalSoknad jsonInternalSoknad, boolean harSamtykke) {
        List<JsonOkonomibekreftelse> bekreftelser = jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse();
        bekreftelser.removeIf(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(BOSTOTTE_SAMTYKKE));
        bekreftelser
                .add(new JsonOkonomibekreftelse().withKilde(JsonKilde.SYSTEM)
                        .withType(BOSTOTTE_SAMTYKKE)
                        .withVerdi(harSamtykke)
                        .withTittel("beskrivelse"));

    }

    private void assertThatUtbetalingErKorrekt(BostotteMottaker mottaker, BigDecimal netto, JsonOkonomiOpplysningUtbetaling utbetaling, LocalDate utbetalingsDato) {
        assertThat(utbetaling.getTittel()).isEqualToIgnoringCase("Statlig bostøtte");
        assertThat(utbetaling.getMottaker()).isEqualTo(JsonOkonomiOpplysningUtbetaling.Mottaker.fromValue(mottaker.getValue()));
        assertThat(utbetaling.getType()).isEqualTo(UTBETALING_HUSBANKEN);
        assertThat(utbetaling.getUtbetalingsdato()).isEqualTo(utbetalingsDato.toString());
        assertThat(utbetaling.getNetto()).isEqualTo(netto.doubleValue());
        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.SYSTEM);
    }
}
