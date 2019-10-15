package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.Bostotte;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteRolle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.SakerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.Bostotte.HUSBANKEN_TYPE;

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
            List<JsonBostotteUtbetaling> jsonBostotteUtbetalinger = mapToJsonOkonomiOpplysningUtbetalinger(bostotteDto, trengerViDataFraDeSiste60Dager);
            okonomi.getOpplysninger().getBostotte().getUtbetalinger().addAll(jsonBostotteUtbetalinger);
            List<JsonBostotteSak> jsonSaksStatuser = mapToJsonOkonomiOpplysningSaker(bostotteDto, trengerViDataFraDeSiste60Dager);
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

    private List<JsonBostotteUtbetaling> mapToJsonOkonomiOpplysningUtbetalinger(BostotteDto bostotteDto, boolean trengerViDataFraDeSiste60Dager) {
        int filterDays = trengerViDataFraDeSiste60Dager ? 60 : 30;
        return bostotteDto.getUtbetalinger().stream()
                .filter(utbetalingerDto -> utbetalingerDto.getUtbetalingsdato().isAfter(LocalDate.now().minusDays(filterDays)))
                .map(this::mapToJsonOkonomiOpplysningUtbetaling)
                .collect(Collectors.toList());
    }

    private JsonBostotteUtbetaling mapToJsonOkonomiOpplysningUtbetaling(UtbetalingerDto utbetalingerDto) {
        return new JsonBostotteUtbetaling()
                .withKilde(JsonKildeSystem.SYSTEM)
                .withType(HUSBANKEN_TYPE)
                .withTittel("Statlig bostotte")
                .withMottaker(JsonBostotteUtbetaling.Mottaker.fromValue(gjorForsteBokstavStor(utbetalingerDto.getMottaker().toString())))
                .withBelop(utbetalingerDto.getBelop().doubleValue())
                .withUtbetalingsdato(utbetalingerDto.getUtbetalingsdato() != null ? utbetalingerDto.getUtbetalingsdato().toString() : null);
    }

    private String gjorForsteBokstavStor(String navn) {
        return navn.substring(0, 1).toUpperCase() + navn.substring(1).toLowerCase();
    }

    private List<JsonBostotteSak> mapToJsonOkonomiOpplysningSaker(BostotteDto bostotteDto, boolean trengerViDataFraDeSiste60Dager) {
        int filterDays = trengerViDataFraDeSiste60Dager ? 60 : 30;
        return bostotteDto.getSaker().stream()
                .filter(sakerDto -> sakerDto.getDato().isAfter(LocalDate.now().minusDays(filterDays)))
                .map(this::mapToJsonOkonomiOpplysningSak)
                .collect(Collectors.toList());
    }

    private JsonBostotteSak mapToJsonOkonomiOpplysningSak(SakerDto sakerDto) {
        return new JsonBostotteSak()
                .withKilde(JsonKildeSystem.SYSTEM)
                .withType(HUSBANKEN_TYPE)
                .withStatus(sakerDto.getStatus().toString())
                .withBeskrivelse(sakerDto.getVedtak() != null ? sakerDto.getVedtak().getBeskrivelse() : null)
                .withDato(sakerDto.getDato().toString());
    }
}
