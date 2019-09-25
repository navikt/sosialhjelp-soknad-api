package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.BostotteImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
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
    }

    private void assertThatUtbetalingErKorrekt(String mottaker, BigDecimal belop, JsonOkonomiOpplysningUtbetaling utbetaling, LocalDate utbetalingsDato) {
        assertThat(utbetaling.getTittel()).endsWith(mottaker);
        assertThat(utbetaling.getType()).isEqualTo("husbanken");
        assertThat(utbetaling.getUtbetalingsdato()).isEqualTo(utbetalingsDato.toString());
        assertThat(utbetaling.getBelop()).isEqualTo(belop.intValue());
        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(utbetaling.getOverstyrtAvBruker()).isFalse();
    }
}
