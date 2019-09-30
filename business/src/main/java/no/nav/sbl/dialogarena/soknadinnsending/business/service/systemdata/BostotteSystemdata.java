package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.Bostotte;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.SakerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningSak;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
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
            boolean trengerBeggeManedene = !harViDataFraSisteManed(bostotteDto);
            List<JsonOkonomiOpplysningUtbetaling> jsonOkonomiOpplysningUtbetaling = mapToJsonOkonomiOpplysningUtbetalinger(bostotteDto, trengerBeggeManedene);
            okonomi.getOpplysninger().getUtbetaling().addAll(jsonOkonomiOpplysningUtbetaling);
            List<JsonOkonomiOpplysningSak> jsonSaksStatuser = mapToJsonOkonomiOpplysningSaker(bostotteDto, trengerBeggeManedene);
            okonomi.getOpplysninger().getSak().addAll(jsonSaksStatuser);
            soknad.getDriftsinformasjon().setStotteFraHusbankenFeilet(false);
        } else {
            soknad.getDriftsinformasjon().setStotteFraHusbankenFeilet(true);
        }
    }

    private boolean harViDataFraSisteManed(BostotteDto bostotteDto) {
        boolean harNyeSaker = bostotteDto.getSaker().stream()
                .anyMatch(sakerDto -> sakerDto.getDato().isAfter(LocalDate.now().minusMonths(1)));
        boolean harNyeUtbetalinger = bostotteDto.getUtbetalinger().stream()
                .anyMatch(utbetalingerDto -> utbetalingerDto.getUtbetalingsdato().isAfter(LocalDate.now().minusMonths(1)));
        return harNyeSaker || harNyeUtbetalinger;
    }

    private BostotteDto innhentBostotteFraHusbanken(String personIdentifikator, String token) {
        BostotteDto bostotteDto = bostotte.hentBostotte(personIdentifikator, token, LocalDate.now().minusMonths(2), LocalDate.now());
        if(bostotteDto != null) {
            bostotteDto.saker = bostotteDto.getSaker().stream()
                    .filter(sakerDto -> sakerDto.getRolle().equalsIgnoreCase("HOVEDPERSON"))
                    .collect(Collectors.toList());
        }
        return bostotteDto;
    }

    private List<JsonOkonomiOpplysningUtbetaling> mapToJsonOkonomiOpplysningUtbetalinger(BostotteDto bostotteDto, boolean trengerBeggeManedene) {
        int filterManeder = trengerBeggeManedene ? 2 : 1;
        return bostotteDto.getUtbetalinger().stream()
                .filter(utbetalingerDto -> utbetalingerDto.getUtbetalingsdato().isAfter(LocalDate.now().minusMonths(filterManeder)))
                .map(this::mapToJsonOkonomiOpplysningUtbetaling)
                .collect(Collectors.toList());
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonOkonomiOpplysningUtbetaling(UtbetalingerDto utbetalingerDto) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(HUSBANKEN_TYPE)
                .withTittel(utbetalingerDto.getMottaker())
                .withBelop(utbetalingerDto.getBelop().intValue())
                .withUtbetalingsdato(utbetalingerDto.getUtbetalingsdato() != null ? utbetalingerDto.getUtbetalingsdato().toString() : null)
                .withOverstyrtAvBruker(false);
    }

    private List<JsonOkonomiOpplysningSak> mapToJsonOkonomiOpplysningSaker(BostotteDto bostotteDto, boolean trengerBeggeManedene) {
        int filterManeder = trengerBeggeManedene ? 2 : 1;
        return bostotteDto.getSaker().stream()
                .filter(sakerDto -> sakerDto.getDato().isAfter(LocalDate.now().minusMonths(filterManeder)))
                .map(this::mapToJsonOkonomiOpplysningSak)
                .collect(Collectors.toList());
    }
    private JsonOkonomiOpplysningSak mapToJsonOkonomiOpplysningSak(SakerDto sakerDto) {
        return new JsonOkonomiOpplysningSak()
                .withKilde(JsonKilde.SYSTEM)
                .withType(HUSBANKEN_TYPE)
                .withStatus(sakerDto.getStatus())
                .withBeskrivelse(sakerDto.getVedtak() != null ? sakerDto.getVedtak().getBeskrivelse() : null)
                .withDato(sakerDto.getDato().toString());
    }
}
