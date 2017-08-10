package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLArbeidUtdanning;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLArbeidUtdanning.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;

public class ArbeidOgUtdanningTilXml implements Function<WebSoknad, XMLArbeidUtdanning> {

    @Override
    public XMLArbeidUtdanning apply(WebSoknad webSoknad) {
        XMLArbeidUtdanning arbeidUtdanning = new XMLArbeidUtdanning();

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

        Boolean annenSituasjon = Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.annensituasjon"));
        Annet annet = new Annet()
                .withKilde(BRUKER)
                .withValue(annenSituasjon);

        arbeidUtdanning
                .withAnnet(annet)
                .withJobb(jobb)
                .withArbeidsledig(arbeidsledig)
                .withStudent(student);

        if(annenSituasjon) {
            AnnetBeskrivelse beskrivelse = new AnnetBeskrivelse()
                    .withKilde(BRUKER)
                    .withValue(webSoknad.getValueForFaktum("arbeid.dinsituasjon.annensituasjon.beskrivelse"));
            arbeidUtdanning.withAnnetBeskrivelse(beskrivelse);
        }

        return arbeidUtdanning;
    }
}