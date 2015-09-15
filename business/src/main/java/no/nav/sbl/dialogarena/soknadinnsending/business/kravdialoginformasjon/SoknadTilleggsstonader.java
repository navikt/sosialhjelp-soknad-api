package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.TilleggsstonaderTilXml;
import org.apache.commons.collections15.Transformer;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

public class SoknadTilleggsstonader implements KravdialogInformasjon {

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
        return Arrays.asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    public String getStrukturFilnavn() {
        return "soknadtilleggsstonader.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 11-12.12", "NAV 11-12.13");
    }

    @Override
    public List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers(MessageSource messageSource) {
        Transformer<WebSoknad, AlternativRepresentasjon> tilleggsstonaderTilXml = new TilleggsstonaderTilXml(messageSource);
        return Arrays.asList(tilleggsstonaderTilXml);
    }
}
