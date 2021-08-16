package no.nav.sosialhjelp.soknad.consumer.fiks;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.KommuneStatus;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static no.nav.sosialhjelp.soknad.consumer.fiks.dto.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE;
import static no.nav.sosialhjelp.soknad.consumer.fiks.dto.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT;
import static no.nav.sosialhjelp.soknad.consumer.fiks.dto.KommuneStatus.MANGLER_KONFIGURASJON;
import static no.nav.sosialhjelp.soknad.consumer.fiks.dto.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA;
import static no.nav.sosialhjelp.soknad.consumer.fiks.dto.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KOMMUNEINFO_LAST_POLL_TIME_KEY;

@Component
public class KommuneInfoService {
    private static final Logger log = LoggerFactory.getLogger(KommuneInfoService.class);
    private static final long MINUTES_TO_PASS_BETWEEN_POLL = 10;

    private final DigisosApi digisosApi;
    private final RedisService redisService;

    @Inject
    public KommuneInfoService(DigisosApi digisosApi, RedisService redisService) {
        this.digisosApi = digisosApi;
        this.redisService = redisService;
    }

    public boolean kanMottaSoknader(String kommunenummer) {
        Map<String, KommuneInfo> kommuneInfoMap = hentAlleKommuneInfo();
        if (kommuneInfoMap == null) {
            return false;
        }
        return kommuneInfoMap
                .getOrDefault(kommunenummer, new KommuneInfo("", false, false, false, false, null, false, null))
                .getKanMottaSoknader();
    }

    public boolean harMidlertidigDeaktivertMottak(String kommunenummer) {
        Map<String, KommuneInfo> kommuneInfoMap = hentAlleKommuneInfo();
        if (kommuneInfoMap == null) {
            return false;
        }
        return kommuneInfoMap
                .getOrDefault(kommunenummer, new KommuneInfo("", false, false, false, false, null, false, null))
                .getHarMidlertidigDeaktivertMottak();
    }

    public Map<String, KommuneInfo> hentAlleKommuneInfo() {
        if (skalBrukeCache()) {
            Map<String, KommuneInfo> cachedMap = redisService.getKommuneInfos();
            if (cachedMap != null && !cachedMap.isEmpty()) {
                return cachedMap;
            }
            log.info("hentAlleKommuneInfo - cache er tom.");
        }

        Map<String, KommuneInfo> kommuneInfoMap = digisosApi.hentAlleKommuneInfo();
        if (kommuneInfoMap == null || kommuneInfoMap.isEmpty()) {
            Map<String, KommuneInfo> cachedMap = redisService.getKommuneInfos();
            if (cachedMap != null && !cachedMap.isEmpty()) {
                log.info("hentAlleKommuneInfo - feiler mot Fiks. Bruker cache mens Fiks er nede.");
                return cachedMap;
            }
            log.error("hentAlleKommuneInfo - feiler mot Fiks og cache er tom.");
            return null;
        }
        return kommuneInfoMap;
    }

    private boolean skalBrukeCache() {
        String timeString = redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY);
        if (timeString == null) {
            return false;
        }
        LocalDateTime lastPollTime = LocalDateTime.parse(timeString, ISO_LOCAL_DATE_TIME);
        return lastPollTime.plusMinutes(MINUTES_TO_PASS_BETWEEN_POLL).isAfter(LocalDateTime.now());
    }

    public String getBehandlingskommune(String kommunenr, String kommunenavnFraAdresseforslag) {
        String kommunenavn = behandlingsansvarlig(kommunenr);
        if (kommunenavn != null) {
            return kommunenavn.endsWith(" kommune") ? kommunenavn.replace(" kommune", "") : kommunenavn;
        }
        return KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(kommunenr, kommunenavnFraAdresseforslag);
    }

    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
    public KommuneStatus kommuneInfo(String kommunenummer) {
        Map<String, KommuneInfo> kommuneInfoMap = hentAlleKommuneInfo();
        if (kommuneInfoMap == null) {
            return FIKS_NEDETID_OG_TOM_CACHE;
        }
        KommuneInfo kommuneInfo = kommuneInfoMap.getOrDefault(kommunenummer, null);
        log.info("Kommuneinfo for {}: {}", kommunenummer, kommuneInfo);

        if (kommuneInfo == null) {
            return MANGLER_KONFIGURASJON;
        }
        if (!kommuneInfo.getKanMottaSoknader()) {
            return HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT;
        }
        if (kommuneInfo.getHarMidlertidigDeaktivertMottak()) {
            return SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER;
        }

        return SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA;
    }

    private String behandlingsansvarlig(String kommunenummer) {
        var kommuneInfoMap = hentAlleKommuneInfo();
        KommuneInfo kommuneInfo = kommuneInfoMap != null ? kommuneInfoMap.getOrDefault(kommunenummer, defaultKommuneInfo()) : defaultKommuneInfo();
        return kommuneInfo.getBehandlingsansvarlig();
    }

    private KommuneInfo defaultKommuneInfo() {
        return new KommuneInfo("", false, false, false, false, null, false, null);
    }
}
