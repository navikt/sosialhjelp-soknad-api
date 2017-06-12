package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.*;
import org.springframework.context.*;

import java.util.*;

import static java.util.Arrays.*;

public class SoknadTilleggsstonader extends KravdialogInformasjon.DefaultOppsett {

    public String getSoknadTypePrefix() {
        return "soknadtilleggsstonader";
    }

    public String getSoknadUrlKey() {
        return "soknadtilleggsstonader.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknadtilleggsstonader.path";
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
    public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {
        AlternativRepresentasjonTransformer tilleggsstonaderTilXml = new TilleggsstonaderTilXml(messageSource);
        Event event = MetricsFactory.createEvent("soknad.alternativrepresentasjon.aktiv");
        event.addTagToReport("skjemanummer", soknad.getskjemaNummer());
        event.addTagToReport("soknadstype", getSoknadTypePrefix());
        event.report();
        return asList(tilleggsstonaderTilXml);
    }

    @Override
    public String getBundleName() {
        return "tilleggsstonader";
    }
}
