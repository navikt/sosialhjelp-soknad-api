package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.List;

public interface KravdialogInformasjon {

    String BOLK_PERSONALIA = "Personalia";
    String BOLK_BARN = "Barn";
    String BOLK_ARBEIDSFORHOLD = "Arbeidsforhold";

    String getSoknadTypePrefix();

    String getSoknadUrlKey();

    String getFortsettSoknadUrlKey();

    List<String> getSoknadBolker(WebSoknad soknad);

    String getStrukturFilnavn();

    List<String> getSkjemanummer();

    List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers();

    public abstract class KravdialogInformasjonUtenAlternativRepresentasjon implements KravdialogInformasjon {
        @Override
        public List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers() {
            return new ArrayList<>();
        }
    }

}
