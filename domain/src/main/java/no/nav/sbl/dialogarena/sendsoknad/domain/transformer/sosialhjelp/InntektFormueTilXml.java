package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBoolean;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLString;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;


public class InntektFormueTilXml implements Function<WebSoknad, XMLInntektFormue> {

    @Override
    public XMLInntektFormue apply(WebSoknad webSoknad) {

        XMLBoolean mottarYtelser = new XMLBoolean()
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.mottarytelser")))
                .withKilde(BRUKER);

        XMLBoolean soktYtelser = new XMLBoolean()
                .withKilde(BRUKER)
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.soktytelser")));

        String mottattBostotte = webSoknad.getValueForFaktum("inntekt.bostotte");
        XMLBoolean mottarBostotte = new XMLBoolean()
                .withKilde(BRUKER)
                .withValue(Boolean.valueOf(mottattBostotte));

        XMLInntektFormue xmlInntektFormue = new XMLInntektFormue();

        //forvirrende på grunn av booleanradio som forventer at nei verdien er det som skal gi oppfølgingspørsmål
        if (mottattBostotte.equals("false")) {
            xmlInntektFormue.withHusbanken(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.bostotte.type.husbanken")))
                    .withKilde(BRUKER));
            xmlInntektFormue.withKommunal(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.bostotte.type.kommunal")))
                    .withKilde(BRUKER));
        }

        String eierandelerVerdi = webSoknad.getValueForFaktum("inntekt.eierandeler");
        XMLBoolean eierandeler = new XMLBoolean().withKilde(BRUKER).withValue(Boolean.valueOf(eierandelerVerdi));

        if (eierandelerVerdi.equals("false")) {
            xmlInntektFormue.withBolig(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum(
                            "inntekt.eierandeler.type.bolig"
                    )))
                    .withKilde(BRUKER));

            xmlInntektFormue.withKjoretoy(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum(
                            "inntekt.eierandeler.type.kjoretoy"
                    )))
                    .withKilde(BRUKER));

            xmlInntektFormue.withCampingvogn(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum(
                            "inntekt.eierandeler.type.campingvogn"
                    )))
                    .withKilde(BRUKER));

            xmlInntektFormue.withFritidseiendom(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum(
                            "inntekt.eierandeler.type.fritidseiendom"
                    )))
                    .withKilde(BRUKER));

            Boolean annetVerdi = Boolean.valueOf(webSoknad.getValueForFaktum(
                    "inntekt.eierandeler.type.annet"
            ));
            xmlInntektFormue.withAnnet(new XMLBoolean()
                    .withValue(annetVerdi)
                    .withKilde(BRUKER));
            if(annetVerdi) {
                xmlInntektFormue.withAnnetBeskrivelse(new XMLString()
                        .withValue(webSoknad.getValueForFaktum("inntekt.eierandeler.type.annet.true.beskrivelse"))
                        .withKilde(BRUKER));
            }
        }

        String bankinnskuddVerdi = webSoknad.getValueForFaktum("inntekt.bankinnskudd");
        XMLBoolean bankinnskudd = new XMLBoolean().withKilde(BRUKER).withValue(Boolean.valueOf(bankinnskuddVerdi));

        if (bankinnskuddVerdi.equals("false")) {
            xmlInntektFormue.withSparekonto(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum(
                            "inntekt.bankinnskudd.false.type.sparekonto"
                    )))
                    .withKilde(BRUKER));

            xmlInntektFormue.withBrukskonto(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum(
                            "inntekt.bankinnskudd.false.type.brukskonto"
                    )))
                    .withKilde(BRUKER));

            xmlInntektFormue.withLivsforsikring(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum(
                            "inntekt.bankinnskudd.false.type.livsforsikring"
                    )))
                    .withKilde(BRUKER));

            xmlInntektFormue.withAksjer(new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum(
                            "inntekt.bankinnskudd.false.type.aksjer"
                    )))
                    .withKilde(BRUKER));

            Boolean annetVerdi = Boolean.valueOf(webSoknad.getValueForFaktum(
                    "inntekt.bankinnskudd.false.type.annet"
            ));
            xmlInntektFormue.withBankinnskuddAnnet(new XMLBoolean()
                    .withValue(annetVerdi)
                    .withKilde(BRUKER));

            if(annetVerdi) {
                xmlInntektFormue.withBankinnskuddAnnetBeskrivelse(new XMLString()
                        .withValue(webSoknad.getValueForFaktum("inntekt.bankinnskudd.false.type.annet.true.beskrivelse"))
                        .withKilde(BRUKER));
            }
        }

        return xmlInntektFormue
                .withMottarYtelser(mottarYtelser)
                .withSoktYtelser(soktYtelser)
                .withMottarBostotte(mottarBostotte)
                .withEierandeler(eierandeler)
                .withBankinnskudd(bankinnskudd);
    }
}