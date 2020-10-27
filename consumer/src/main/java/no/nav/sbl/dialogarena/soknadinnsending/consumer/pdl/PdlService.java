package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PdlService {

    private static final Logger log = getLogger(PdlService.class);

    private final PdlConsumer pdlConsumer;
    private final PdlPersonMapper pdlPersonMapper;

    public PdlService(PdlConsumer pdlConsumer, PdlPersonMapper pdlPersonMapper) {
        this.pdlConsumer = pdlConsumer;
        this.pdlPersonMapper = pdlPersonMapper;
    }

    public Person hentPerson(String ident) {
        PdlPerson pdlPerson = pdlConsumer.hentPerson(ident);
        if (pdlPerson != null) {
            log.info("Hentet PdlPerson");
        }
        return pdlPersonMapper.mapTilPerson(pdlPerson, ident);
    }

}
