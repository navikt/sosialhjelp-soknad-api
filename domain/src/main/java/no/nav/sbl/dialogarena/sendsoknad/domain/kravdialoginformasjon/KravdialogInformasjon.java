package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.*;

public interface KravdialogInformasjon {

    String BOLK_PERSONALIA = "Personalia";
    String BOLK_BARN = "Barn";
    String BOLK_ARBEIDSFORHOLD = "Arbeidsforhold";
    String UTBETALING_BOLK = "UtbetalingBolk";

    Steg[] getStegliste();

    String getSoknadTypePrefix();

    String getSoknadUrlKey();

    String getFortsettSoknadUrlKey();

    List<String> getSkjemanummer();

    List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad);

    List<EkstraMetadataTransformer> getMetadataTransformers(WebSoknad soknad);

    boolean brukerNyOppsummering();

    boolean skalSendeMedFullSoknad();

    String getBundleName();

    SoknadType getSoknadstype();

    String getKvitteringTemplate();

    Integer getSkjemaVersjon();

    abstract class DefaultOppsett implements KravdialogInformasjon {

        public static final int VERSJON = 0;

        @Override
        public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {
            return new ArrayList<>();
        }

        @Override
        public List<EkstraMetadataTransformer> getMetadataTransformers(WebSoknad soknad) {
            return Collections.emptyList();
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

        public SoknadType getSoknadstype() {
            return SoknadType.SEND_SOKNAD;
        }

        public String getKvitteringTemplate() {
            return "/skjema/kvittering";
        }

        @Override
        public Integer getSkjemaVersjon() { return VERSJON; }
    }
}
