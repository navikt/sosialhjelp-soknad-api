package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLString;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;


public class FamiliesituasjonTilXml implements Function<WebSoknad, XMLFamiliesituasjon> {

    @Override
    public XMLFamiliesituasjon apply(WebSoknad webSoknad) {

        XMLString sivilstatus = new XMLString()
                .withValue(webSoknad.getValueForFaktum("begrunnelse.hva"))
                .withKilde(BRUKER);

        return new XMLFamiliesituasjon()
                .withSivilstatus(sivilstatus);
    }
}