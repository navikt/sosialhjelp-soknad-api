package no.nav.sosialhjelp.soknad.consumer.norg;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sosialhjelp.soknad.client.norg.NorgClient;
import no.nav.sosialhjelp.soknad.client.norg.dto.NavEnhetDto;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.domain.model.navenhet.NavEnhet;
import no.nav.sosialhjelp.soknad.domain.model.navenhet.NavenhetFraLokalListe;
import no.nav.sosialhjelp.soknad.domain.model.navenhet.NavenheterFraLokalListe;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.stream.Collectors.toList;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.GT_CACHE_KEY_PREFIX;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.GT_LAST_POLL_TIME_PREFIX;

@Service
public class NorgService {

    private static final Logger logger = LoggerFactory.getLogger(NorgService.class);
    private static final String NAVENHET_PATH = "/navenhet.json";
    private static final long MINUTES_TO_PASS_BETWEEN_POLL = 60;

//    private final NorgConsumer norgConsumer;
    private final NorgClient norgClient;
    private final RedisService redisService;

    private List<NavenhetFraLokalListe> cachedNavenheterFraLokalListe;

    public NorgService(
//            NorgConsumer norgConsumer,
            NorgClient norgClient,
            RedisService redisService
    ) {
//        this.norgConsumer = norgConsumer;
        this.norgClient = norgClient;
        this.redisService = redisService;
    }

    public List<NavEnhet> getEnheterForKommunenummer(String kommunenummer) {
        return getNavenhetForKommunenummerFraCacheEllerLokalListe(kommunenummer)
                .stream()
                .map(this::mapToNavEnhet)
                .distinct()
                .collect(toList());
    }

    private NavEnhet mapToNavEnhet(NavenhetFraLokalListe navenhetFraLokalListe) {
        NavEnhet navEnhet = new NavEnhet();
        navEnhet.navn = navenhetFraLokalListe.enhetsnavn;
        navEnhet.enhetNr = navenhetFraLokalListe.enhetsnummer;
        navEnhet.kommunenavn = navenhetFraLokalListe.kommunenavn;
        navEnhet.sosialOrgnr = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(navenhetFraLokalListe.enhetsnummer);
        return navEnhet;
    }

    public NavEnhet getEnhetForGt(String gt) {
        if (gt == null || !gt.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("GT ikke på gyldig format: " + gt);
        }

        var navEnhetDto = hentFraCacheEllerConsumer(gt);

        if (navEnhetDto == null) {
            logger.warn("Kunne ikke finne NorgEnhet for gt: {}", gt);
            return null;
        }

        NavEnhet enhet = new NavEnhet();
        enhet.enhetNr = navEnhetDto.getEnhetNr();
        enhet.navn = navEnhetDto.getNavn();
        if (navEnhetDto.getEnhetNr().equals("0513") && gt.equals("3434")) {
            /*
            Jira sak 1200

            Lom og Skjåk har samme enhetsnummer. Derfor vil alle søknader bli sendt til Skjåk når vi henter organisajonsnummer basert på enhetNr.
            Dette er en midlertidig fix for å få denne casen til å fungere.
            */
            enhet.sosialOrgnr = "974592274";
        } else if (navEnhetDto.getEnhetNr().equals("0511") && gt.equals("3432")) {
            enhet.sosialOrgnr = "964949204";
        } else if (navEnhetDto.getEnhetNr().equals("1620") && gt.equals("5014")) {
            enhet.sosialOrgnr = "913071751";
        } else {
            enhet.sosialOrgnr = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(navEnhetDto.getEnhetNr());
        }

        return enhet;
    }

    private NavEnhetDto hentFraCacheEllerConsumer(String gt) {
        if (skalBrukeCache(gt)) {
            var cached = hentFraCache(gt);
            if (cached != null) {
                return cached;
            }
        }

        return hentFraConsumerMedCacheSomFallback(gt);
    }

    private boolean skalBrukeCache(String gt) {
        String timeString = redisService.getString(GT_LAST_POLL_TIME_PREFIX + gt);
        if (timeString == null) {
            return false;
        }
        LocalDateTime lastPollTime = LocalDateTime.parse(timeString, ISO_LOCAL_DATE_TIME);
        return lastPollTime.plusMinutes(MINUTES_TO_PASS_BETWEEN_POLL).isAfter(LocalDateTime.now());
    }

    private NavEnhetDto hentFraCache(String gt) {
        return (NavEnhetDto) redisService.get(GT_CACHE_KEY_PREFIX + gt, NavEnhetDto.class);
    }

    private NavEnhetDto hentFraConsumerMedCacheSomFallback(String gt) {
        try {
            var navEnhetDto = norgClient.hentNavEnhetForGeografiskTilknytning(gt);
            if (navEnhetDto != null) {
                return navEnhetDto;
            }
        } catch (TjenesteUtilgjengeligException e) {
            // Norg feiler -> prøv å hent tidligere cached verdi
            var cached = hentFraCache(gt);
            if (cached != null) {
                logger.info("Norg-client feilet, men bruker tidligere cachet response fra Norg");
                return cached;
            }
            logger.warn("Norg-client feilet og cache [key={}] er tom", GT_CACHE_KEY_PREFIX + gt);
            throw e;
        }

        return null;
    }

    private List<NavenhetFraLokalListe> getNavenhetForKommunenummerFraCacheEllerLokalListe(String kommunenummer) {
        if (cachedNavenheterFraLokalListe == null) {
            NavenheterFraLokalListe allNavenheterFromPath = getAllNavenheterFromPath();
            if (allNavenheterFromPath == null) {
                throw new IllegalStateException(String.format("Fant ingen navenheter i path: %s", NAVENHET_PATH));
            }
            cachedNavenheterFraLokalListe = allNavenheterFromPath.navenheter;
        }

        return cachedNavenheterFraLokalListe
                .stream()
                .filter(navenhet -> navenhet.kommunenummer.equals(kommunenummer))
                .distinct()
                .collect(toList());
    }

    NavenheterFraLokalListe getAllNavenheterFromPath() {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream(NAVENHET_PATH);
            if (resourceAsStream == null) {
                return null;
            }
            String json = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, NavenheterFraLokalListe.class);
        } catch (IOException e) {
            logger.error("IOException ved henting av navenheter fra lokal liste", e);
            return null;
        }
    }
}
