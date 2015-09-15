package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Steg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;

public interface KravdialogInformasjon {

    String BOLK_PERSONALIA = "Personalia";
    String BOLK_BARN = "Barn";
    String BOLK_ARBEIDSFORHOLD = "Arbeidsforhold";

    Steg[] getStegliste();

    String getSoknadTypePrefix();

    String getSoknadUrlKey();

    String getFortsettSoknadUrlKey();

    List<String> getSoknadBolker(WebSoknad soknad);

    String getStrukturFilnavn();

    List<String> getSkjemanummer();

    List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers(MessageSource messageSource);

    public abstract class DefaultOppsett implements KravdialogInformasjon {
        @Override
        public List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers(MessageSource messageSource) {
            return new ArrayList<>();
        }

        @Override
        public Steg[] getStegliste() {
            return new Steg[]{Steg.VEILEDNING, Steg.SOKNAD, Steg.VEDLEGG, Steg.OPPSUMMERING};
        }
    }
}
