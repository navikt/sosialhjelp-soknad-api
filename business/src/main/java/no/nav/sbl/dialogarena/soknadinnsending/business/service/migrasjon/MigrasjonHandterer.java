package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

@Component
public class MigrasjonHandterer{

    private final BrukerregistrertNavnMigrasjon migrasjon = new BrukerregistrertNavnMigrasjon();

    public WebSoknad handterMigrasjon(WebSoknad soknad){
        if (soknad != null) {
            return migrasjon.migrer(soknad);
        }
        return soknad;
    }
}