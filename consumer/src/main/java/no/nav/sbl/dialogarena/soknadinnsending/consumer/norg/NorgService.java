package no.nav.sbl.dialogarena.soknadinnsending.consumer.norg;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavenhetFraLokalListe;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavenheterFraLokalListe;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer.RsNorgEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NorgService {

    private static final Logger logger = LoggerFactory.getLogger(NorgService.class);

    @Inject
    private NorgConsumer norgConsumer;

    public List<NavenhetFraLokalListe> getEnheterForKommunenummer(String kommunenummer) {
        return getAllNavenheterFromPath().navenheter.stream().filter(navenhet -> navenhet.kommunenummer.equals(kommunenummer)).collect(Collectors.toList());
    }

    public NavEnhet getEnhetForGt(String gt) {
        if (gt == null || !gt.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("GT ikke på gyldig format: " + gt);
        }

        RsNorgEnhet rsNorgEnhet = norgConsumer.getEnhetForGeografiskTilknytning(gt);
        if (rsNorgEnhet == null) {
            logger.warn("Kunne ikke finne NorgEnhet for gt: " + gt);
            return null;
        }

        NavEnhet enhet = new NavEnhet();
        enhet.enhetNr = rsNorgEnhet.enhetNr;
        enhet.navn = rsNorgEnhet.navn;
        if (rsNorgEnhet.enhetNr.equals("0513")  && gt.equals("3434")){
            /*
            Jira sak 1200

            Lom og Skjåk har samme enhetsnummer. Derfor vil alle søknader bli sendt til Skjåk når vi henter organisajonsnummer basert på enhetNr.
            Dette er en midlertidig fix for å få denne casen til å fungere.
            */
            enhet.sosialOrgnr = "974592274";
        } else if (rsNorgEnhet.enhetNr.equals("0511")  && gt.equals("3432")){
            enhet.sosialOrgnr = "964949204";
        } else {
            enhet.sosialOrgnr = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(rsNorgEnhet.enhetNr);
        }


        return enhet;
    }

    private NavenheterFraLokalListe getAllNavenheterFromPath() {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/navenhet.json");
            if (resourceAsStream == null) {
                return null;
            }
            String json = IOUtils.toString(resourceAsStream);
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, NavenheterFraLokalListe.class);
        } catch (IOException e) {
            return null;
        }
    }
}
