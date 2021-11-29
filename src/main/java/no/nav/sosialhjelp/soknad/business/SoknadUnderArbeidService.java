package no.nav.sosialhjelp.soknad.business;

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;


@Component
public class SoknadUnderArbeidService {

    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public SoknadUnderArbeidService(SoknadUnderArbeidRepository soknadUnderArbeidRepository) {
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
    }

    public void settInnsendingstidspunktPaSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Søknad under arbeid mangler");
        }
        if (soknadUnderArbeid.erEttersendelse()){
            return;
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().setInnsendingstidspunkt(nowWithForcedNanoseconds());
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, soknadUnderArbeid.getEier());
    }

    public static String nowWithForcedNanoseconds() {
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (now.getNano() == 0) {
            return now.plusNanos(1_000_000).toString();
        }
        return now.toString();
    }

    public void sortArbeid(JsonArbeid arbeid) {
        if (arbeid.getForhold() != null) {
            arbeid.getForhold().sort(Comparator.comparing(JsonArbeidsforhold::getArbeidsgivernavn));
        }
    }

    public void sortOkonomi(JsonOkonomi okonomi) {
        okonomi.getOpplysninger().getBekreftelse().sort(Comparator.comparing(JsonOkonomibekreftelse::getType));
        okonomi.getOpplysninger().getUtbetaling().sort(Comparator.comparing(JsonOkonomiOpplysningUtbetaling::getType));
        okonomi.getOpplysninger().getUtgift().sort(Comparator.comparing(JsonOkonomiOpplysningUtgift::getType));
        okonomi.getOversikt().getInntekt().sort(Comparator.comparing(JsonOkonomioversiktInntekt::getType));
        okonomi.getOversikt().getUtgift().sort(Comparator.comparing(JsonOkonomioversiktUtgift::getType));
        okonomi.getOversikt().getFormue().sort(Comparator.comparing(JsonOkonomioversiktFormue::getType));
    }

}
