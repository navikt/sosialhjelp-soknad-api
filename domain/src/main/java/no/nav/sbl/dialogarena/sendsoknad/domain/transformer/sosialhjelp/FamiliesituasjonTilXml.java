package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon.XMLForsorgerAnsvar;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon.XMLForsorgerAnsvar.XMLAnsvarliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon.XMLForsorgerAnsvar.XMLAnsvarliste.XMLAnsvar;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLFamiliesituasjon.XMLSivilstatus;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLPerson;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLSivilstatus.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilInteger;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class FamiliesituasjonTilXml implements Function<WebSoknad, XMLFamiliesituasjon> {

    @Override
    public XMLFamiliesituasjon apply(WebSoknad webSoknad) {
        return new XMLFamiliesituasjon()
                .withSivilstatus(lagSivilstatus(webSoknad))
                .withForsorgerAnsvar(lagForsorgerAnsvar(webSoknad));
    }

    private XMLSivilstatus lagSivilstatus(WebSoknad webSoknad) {
        XMLSivilstatus sivilstatus = new XMLSivilstatus();

        String sivilStatusVerdi = webSoknad.getValueForFaktum("familie.sivilstatus");

        String borsammen = null, ikkeSammenBegrunnelse = null;

        if ("gift".equals(sivilStatusVerdi)) {
            Faktum ektefelle = webSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle");
            sivilstatus.setEktefelle(faktumTilPerson(ektefelle, BRUKER));
            borsammen = ektefelle.getProperties().get("borsammen");

            if ("false".equals(borsammen)) {
                ikkeSammenBegrunnelse = ektefelle.getProperties().get("ikkesammenbeskrivelse");
            }
        }

        return sivilstatus
                .withStatus(tilString(getSivilstatusKode(sivilStatusVerdi)))
                .withBorSammenMedEktefelle(tilString(borsammen))
                .withBorIkkeSammenMedEktefelleBegrunnelse(tilString(ikkeSammenBegrunnelse));
    }

    private XMLForsorgerAnsvar lagForsorgerAnsvar(WebSoknad webSoknad) {
        return new XMLForsorgerAnsvar()
                .withHarForsorgerAnsvar(tilString(webSoknad, "familie.barn"))
                .withAnsvarliste(new XMLAnsvarliste().withAnsvars(
                        webSoknad.getFaktaMedKey("familie.barn.true.barn").stream()
                                .map(this::lagBarn)
                                .collect(Collectors.toList())
                ));
    }

    private XMLAnsvar lagBarn(Faktum barn) {
        String borsammen = barn.getProperties().get("borsammen");
        String grad = null;

        if ("true".equals(borsammen)) {
            grad = barn.getProperties().get("grad");
        }

        return new XMLAnsvar()
                .withBarn(faktumTilPerson(barn, BRUKER))
                .withBorSammen(tilString(borsammen))
                .withSamvarsgrad(tilInteger(grad));
    }

    private String getSivilstatusKode(String faktumVerdi) {
        switch (faktumVerdi) {
            case "gift":
                return GIFT.value();
            case "ugift":
                return UGIFT.value();
            case "samboer":
                return SAMBOER.value();
            case "enke":
                return ENKE.value();
            case "skilt":
                return SKILT.value();
            default:
                return faktumVerdi;
        }
    }

    private XMLPerson faktumTilPerson(Faktum faktum, XMLKilde kilde) {
        Map<String, String> props = faktum.getProperties();
        return new XMLPerson()
                .withKilde(kilde.value())
                // TODO navn er splittet
                .withFornavn(props.getOrDefault("navn", ""))
                .withFodselsdato(props.getOrDefault("fnr", ""))
                .withPersonnummer(props.getOrDefault("pnr", ""));
    }
}