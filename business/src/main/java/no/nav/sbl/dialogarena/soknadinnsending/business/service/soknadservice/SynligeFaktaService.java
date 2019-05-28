package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class SynligeFaktaService {

    @Inject
    private SoknadService soknadService;



    private Faktum lagDummyFaktum(FaktumStruktur faktumStruktur, WebSoknad soknad) {
        Long parrentFaktum = null;
        if (faktumStruktur.getDependOn() != null) {
            parrentFaktum = soknad.getFaktumMedKey(faktumStruktur.getDependOn().getId()).getFaktumId();
        }

        return new Faktum()
                .medKey(faktumStruktur.getId())
                .medParrentFaktumId(parrentFaktum)
                .medValue("");
    }

}

