package no.nav.sbl.dialogarena.soknadinnsending.business.arena;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.AktiviteterService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class VedtakService implements BolkService {

    @Inject
    private FaktaService faktaService;
    @Inject
    private AktiviteterService aktiviteterService;

    @Override
    public String tilbyrBolk() {
        return "vedtakperioder";
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        Faktum vedtakFaktum = faktaService.hentFaktumMedKey(soknadId, "vedtak");
        if (vedtakFaktum != null) {
            return aktiviteterService.hentBetalingsplanerForVedtak(fodselsnummer, vedtakFaktum.getProperties().get("aktivitetId")
                    , vedtakFaktum.getProperties().get("id"));

        }
        return null;
    }
}
