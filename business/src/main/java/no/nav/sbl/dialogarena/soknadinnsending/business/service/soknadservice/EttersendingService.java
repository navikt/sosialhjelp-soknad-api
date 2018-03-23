package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

@Component
public class EttersendingService {

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    public String start(String behandlingsIdDetEttersendesPaa) {
        if (true) {
            throw new NotImplementedException("Støtter ikke ettersendelse enda"); // TODO
        }
        return null;
    }

}
