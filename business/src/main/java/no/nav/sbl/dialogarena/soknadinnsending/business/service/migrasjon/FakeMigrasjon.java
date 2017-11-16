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
    public WebSoknad migreer(WebSoknad soknad, int versjon) {
        logger.debug("Migrerer til fake versjon");
        //her kan vi vurdere om vi alltid skal sende bruker tilbake til utfylling
        soknad.setDelstegStatus(DelstegStatus.UTFYLLING);
        return new WebSoknad();
    }
}
