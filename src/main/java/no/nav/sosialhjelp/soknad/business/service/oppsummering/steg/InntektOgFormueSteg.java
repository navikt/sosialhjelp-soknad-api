package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.AndreInntekter;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.AnnenFormue;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.Bank;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.BostotteHusbanken;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.NavUtbetalinger;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.SkattbarInntekt;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.Studielan;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;

import java.util.ArrayList;

public class InntektOgFormueSteg {

    private final SkattbarInntekt skattbarInntektAvsnitt;
    private final NavUtbetalinger navUtbetalinger;
    private final BostotteHusbanken bostotteHusbanken;
    private final Studielan studielan;
    private final AndreInntekter andreInntekter;
    private final Bank bank;
    private final AnnenFormue annenFormue;

    public InntektOgFormueSteg() {
        skattbarInntektAvsnitt = new SkattbarInntekt();
        navUtbetalinger = new NavUtbetalinger();
        bostotteHusbanken = new BostotteHusbanken();
        studielan = new Studielan();
        andreInntekter = new AndreInntekter();
        bank = new Bank();
        annenFormue = new AnnenFormue();
    }

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var okonomi = jsonInternalSoknad.getSoknad().getData().getOkonomi();
        var opplysninger = okonomi.getOpplysninger();
        var driftsinformasjon = jsonInternalSoknad.getSoknad().getDriftsinformasjon();

        var avsnitt = new ArrayList<Avsnitt>();
        avsnitt.add(skattbarInntektAvsnitt.getAvsnitt(okonomi, driftsinformasjon));
        avsnitt.add(navUtbetalinger.getAvsnitt(opplysninger, driftsinformasjon));
        avsnitt.add(bostotteHusbanken.getAvsnitt(opplysninger, driftsinformasjon));
        if (erStudent(jsonInternalSoknad.getSoknad().getData().getUtdanning())) {
            avsnitt.add(studielan.getAvsnitt(opplysninger));
        }
        avsnitt.add(andreInntekter.getAvsnitt(opplysninger));
        avsnitt.add(bank.getAvsnitt(okonomi));
        avsnitt.add(annenFormue.getAvsnitt(okonomi));

        return new Steg.Builder()
                .withStegNr(6)
                .withTittel("inntektbolk.tittel")
                .withAvsnitt(avsnitt)
                .build();
    }

    private boolean erStudent(JsonUtdanning utdanning) {
        return utdanning != null && utdanning.getErStudent() != null && utdanning.getErStudent().equals(Boolean.TRUE);
    }
}
