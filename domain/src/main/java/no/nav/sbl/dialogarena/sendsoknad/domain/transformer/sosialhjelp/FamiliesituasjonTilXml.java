package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLSivilstatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class FamiliesituasjonTilXml implements Function<WebSoknad, XMLFamiliesituasjon> {

    @Override
    public XMLFamiliesituasjon apply(WebSoknad webSoknad) {

        XMLFamiliesituasjon familiesituasjon = new XMLFamiliesituasjon();
        XMLFamiliesituasjon.XMLSivilstatus sivilstatus = new XMLFamiliesituasjon.XMLSivilstatus();

        String sivilstatusVerdi = webSoknad.getValueForFaktum("familie.sivilstatus");
        switch (sivilstatusVerdi) {
            case "gift":
                sivilstatus.withStatus(tilString(XMLSivilstatus.GIFT.value()));
            case "ugift":
                sivilstatus.withStatus(tilString(XMLSivilstatus.UGIFT.value()));
            case "samboer":
                sivilstatus.withStatus(tilString(XMLSivilstatus.SAMBOER.value()));
            case "enke":
                sivilstatus.withStatus(tilString(XMLSivilstatus.ENKE.value()));
            case "skilt":
                sivilstatus.withStatus(tilString(XMLSivilstatus.SKILT.value()));
        }

        return familiesituasjon.withSivilstatus(sivilstatus);
//                .withHjemmeboendeBarn(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "familie.barn"))
//                .withAndreHjemmeboendeBarn(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "familie.andrebarn"));
    }
}