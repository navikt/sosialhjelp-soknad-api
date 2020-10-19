package no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks;

import no.nav.sbl.dialogarena.redis.RedisService;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static no.nav.sbl.dialogarena.redis.CacheConstants.KOMMUNEINFO_LAST_POLL_TIME_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus.MANGLER_KONFIGURASJON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA;
import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER;

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
        return hentAlleKommuneInfo()
                .getOrDefault(kommunenummer, new KommuneInfo("", false, false, false, false, null, false, null))
                .getKanMottaSoknader();
    }

    public boolean harMidlertidigDeaktivertMottak(String kommunenummer) {
        return hentAlleKommuneInfo()
                .getOrDefault(kommunenummer, new KommuneInfo("", false, false, false, false, null, false, null))
                .getHarMidlertidigDeaktivertMottak();
    }

    private Map<String, KommuneInfo> hentAlleKommuneInfo() {
        if (skalBrukeCache()) {
            Map<String, KommuneInfo> cachedMap = redisService.getKommuneInfos();
            if (cachedMap != null && !cachedMap.isEmpty()) {
                return cachedMap;
            }
        }

        Map<String, KommuneInfo> kommuneInfoMap = digisosApi.hentAlleKommuneInfo();
        if (kommuneInfoMap.isEmpty()) {
            log.info("hentAlleKommuneInfo - Feil mot Fiks. Forsøker å bruke cache mens Fiks er nede.");
            Map<String, KommuneInfo> cachedMap = redisService.getKommuneInfos();
            if (cachedMap != null && !cachedMap.isEmpty()) {
                return cachedMap;
            }
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
        KommuneInfo kommuneInfo = digisosApi.hentAlleKommuneInfo().getOrDefault(kommunenummer, null);
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
        return digisosApi.hentAlleKommuneInfo()
                .getOrDefault(kommunenummer, new KommuneInfo("", false, false, false, false, null, false, null))
                .getBehandlingsansvarlig();
    }
}
