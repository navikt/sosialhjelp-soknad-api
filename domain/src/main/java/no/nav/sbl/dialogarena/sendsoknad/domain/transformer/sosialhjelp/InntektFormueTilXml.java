package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBoolean;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilXMLBoolean;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilXMLString;


public class InntektFormueTilXml implements Function<WebSoknad, XMLInntektFormue> {

    @Override
    public XMLInntektFormue apply(WebSoknad webSoknad) {
        XMLInntektFormue xmlInntektFormue = new XMLInntektFormue();

        String mottattBostotte = webSoknad.getValueForFaktum("inntekt.bostotte");

        xmlInntektFormue
                .withMottarBostotte(tilXMLBoolean(Boolean.valueOf(mottattBostotte)))
                .withInntekter(tilXMLBoolean(webSoknad, "inntekt.mottarytelser"))
                .withSoktYtelser(tilXMLBoolean(webSoknad, "inntekt.soktytelser"));


        //forvirrende på grunn av booleanradio som forventer at nei verdien er det som skal gi oppfølgingspørsmål
        if (mottattBostotte.equals("false")) {
            xmlInntektFormue
                    .withHusbanken(tilXMLBoolean(webSoknad, "inntekt.bostotte.type.husbanken"))
                    .withKommunal(tilXMLBoolean(webSoknad, "inntekt.bostotte.type.kommunal"));
        }

        String eierandelerVerdi = webSoknad.getValueForFaktum("inntekt.eierandeler");
        xmlInntektFormue.withEierandeler(tilXMLBoolean(Boolean.valueOf(eierandelerVerdi)));

        if (eierandelerVerdi.equals("false")) {
            xmlInntektFormue
                    .withBolig(tilXMLBoolean(webSoknad, "inntekt.eierandeler.false.type.bolig"))
                    .withKjoretoy(tilXMLBoolean(webSoknad, "inntekt.eierandeler.false.type.kjoretoy"))
                    .withCampingvogn(tilXMLBoolean(webSoknad, "inntekt.eierandeler.false.type.campingvogn"))
                    .withFritidseiendom(tilXMLBoolean(webSoknad, "inntekt.eierandeler.false.type.fritidseiendom"));

            Boolean annetVerdi = Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.eierandeler.false.type.annet"));
            xmlInntektFormue.withAnnet(tilXMLBoolean(annetVerdi));

            if (annetVerdi) {
                xmlInntektFormue.withAnnetBeskrivelse(tilXMLString(webSoknad, "inntekt.eierandeler.false.type.annet.true.beskrivelse"));
            }
        }

        String bankinnskuddVerdi = webSoknad.getValueForFaktum("inntekt.bankinnskudd");
        xmlInntektFormue.withBankinnskudd(tilXMLBoolean(Boolean.valueOf(bankinnskuddVerdi)));

        if (bankinnskuddVerdi.equals("false")) {
            xmlInntektFormue
                    .withSparekonto(tilXMLBoolean(webSoknad, "inntekt.bankinnskudd.false.type.sparekonto"))
                    .withBrukskonto(tilXMLBoolean(webSoknad, "inntekt.bankinnskudd.false.type.brukskonto"))
                    .withLivsforsikring(tilXMLBoolean(webSoknad, "inntekt.bankinnskudd.false.type.livsforsikring"))
                    .withAksjer(tilXMLBoolean(webSoknad, "inntekt.bankinnskudd.false.type.aksjer"));

            Boolean annetVerdi = Boolean.valueOf(webSoknad.getValueForFaktum(
                    "inntekt.bankinnskudd.false.type.annet"
            ));

            xmlInntektFormue.withBankinnskuddAnnet(tilXMLBoolean(annetVerdi));

            if (annetVerdi) {
                xmlInntektFormue.withBankinnskuddAnnetBeskrivelse(tilXMLString(webSoknad, "inntekt.bankinnskudd.false.type.annet.true.beskrivelse"));
            }
        }

        String inntekterVerdi = webSoknad.getValueForFaktum("inntekt.inntekter");
        xmlInntektFormue.withInntekter(tilXMLBoolean(Boolean.valueOf(inntekterVerdi)));

        if (inntekterVerdi.equals("false")) {
            xmlInntektFormue.withUtbytte(tilXMLBoolean(webSoknad, "inntekt.inntekter.false.type.utbytte"))
                    .withSalg(tilXMLBoolean(webSoknad, "inntekt.inntekter.false.type.salg"))
                    .withLeieinntekter(tilXMLBoolean(webSoknad, "inntekt.inntekter.false.type.leieinntekter"))
                    .withForsikringsutbetaling(tilXMLBoolean(webSoknad, "inntekt.inntekter.false.type.forsikringsutbetalinger"));

            Boolean annetVerdi = Boolean.valueOf(webSoknad.getValueForFaktum(
                    "inntekt.inntekter.false.type.annet"
            ));
            xmlInntektFormue.withAndreInntekter(tilXMLBoolean(annetVerdi));

            if (annetVerdi) {
                xmlInntektFormue.withAndreInntekterBeskrivelse(tilXMLString(webSoknad, "inntekt.inntekter.false.type.annet.true.beskrivelse"));
            }
        }

        return xmlInntektFormue;
    }
}