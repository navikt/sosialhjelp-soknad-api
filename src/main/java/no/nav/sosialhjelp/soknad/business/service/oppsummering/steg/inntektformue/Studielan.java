package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;

import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.booleanVerdiFelt;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelse;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue;

public class Studielan {

    public Avsnitt getAvsnitt(JsonOkonomiopplysninger opplysninger) {
        return new Avsnitt.Builder()
                .withTittel("inntekt.studielan.titel")
                .withSporsmal(studielanSporsmal(opplysninger))
                .build();
    }

    private List<Sporsmal> studielanSporsmal(JsonOkonomiopplysninger opplysninger) {
        var harUtfyltStudielanSporsmal = harBekreftelse(opplysninger, STUDIELAN);
        var harSvartJaStudielan = harUtfyltStudielanSporsmal && harBekreftelseTrue(opplysninger, STUDIELAN);

        return singletonList(
                new Sporsmal.Builder()
                        .withTittel("inntekt.studielan.sporsmal")
                        .withErUtfylt(harUtfyltStudielanSporsmal)
                        .withFelt(harUtfyltStudielanSporsmal ? booleanVerdiFelt(harSvartJaStudielan, "inntekt.studielan.true", "inntekt.studielan.false") : null)
                        .build()
        );
    }
}
