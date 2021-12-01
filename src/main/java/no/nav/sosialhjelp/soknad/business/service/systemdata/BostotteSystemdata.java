package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Bostotte;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Sak;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Utbetaling;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService.nowWithForcedNanoseconds;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addUtbetalingIfNotPresentInOpplysninger;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.removeUtbetalingIfPresentInOpplysninger;
import static no.nav.sosialhjelp.soknad.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;

@Component
public class BostotteSystemdata {

    private final HusbankenClient husbankenClient;
    private final TextService textService;

    public BostotteSystemdata(
            HusbankenClient husbankenClient,
            TextService textService
    ) {
        this.husbankenClient = husbankenClient;
        this.textService = textService;
    }

    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        JsonSoknad soknad = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        JsonOkonomi okonomi = soknad.getData().getOkonomi();
        if(okonomi.getOpplysninger().getBekreftelse().stream().anyMatch(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(BOSTOTTE_SAMTYKKE) && bekreftelse.getVerdi())) {
            Bostotte bostotte = innhentBostotteFraHusbanken(token);
            if (bostotte != null) {
                okonomi.getOpplysninger().getBekreftelse().stream()
                        .filter(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(BOSTOTTE_SAMTYKKE))
                        .findAny()
                        .ifPresent(bekreftelse -> bekreftelse.withBekreftelsesDato(nowWithForcedNanoseconds()));
                fjernGamleHusbankenData(okonomi, false);
                boolean trengerViDataFraDeSiste60Dager = !harViDataFraSiste30Dager(bostotte);
                List<JsonOkonomiOpplysningUtbetaling> jsonBostotteUtbetalinger = mapToJsonOkonomiOpplysningUtbetalinger(bostotte, trengerViDataFraDeSiste60Dager);
                okonomi.getOpplysninger().getUtbetaling().addAll(jsonBostotteUtbetalinger);
                List<JsonBostotteSak> jsonSaksStatuser = mapToBostotteSaker(bostotte, trengerViDataFraDeSiste60Dager);
                okonomi.getOpplysninger().getBostotte().getSaker().addAll(jsonSaksStatuser);
                soknad.getDriftsinformasjon().setStotteFraHusbankenFeilet(false);
            } else {
                soknad.getDriftsinformasjon().setStotteFraHusbankenFeilet(true);
            }
        } else { // Ikke samtykke!!!
            fjernGamleHusbankenData(okonomi, true);
            soknad.getDriftsinformasjon().setStotteFraHusbankenFeilet(false);
        }
    }

    private void fjernGamleHusbankenData(JsonOkonomi okonomi, boolean skalFortsattHaBrukerUtbetaling) {
        JsonOkonomiopplysninger okonomiopplysninger = okonomi.getOpplysninger();
        okonomiopplysninger.setBostotte(new JsonBostotte());

        okonomiopplysninger.getUtbetaling().removeIf(utbetaling ->
                utbetaling.getType().equalsIgnoreCase(UTBETALING_HUSBANKEN) &&
                        utbetaling.getKilde().equals(JsonKilde.SYSTEM)
        );
        if(skalFortsattHaBrukerUtbetaling) {
            String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(BOSTOTTE));
            addUtbetalingIfNotPresentInOpplysninger(okonomiopplysninger.getUtbetaling(), UTBETALING_HUSBANKEN, tittel);
        } else {
            removeUtbetalingIfPresentInOpplysninger(okonomiopplysninger.getUtbetaling(), UTBETALING_HUSBANKEN);
        }
    }

    private boolean harViDataFraSiste30Dager(Bostotte bostotte) {
        boolean harNyeSaker = bostotte.getSaker().stream()
                .anyMatch(sak -> sak.getDato().isAfter(LocalDate.now().minusDays(30)));
        boolean harNyeUtbetalinger = bostotte.getUtbetalinger().stream()
                .anyMatch(utbetaling -> utbetaling.getUtbetalingsdato().isAfter(LocalDate.now().minusDays(30)));
        return harNyeSaker || harNyeUtbetalinger;
    }

    private Bostotte innhentBostotteFraHusbanken(String token) {
        var optionalDto = husbankenClient.hentBostotte(token, LocalDate.now().minusDays(60), LocalDate.now());
        return optionalDto.map(BostotteDto::toDomain).orElse(null);
    }

    private List<JsonOkonomiOpplysningUtbetaling> mapToJsonOkonomiOpplysningUtbetalinger(Bostotte bostotte, boolean trengerViDataFraDeSiste60Dager) {
        int filterDays = trengerViDataFraDeSiste60Dager ? 60 : 30;
        return bostotte.getUtbetalinger().stream()
                .filter(utbetaling -> utbetaling.getUtbetalingsdato().isAfter(LocalDate.now().minusDays(filterDays)))
                .map(this::mapToJsonOkonomiOpplysningUtbetaling)
                .collect(Collectors.toList());
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonOkonomiOpplysningUtbetaling(Utbetaling utbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(UTBETALING_HUSBANKEN)
                .withTittel("Statlig bostøtte")
                .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.fromValue(gjorForsteBokstavStor(utbetaling.getMottaker().toString())))
                .withNetto(utbetaling.getBelop().doubleValue())
                .withUtbetalingsdato(utbetaling.getUtbetalingsdato() != null ? utbetaling.getUtbetalingsdato().toString() : null)
                .withOverstyrtAvBruker(false);
    }

    private String gjorForsteBokstavStor(String navn) {
        return WordUtils.capitalizeFully(navn);
    }

    private List<JsonBostotteSak> mapToBostotteSaker(Bostotte bostotte, boolean trengerViDataFraDeSiste60Dager) {
        int filterDays = trengerViDataFraDeSiste60Dager ? 60 : 30;
        return bostotte.getSaker().stream()
                .filter(sak -> sak.getDato().isAfter(LocalDate.now().minusDays(filterDays)))
                .map(this::mapToBostotteSak)
                .collect(Collectors.toList());
    }

    private JsonBostotteSak mapToBostotteSak(Sak sak) {
        JsonBostotteSak bostotteSak = new JsonBostotteSak()
                .withKilde(JsonKildeSystem.SYSTEM)
                .withType(UTBETALING_HUSBANKEN)
                .withStatus(sak.getStatus().toString())
                .withDato(sak.getDato().toString());
        if (sak.getVedtak() != null) {
            bostotteSak.withBeskrivelse(sak.getVedtak().getBeskrivelse());
            if (sak.getVedtak().getType() != null) {
                bostotteSak.withVedtaksstatus(JsonBostotteSak.Vedtaksstatus.fromValue(sak.getVedtak().getType()));
            }
        }
        return bostotteSak;
    }
}
