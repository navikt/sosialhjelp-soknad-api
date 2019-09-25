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
            List<JsonOkonomiOpplysningUtbetaling> jsonOkonomiOpplysningUtbetaling = mapToJsonOkonomiOpplysningUtbetalinger(bostotteDto);
            okonomi.getOpplysninger().getUtbetaling().addAll(jsonOkonomiOpplysningUtbetaling);
            List<JsonOkonomiOpplysningSak> jsonSaksStatuser = mapToJsonOkonomiOpplysningSaker(bostotteDto);
            okonomi.getOpplysninger().getSak().addAll(jsonSaksStatuser);
        }
    }

    private BostotteDto innhentBostotteFraHusbanken(String personIdentifikator, String token) {
        return bostotte.hentBostotte(personIdentifikator, token, LocalDate.now().minusMonths(3), LocalDate.now());
    }

    private List<JsonOkonomiOpplysningUtbetaling> mapToJsonOkonomiOpplysningUtbetalinger(BostotteDto bostotteDto) {
        return bostotteDto.getUtbetalinger().stream()
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

    private List<JsonOkonomiOpplysningSak> mapToJsonOkonomiOpplysningSaker(BostotteDto bostotteDto) {
        return bostotteDto.getSaker().stream()
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
