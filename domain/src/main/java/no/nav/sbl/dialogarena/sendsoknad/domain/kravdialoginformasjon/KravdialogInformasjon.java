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

    Steg[] getStegliste();

    String getSoknadTypePrefix();

    String getSoknadUrlKey();

    String getFortsettSoknadUrlKey();

    List<String> getSkjemanummer();

    String getBundleName();

    abstract class DefaultOppsett implements KravdialogInformasjon {

        public static final int VERSJON = 0;

        @Override
        public Steg[] getStegliste() {
            return new Steg[]{VEILEDNING, SOKNAD, VEDLEGG, OPPSUMMERING};
        }
    }
}
