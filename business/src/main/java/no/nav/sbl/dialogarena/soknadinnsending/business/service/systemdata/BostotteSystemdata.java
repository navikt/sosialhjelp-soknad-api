package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.Bostotte;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteRolle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.SakerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;

@Component
public class BostotteSystemdata implements Systemdata {
    @Inject
    private Bostotte bostotte;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        JsonSoknad soknad = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        JsonOkonomi okonomi = soknad.getData().getOkonomi();
        String personIdentifikator = soknad.getData().getPersonalia().getPersonIdentifikator().getVerdi();
        BostotteDto bostotteDto = innhentBostotteFraHusbanken(personIdentifikator, token);
        if (bostotteDto != null) {
            boolean trengerViDataFraDeSiste60Dager = !harViDataFraSiste30Dager(bostotteDto);
            List<JsonOkonomiOpplysningUtbetaling> jsonBostotteUtbetalinger = mapToJsonOkonomiOpplysningUtbetalinger(bostotteDto, trengerViDataFraDeSiste60Dager);
            okonomi.getOpplysninger().getUtbetaling().addAll(jsonBostotteUtbetalinger);
            List<JsonBostotteSak> jsonSaksStatuser = mapToJsonOkonomiOpplysningSaker(bostotteDto, trengerViDataFraDeSiste60Dager);
            if(okonomi.getOpplysninger().getBostotte() == null) {
                okonomi.getOpplysninger().setBostotte(new JsonBostotte());
            }
            okonomi.getOpplysninger().getBostotte().getSaker().addAll(jsonSaksStatuser);
            soknad.getDriftsinformasjon().setStotteFraHusbankenFeilet(false);
        } else {
            soknad.getDriftsinformasjon().setStotteFraHusbankenFeilet(true);
        }
    }

    private boolean harViDataFraSiste30Dager(BostotteDto bostotteDto) {
        boolean harNyeSaker = bostotteDto.getSaker().stream()
                .anyMatch(sakerDto -> sakerDto.getDato().isAfter(LocalDate.now().minusDays(30)));
        boolean harNyeUtbetalinger = bostotteDto.getUtbetalinger().stream()
                .anyMatch(utbetalingerDto -> utbetalingerDto.getUtbetalingsdato().isAfter(LocalDate.now().minusDays(30)));
        return harNyeSaker || harNyeUtbetalinger;
    }

    private BostotteDto innhentBostotteFraHusbanken(String personIdentifikator, String token) {
        BostotteDto bostotteDto = bostotte.hentBostotte(personIdentifikator, token, LocalDate.now().minusDays(60), LocalDate.now());
        if (bostotteDto != null) {
            bostotteDto.saker = bostotteDto.getSaker().stream()
                    .filter(sakerDto -> sakerDto.getRolle().equals(BostotteRolle.HOVEDPERSON))
                    .collect(Collectors.toList());
            bostotteDto.utbetalinger = bostotteDto.getUtbetalinger().stream()
                    .filter(utbetalingerDto -> utbetalingerDto.getRolle().equals(BostotteRolle.HOVEDPERSON))
                    .collect(Collectors.toList());
        }
        return bostotteDto;
    }

    private List<JsonOkonomiOpplysningUtbetaling> mapToJsonOkonomiOpplysningUtbetalinger(BostotteDto bostotteDto, boolean trengerViDataFraDeSiste60Dager) {
        int filterDays = trengerViDataFraDeSiste60Dager ? 60 : 30;
        return bostotteDto.getUtbetalinger().stream()
                .filter(utbetalingerDto -> utbetalingerDto.getUtbetalingsdato().isAfter(LocalDate.now().minusDays(filterDays)))
                .map(this::mapToJsonOkonomiOpplysningUtbetaling)
                .collect(Collectors.toList());
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonOkonomiOpplysningUtbetaling(UtbetalingerDto utbetalingerDto) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(UTBETALING_HUSBANKEN)
                .withTittel("Statlig bostøtte")
                .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.fromValue(gjorForsteBokstavStor(utbetalingerDto.getMottaker().toString())))
                .withNetto(utbetalingerDto.getBelop().doubleValue())
                .withUtbetalingsdato(utbetalingerDto.getUtbetalingsdato() != null ? utbetalingerDto.getUtbetalingsdato().toString() : null)
                .withOverstyrtAvBruker(false);
    }

    private String gjorForsteBokstavStor(String navn) {
        return WordUtils.capitalizeFully(navn);
    }

    private List<JsonBostotteSak> mapToJsonOkonomiOpplysningSaker(BostotteDto bostotteDto, boolean trengerViDataFraDeSiste60Dager) {
        int filterDays = trengerViDataFraDeSiste60Dager ? 60 : 30;
        return bostotteDto.getSaker().stream()
                .filter(sakerDto -> sakerDto.getDato().isAfter(LocalDate.now().minusDays(filterDays)))
                .map(this::mapToJsonOkonomiOpplysningSak)
                .collect(Collectors.toList());
    }

    private JsonBostotteSak mapToJsonOkonomiOpplysningSak(SakerDto sakerDto) {
        JsonBostotteSak bostotteSak = new JsonBostotteSak()
                .withKilde(JsonKildeSystem.SYSTEM)
                .withType(UTBETALING_HUSBANKEN)
                .withStatus(sakerDto.getStatus().toString())
                .withDato(sakerDto.getDato().toString());
        if(sakerDto.getVedtak() != null) {
            bostotteSak.withBeskrivelse(sakerDto.getVedtak().getBeskrivelse());
            if(sakerDto.getVedtak().getType() != null) {
                bostotteSak.withVedtaksstatus(JsonBostotteSak.Vedtaksstatus.fromValue(sakerDto.getVedtak().getType()));
            }
        }
        return bostotteSak;
    }
}
