package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBoolean;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon.Sivilstatus;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSivilstatus;
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

        Boolean mottatBostotte = Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.bostotte"));
        XMLBoolean mottarBostotte = new XMLBoolean()
                .withKilde(BRUKER)
                .withValue(mottatBostotte);

        XMLInntektFormue xmlInntektFormue = new XMLInntektFormue();

        //forvirrende på grunn av booleanradio som forventer at nei verdien er det som skal gi oppfølgingspørsmål
        if (mottatBostotte == false) {
            XMLBoolean husbanken = new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.bostotte.type.husbanken")))
                    .withKilde(BRUKER);
            XMLBoolean kommunal = new XMLBoolean()
                    .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("inntekt.bostotte.type.kommunal")))
                    .withKilde(BRUKER);

            xmlInntektFormue
                    .withHusbanken(husbanken)
                    .withKommunal(kommunal);
        }

        return xmlInntektFormue
                .withMottarYtelser(mottarYtelser)
                .withSoktYtelser(soktYtelser)
                .withMottarBostotte(mottarBostotte);
    }
}