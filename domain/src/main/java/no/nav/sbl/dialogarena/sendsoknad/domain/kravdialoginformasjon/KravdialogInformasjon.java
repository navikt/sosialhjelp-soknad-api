package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;

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
