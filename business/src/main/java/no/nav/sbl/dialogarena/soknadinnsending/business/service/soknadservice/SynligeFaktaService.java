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

    private String finnPanelForStruktur(FaktumStruktur faktumStruktur) {
        if (faktumStruktur.getPanel() != null && !faktumStruktur.getPanel().isEmpty()) {
            return faktumStruktur.getPanel();
        } else if (faktumStruktur.getDependOn() != null) {
            return finnPanelForStruktur(faktumStruktur.getDependOn());
        } else {
            return null;
        }
    }

    private Faktum finnFaktumForStruktur(FaktumStruktur faktumStruktur, WebSoknad soknad) {
        Faktum faktum = soknad.getFaktumMedKey(faktumStruktur.getId());
        if (faktum != null) {
            return faktum;
        }

        if ("true".equals(faktumStruktur.getFlereTillatt())) {
            return lagDummyFaktum(faktumStruktur, soknad);
        }

        return null;
    }

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

