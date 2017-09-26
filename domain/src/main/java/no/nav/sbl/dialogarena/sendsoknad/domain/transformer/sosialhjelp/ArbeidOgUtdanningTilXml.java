package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLArbeidUtdanning;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKildeBoolean;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.*;

public class ArbeidOgUtdanningTilXml implements Function<WebSoknad, XMLArbeidUtdanning> {

    @Override
    public XMLArbeidUtdanning apply(WebSoknad webSoknad) {
        XMLArbeidUtdanning arbeidUtdanning = new XMLArbeidUtdanning();

        XMLKildeBoolean arbeidsledig = tilBoolean(webSoknad, "arbeid.dinsituasjon.arbeidsledig");

        XMLKildeBoolean jobb = tilBoolean(webSoknad, "arbeid.dinsituasjon.jobb");

        Boolean erStudent = Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.student"));
        XMLKildeBoolean student = tilBoolean(erStudent);

        if (erStudent) {
//            arbeidUtdanning.withStudererGrad(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "arbeid.dinsituasjon.student.heltid"));
        }

        Boolean annenSituasjon = Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.annensituasjon"));

        XMLKildeBoolean annet = tilBoolean(annenSituasjon);

//        arbeidUtdanning
//                .withAnnet(annet)
//                .withJobb(jobb)
//                .withArbeidsledig(arbeidsledig)
//                .withStudent(student);

        if (annenSituasjon) {
//            arbeidUtdanning.withAnnetBeskrivelse(tilXMLKildeString(webSoknad, "arbeid.dinsituasjon.annensituasjon.beskrivelse"));
        }

        return arbeidUtdanning;
    }
}