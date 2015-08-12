package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

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
        return Arrays.asList("NAV 08-14.01");
    }

    @Override
    public List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers() {
        Transformer<WebSoknad, AlternativRepresentasjon> transformer = new Transformer<WebSoknad, AlternativRepresentasjon>() {
            @Override
            public AlternativRepresentasjon transform(final WebSoknad webSoknad) {
                return new AlternativRepresentasjon();
            }
        };

        return Arrays.asList(transformer);
    }
}
