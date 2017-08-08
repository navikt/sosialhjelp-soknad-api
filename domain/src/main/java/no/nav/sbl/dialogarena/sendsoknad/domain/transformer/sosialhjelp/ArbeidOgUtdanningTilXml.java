package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp.ArbeidUtdanning;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp.ArbeidUtdanning.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;

public class ArbeidOgUtdanningTilXml implements Function<WebSoknad, ArbeidUtdanning> {

    @Override
    public ArbeidUtdanning apply(WebSoknad webSoknad) {
        ArbeidUtdanning arbeidUtdanning = new ArbeidUtdanning();

        Arbeidsledig arbeidsledig = new Arbeidsledig()
                .withKilde(BRUKER)
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.arbeidsledig")));

        Jobb jobb = new Jobb()
                .withKilde(BRUKER)
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.jobb")));

        Boolean erStudent = Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.student"));
        Student student = new Student()
                .withKilde(BRUKER)
                .withValue(erStudent);

        if (erStudent) {
            StudererGrad studererGrad = new StudererGrad()
                    .withKilde(BRUKER)
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.student.heltid")));
            arbeidUtdanning.withStudererGrad(studererGrad);
        }

        Annet annet = new Annet()
                .withKilde(BRUKER)
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.annensituasjon")));

        return arbeidUtdanning
                .withAnnet(annet)
                .withJobb(jobb)
                .withArbeidsledig(arbeidsledig)
                .withStudent(student);
    }
}