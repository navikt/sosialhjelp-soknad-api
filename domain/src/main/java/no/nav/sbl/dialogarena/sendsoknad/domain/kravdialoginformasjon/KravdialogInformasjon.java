package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
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

    int getSkjemaVersjon();

    List<String> getSkjemanummer();

    List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad);

    boolean brukerNyOppsummering();

    boolean skalSendeMedFullSoknad();

    String getBundleName();

    boolean brukerEnonicLedetekster();

    abstract class DefaultOppsett implements KravdialogInformasjon {
        @Override
        public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {
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
        public boolean brukerEnonicLedetekster(){
            return true;
        }
        public int getSkjemaVersjon() { return 1; }
    }
}
