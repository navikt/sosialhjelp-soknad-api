package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBoolean;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon.Sivilstatus;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSivilstatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;


public class FamiliesituasjonTilXml implements Function<WebSoknad, XMLFamiliesituasjon> {

    @Override
    public XMLFamiliesituasjon apply(WebSoknad webSoknad) {

        Sivilstatus sivilstatus = new Sivilstatus()
                .withKilde(BRUKER);

        String sivilstatusVerdi = webSoknad.getValueForFaktum("familie.sivilstatus");
        switch (sivilstatusVerdi) {
            case "gift":
                sivilstatus.withValue(XMLSivilstatus.GIFT);
            case "ugift":
                sivilstatus.withValue(XMLSivilstatus.UGIFT);
            case "samboer":
                sivilstatus.withValue(XMLSivilstatus.SAMBOER);
            case "enke":
                sivilstatus.withValue(XMLSivilstatus.ENKE);
            case "skilt":
                sivilstatus.withValue(XMLSivilstatus.SKILT);
        }

        XMLBoolean hjemmeboendeBarn = new XMLBoolean()
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("familie.barn")))
                .withKilde(BRUKER);

        XMLBoolean andreHjemmeboendeBarn = new XMLBoolean()
                .withKilde(BRUKER)
                .withValue(Boolean.valueOf(webSoknad.getValueForFaktum("familie.andrebarn")));

        return new XMLFamiliesituasjon()
                .withSivilstatus(sivilstatus)
                .withHjemmeboendeBarn(hjemmeboendeBarn)
                .withAndreHjemmeboendeBarn(andreHjemmeboendeBarn);
    }
}