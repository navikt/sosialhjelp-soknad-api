package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class InntektFormueTilXml implements Function<WebSoknad, XMLInntektFormue> {

    @Override
    public XMLInntektFormue apply(WebSoknad webSoknad) {
        XMLInntektFormue xmlInntektFormue = new XMLInntektFormue();
/*
        String mottattBostotte = webSoknad.getValueForFaktum("inntekt.bostotte");

        xmlInntektFormue
                .withMottarBostotte(SoknadSosialhjelpUtils.tilBoolean(Boolean.valueOf(mottattBostotte)))
                .withInntekter(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.mottarytelser"))
                .withSoktYtelser(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.soktytelser"));


        //forvirrende på grunn av booleanradio som forventer at nei verdien er det som skal gi oppfølgingspørsmål
        if (mottattBostotte.equals("false")) {
            xmlInntektFormue
                    .withHusbanken(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.bostotte.type.husbanken"))
                    .withKommunal(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.bostotte.type.kommunal"));
        }

        String eierandelerVerdi = webSoknad.getValueForFaktum("inntekt.eierandeler");
        xmlInntektFormue.withEierandeler(SoknadSosialhjelpUtils.tilBoolean(Boolean.valueOf(eierandelerVerdi)));

        if (eierandelerVerdi.equals("false")) {
            xmlInntektFormue
                    .withBolig(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.eierandeler.false.type.bolig"))
                    .withKjoretoy(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.eierandeler.false.type.kjoretoy"))
                    .withCampingvogn(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.eierandeler.false.type.campingvogn"))
                    .withFritidseiendom(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.eierandeler.false.type.fritidseiendom"));

            Boolean annetVerdi = Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.eierandeler.false.type.annet"));
            xmlInntektFormue.withAnnet(SoknadSosialhjelpUtils.tilBoolean(annetVerdi));

            if (annetVerdi) {
                xmlInntektFormue.withAnnetBeskrivelse(tilXMLKildeString(webSoknad, "inntekt.eierandeler.false.type.annet.true.beskrivelse"));
            }
        }

        String bankinnskuddVerdi = webSoknad.getValueForFaktum("inntekt.bankinnskudd");
        xmlInntektFormue.withBankinnskudd(SoknadSosialhjelpUtils.tilBoolean(Boolean.valueOf(bankinnskuddVerdi)));

        if (bankinnskuddVerdi.equals("false")) {
            xmlInntektFormue
                    .withSparekonto(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.bankinnskudd.false.type.sparekonto"))
                    .withBrukskonto(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.bankinnskudd.false.type.brukskonto"))
                    .withLivsforsikring(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.bankinnskudd.false.type.livsforsikring"))
                    .withAksjer(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.bankinnskudd.false.type.aksjer"));

            Boolean annetVerdi = Boolean.valueOf(webSoknad.getValueForFaktum(
                    "inntekt.bankinnskudd.false.type.annet"
            ));

            xmlInntektFormue.withBankinnskuddAnnet(SoknadSosialhjelpUtils.tilBoolean(annetVerdi));

            if (annetVerdi) {
                xmlInntektFormue.withBankinnskuddAnnetBeskrivelse(tilXMLKildeString(webSoknad, "inntekt.bankinnskudd.false.type.annet.true.beskrivelse"));
            }
        }

        String inntekterVerdi = webSoknad.getValueForFaktum("inntekt.inntekter");
        xmlInntektFormue.withInntekter(SoknadSosialhjelpUtils.tilBoolean(Boolean.valueOf(inntekterVerdi)));

        if (inntekterVerdi.equals("false")) {
            xmlInntektFormue.withUtbytte(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.inntekter.false.type.utbytte"))
                    .withSalg(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.inntekter.false.type.salg"))
                    .withLeieinntekter(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.inntekter.false.type.leieinntekter"))
                    .withForsikringsutbetaling(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "inntekt.inntekter.false.type.forsikringsutbetalinger"));

            Boolean annetVerdi = Boolean.valueOf(webSoknad.getValueForFaktum(
                    "inntekt.inntekter.false.type.annet"
            ));
            xmlInntektFormue.withAndreInntekter(SoknadSosialhjelpUtils.tilBoolean(annetVerdi));

            if (annetVerdi) {
                xmlInntektFormue.withAndreInntekterBeskrivelse(tilXMLKildeString(webSoknad, "inntekt.inntekter.false.type.annet.true.beskrivelse"));
            }
        }
*/
        return xmlInntektFormue;
    }
}