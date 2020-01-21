package no.nav.sbl.dialogarena.soknadinnsending.consumer.norg;

import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer.RsNorgEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class NorgService {

    private static final Logger logger = LoggerFactory.getLogger(NorgService.class);

    @Inject
    private NorgConsumer norgConsumer;

    public NavEnhet finnEnhetForGt(String gt) {
        logger.info("SOK-DEBUG PUT 5: finnEnhetForGt {}", gt);
        if (gt == null || !gt.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("GT ikke på gyldig format: " + gt);
        }

        RsNorgEnhet rsNorgEnhet = norgConsumer.finnEnhetForGeografiskTilknytning(gt);
        if (rsNorgEnhet == null) {
            logger.warn("Kunne ikke finne NorgEnhet for gt: " + gt);
            return null;
        }

        logger.info("SOK-DEBUG PUT 6: rsNorgEnhet: navn {}, enhetnr {}   |||   enhetId {}, orgnr {}, sosialtjeneste {}, orgNrTilKommunaltNavKontor {}, orgNivaa {}, status {}, type {}, antallRessurser {}",
                rsNorgEnhet.navn,
                rsNorgEnhet.enhetNr,
                rsNorgEnhet.enhetId,
                rsNorgEnhet.organisasjonsnummer,
                rsNorgEnhet.sosialeTjenester,
                rsNorgEnhet.orgNrTilKommunaltNavKontor,
                rsNorgEnhet.orgNivaa,
                rsNorgEnhet.status,
                rsNorgEnhet.type,
                rsNorgEnhet.antallRessurser);

        NavEnhet enhet = new NavEnhet();
        enhet.enhetNr = rsNorgEnhet.enhetNr;
        enhet.navn = rsNorgEnhet.navn;
        if (rsNorgEnhet.enhetNr.equals("0513")  && gt.equals("0514")){
            /*
            Jira sak 1200

            Lom og Skjåk har samme enhetsnummer. Derfor vil alle søknader bli sendt til Skjåk når vi henter organisajonsnummer basert på enhetNr.
            Dette er en midlertidig fix for å få denne casen til å fungere.
            */
            enhet.sosialOrgnr = "974592274";
            logger.info("SOK-DEBUG PUT 7.1: Spesialcase 1  sosialOrgnr {}", enhet.sosialOrgnr);
        } else if (rsNorgEnhet.enhetNr.equals("0511")  && gt.equals("0512")){
            enhet.sosialOrgnr = "964949204";
            logger.info("SOK-DEBUG PUT 7.2: Spesialcase 2  sosialOrgnr {}", enhet.sosialOrgnr);
        } else {
            enhet.sosialOrgnr = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(rsNorgEnhet.enhetNr);
            logger.info("SOK-DEBUG PUT 7: sosialOrgnr {}", enhet.sosialOrgnr);
        }


        return enhet;
    }
}
