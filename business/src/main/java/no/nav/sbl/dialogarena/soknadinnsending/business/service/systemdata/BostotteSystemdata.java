package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.bostotte.Bostotte;
import no.nav.sbl.dialogarena.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.bostotte.dto.SakerDto;
import no.nav.sbl.dialogarena.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse;
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus;
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

import static no.nav.sbl.dialogarena.bostotte.Bostotte.HUSBANKEN_TYPE;

@Component
public class BostotteSystemdata implements Systemdata {
    @Inject
    private Bostotte bostotte;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        JsonSoknad soknad = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        JsonOkonomi okonomi = soknad.getData().getOkonomi();
        String personIdentifikator = soknad.getData().getPersonalia().getPersonIdentifikator().getVerdi();
        BostotteDto bostotteDto = innhentBostotteFraHusbanken(personIdentifikator);
        List<JsonOkonomiOpplysningUtbetaling> jsonOkonomiOpplysningUtbetaling = mapToJsonOkonomiOpplysningUtbetalinger(bostotteDto);
        okonomi.getOpplysninger().getUtbetaling().addAll(jsonOkonomiOpplysningUtbetaling);
        List<JsonSaksStatus> jsonSaksStatuser = mapToJsonSaksStatuser(bostotteDto);
        // TODO: pcn: dytt jsonSaksStatuser inn i soknad +/- bytt bort fra jsonSaksStatuser
    }

    private BostotteDto innhentBostotteFraHusbanken(String personIdentifikator) {
        return bostotte.hentBostotte(personIdentifikator, LocalDate.now().minusMonths(3), LocalDate.now());
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

    private List<JsonSaksStatus> mapToJsonSaksStatuser(BostotteDto bostotteDto) {
        return bostotteDto.getSaker().stream()
                .map(this::mapToJsonSaksStatus)
                .collect(Collectors.toList());
    }
    private JsonSaksStatus mapToJsonSaksStatus(SakerDto sakerDto) {
        return new JsonSaksStatus()
                .withStatus(JsonSaksStatus.Status.fromValue(sakerDto.getStatus()))
//                .withAdditionalProperty(sakerDto.getRolle(), sakerDto.getRolle())
                .withHendelsestidspunkt(sakerDto.getDato().toString())
                .withReferanse(sakerDto.getVedtak().getBeskrivelse())
                .withTittel("Bostøtte")
                .withType(JsonHendelse.Type.fromValue(sakerDto.getVedtak().getKode()))
                ;
    }
}
