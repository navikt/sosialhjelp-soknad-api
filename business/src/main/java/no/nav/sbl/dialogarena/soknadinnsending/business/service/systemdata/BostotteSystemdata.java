package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.bostotte.Bostotte;
import no.nav.sbl.dialogarena.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BostotteSystemdata implements Systemdata {
    @Inject
    private Bostotte bostotte;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        JsonSoknad soknad = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        JsonOkonomi okonomi = soknad.getData().getOkonomi();
        String personIdentifikator = soknad.getData().getPersonalia().getPersonIdentifikator().getVerdi();
        List<JsonOkonomiOpplysningUtbetaling> jsonOkonomiOpplysningUtbetaling = innhentBostotteFraHusbanken(personIdentifikator);
        okonomi.getOpplysninger().getUtbetaling().addAll(jsonOkonomiOpplysningUtbetaling);
    }

    private List<JsonOkonomiOpplysningUtbetaling> innhentBostotteFraHusbanken(String personIdentifikator) {
        BostotteDto bostotteDto = bostotte.hentBostotte(personIdentifikator, LocalDate.now().minusMonths(3), LocalDate.now());
        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = bostotteDto.getUtbetalinger().stream()
                .map(this::mapToJsonOkonomiOpplysningUtbetaling)
                .collect(Collectors.toList());
        return jsonUtbetalinger;
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonOkonomiOpplysningUtbetaling(UtbetalingerDto utbetalingerDto) {
        String type = "husbanken";
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(type)
                .withTittel(utbetalingerDto.getMottaker())
                .withBelop(utbetalingerDto.getBelop().intValue())
//                .withNetto(utbetalingerDto.netto)
//                .withBrutto(utbetalingerDto.brutto)
//                .withSkattetrekk(utbetalingerDto.skattetrekk)
//                .withOrganisasjon(mapToJsonOrganisasjon(bostotteDto.orgnummer))
//                .withAndreTrekk(utbetalingerDto.andreTrekk)
//                .withPeriodeFom(utbetalingerDto.periodeFom != null ? utbetalingerDto.periodeFom.toString() : null)
//                .withPeriodeTom(utbetalingerDto.periodeTom != null ? utbetalingerDto.periodeTom.toString() : null)
                .withUtbetalingsdato(utbetalingerDto.getUtbetalingsdato() == null ? null : utbetalingerDto.getUtbetalingsdato().toString())
//                .withKomponenter(tilUtbetalingskomponentListe(bostotteDto.komponenter))
                .withOverstyrtAvBruker(false);
    }
}
