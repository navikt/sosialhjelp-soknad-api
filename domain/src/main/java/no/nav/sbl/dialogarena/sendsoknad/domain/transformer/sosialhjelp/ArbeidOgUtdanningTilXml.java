package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLArbeidUtdanning;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBoolean;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilXMLBoolean;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilXMLString;

public class ArbeidOgUtdanningTilXml implements Function<WebSoknad, XMLArbeidUtdanning> {

    @Override
    public XMLArbeidUtdanning apply(WebSoknad webSoknad) {
        XMLArbeidUtdanning arbeidUtdanning = new XMLArbeidUtdanning();

        XMLBoolean arbeidsledig = tilXMLBoolean(webSoknad, "arbeid.dinsituasjon.arbeidsledig");

        XMLBoolean jobb = tilXMLBoolean(webSoknad, "arbeid.dinsituasjon.jobb");

        Boolean erStudent = Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.student"));
        XMLBoolean student = tilXMLBoolean(erStudent);

        if (erStudent) {
            arbeidUtdanning.withStudererGrad(tilXMLBoolean(webSoknad, "arbeid.dinsituasjon.student.heltid"));
        }

        Boolean annenSituasjon = Boolean.valueOf(webSoknad.getValueForFaktum("arbeid.dinsituasjon.annensituasjon"));

        XMLBoolean annet = tilXMLBoolean(annenSituasjon);

        arbeidUtdanning
                .withAnnet(annet)
                .withJobb(jobb)
                .withArbeidsledig(arbeidsledig)
                .withStudent(student);

        if (annenSituasjon) {
            arbeidUtdanning.withAnnetBeskrivelse(tilXMLString(webSoknad, "arbeid.dinsituasjon.annensituasjon.beskrivelse"));
        }

        return arbeidUtdanning;
    }
}