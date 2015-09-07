package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoknadRefusjonDagligreise implements KravdialogInformasjon {

    public String getSoknadTypePrefix() {
        return "soknadrefusjondagligreise";
    }

    public String getSoknadUrlKey() {
        return "soknadrefusjondagligreise.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknadrefusjondagligreise.path";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Collections.emptyList();
    }

    public String getStrukturFilnavn() {
        return "refusjondagligreise.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 11-12.12", "NAV 11-12.13"); //TODO finn riktig skjemanummer
    }

    @Override
    public List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers() {
        return Collections.emptyList();
    }
}
