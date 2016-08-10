package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.OPPSUMMERING;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.SOKNAD;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.VEDLEGG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.VEILEDNING;

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

    boolean brukerNyOppsummering();

    boolean skalSendeMedFullSoknad();

    abstract class DefaultOppsett implements KravdialogInformasjon {
        @Override
        public List<Transformer<WebSoknad, AlternativRepresentasjon>> getTransformers(MessageSource messageSource) {
            return new ArrayList<>();
        }

        @Override
        public Steg[] getStegliste() {
            return new Steg[]{VEILEDNING, SOKNAD, VEDLEGG, OPPSUMMERING};
        }

        @Override
        public boolean brukerNyOppsummering(){
            return false;
        }
        public boolean skalSendeMedFullSoknad(){
            return false;
        }
    }
}
