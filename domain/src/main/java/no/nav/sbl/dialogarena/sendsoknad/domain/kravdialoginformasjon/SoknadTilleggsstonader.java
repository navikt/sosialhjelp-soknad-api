package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.*;
import org.apache.commons.collections15.*;
import org.springframework.context.*;

import java.util.*;

import static java.util.Arrays.*;

public class SoknadTilleggsstonader extends KravdialogInformasjon.DefaultOppsett {

    private static final String FORTSETT_PATH = "soknadtilleggsstonader.path";
    private static final String SOKNAD_PATH = "soknadtilleggsstonader.path";

    public String getSoknadTypePrefix() {
        return "soknadtilleggsstonader";
    }

    public String getSoknadUrlKey() {
        return SOKNAD_PATH;
    }

    public String getFortsettSoknadUrlKey() {
        return FORTSETT_PATH;
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    public String getStrukturFilnavn() {
        return "soknadtilleggsstonader.xml";
    }

    public List<String> getSkjemanummer() {
        return asList("NAV 11-12.12", "NAV 11-12.13");
    }

    @Override
    public List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers(MessageSource messageSource) {
        Transformer<WebSoknad, AlternativRepresentasjon> tilleggsstonaderTilXml = new TilleggsstonaderTilXml(messageSource);
        return asList(tilleggsstonaderTilXml);
    }
}
