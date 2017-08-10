package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLArbeidUtdanning;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBoolean;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLString;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;

public class ArbeidOgUtdanningTilXml implements Function<WebSoknad, XMLArbeidUtdanning> {

    @Override
    public XMLArbeidUtdanning apply(WebSoknad webSoknad) {
        XMLArbeidUtdanning arbeidUtdanning = new XMLArbeidUtdanning();

        XMLBoolean arbeidsledig = new XMLBoolean()
                .withKilde(BRUKER)
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.arbeidsledig")));

        XMLBoolean jobb = new XMLBoolean()
                .withKilde(BRUKER)
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.jobb")));

        Boolean erStudent = Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.student"));
        XMLBoolean student = new XMLBoolean()
                .withKilde(BRUKER)
                .withValue(erStudent);

        if (erStudent) {
            XMLBoolean studererGrad = new XMLBoolean()
                    .withKilde(BRUKER)
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.student.heltid")));
            arbeidUtdanning.withStudererGrad(studererGrad);
        }

        Boolean annenSituasjon = Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.annensituasjon"));

        XMLBoolean annet = new XMLBoolean()
                .withKilde(BRUKER)
                .withValue(annenSituasjon);

        arbeidUtdanning
                .withAnnet(annet)
                .withJobb(jobb)
                .withArbeidsledig(arbeidsledig)
                .withStudent(student);

        if(annenSituasjon) {
            XMLString beskrivelse = new XMLString()
                    .withKilde(BRUKER)
                    .withValue(webSoknad.getValueForFaktum("arbeid.dinsituasjon.annensituasjon.beskrivelse"));
            arbeidUtdanning.withAnnetBeskrivelse(beskrivelse);
        }

        return arbeidUtdanning;
    }
}