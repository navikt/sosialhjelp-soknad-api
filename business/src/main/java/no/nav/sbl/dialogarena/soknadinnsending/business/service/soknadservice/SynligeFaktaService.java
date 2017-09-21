package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SynligeFaktaService {

    @Inject
    private SoknadService soknadService;

    public List<FaktumStruktur> finnSynligeFaktaForSoknad(String behandlingsid, String panelFilter) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsid, true, true);
        SoknadStruktur struktur = soknadService.hentSoknadStruktur(soknad.getskjemaNummer());

        return struktur.getFakta().stream()
                .filter(faktumStruktur -> panelFilter.equals(finnPanelForStruktur(faktumStruktur)))
                .filter(faktumStruktur -> !"hidden".equals(faktumStruktur.getType()))
                .filter(faktumStruktur -> {
                    Faktum faktum = soknad.getFaktumMedKey(faktumStruktur.getId());
                    return faktumStruktur.erSynlig(soknad, faktum);
                }).collect(Collectors.toList());
    }

    private String finnPanelForStruktur(FaktumStruktur faktumStruktur) {
        if (faktumStruktur.getPanel() != null && !faktumStruktur.getPanel().isEmpty()) {
            return faktumStruktur.getPanel();
        } else if (faktumStruktur.getDependOn() != null) {
            return finnPanelForStruktur(faktumStruktur.getDependOn());
        } else {
            return null;
        }
    }

}

