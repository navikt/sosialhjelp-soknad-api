package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class FakeMigrasjon extends Migrasjon {
    private static final Logger logger = getLogger(FakeMigrasjon.class);

    public FakeMigrasjon() {
        super(2,"NAV XO.XO-XO");
    }

    @Override
    public WebSoknad migrer(int fraVersjon, WebSoknad soknad) {
        soknad.medDelstegStatus(DelstegStatus.UTFYLLING)
                .medVersjon(super.getTilVersjon());
        return soknad;
    }
}
