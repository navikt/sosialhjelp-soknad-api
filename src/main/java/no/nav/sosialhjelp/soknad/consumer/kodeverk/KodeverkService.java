//package no.nav.sosialhjelp.soknad.consumer.kodeverk;
//
//import no.nav.sosialhjelp.soknad.consumer.kodeverk.dto.KodeverkDto;
//import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
//import org.slf4j.Logger;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//import static no.nav.sosialhjelp.soknad.consumer.kodeverk.dto.BetydningDto.SPRAAKKODE_NB;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KODEVERK_LAST_POLL_TIME_KEY;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KOMMUNER_CACHE_KEY;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.LANDKODER_CACHE_KEY;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.POSTNUMMER_CACHE_KEY;
//import static org.slf4j.LoggerFactory.getLogger;
//
//@Component
//public class KodeverkService {
//
//    private static final Logger logger = getLogger(KodeverkService.class);
//    private static final long MINUTES_TO_PASS_BETWEEN_POLL = 60;
//
//    private final KodeverkConsumer kodeverkConsumer;
//    private final RedisService redisService;
//
//    public KodeverkService(KodeverkConsumer kodeverkConsumer, RedisService redisService) {
//        this.kodeverkConsumer = kodeverkConsumer;
//        this.redisService = redisService;
//    }
//
//    public String getKommunenavn(String kommunenummer) {
//        var kommuneKodeverk = hentKodeverkFraCacheEllerConsumer(KOMMUNER_CACHE_KEY);
//        return finnFoersteTermForKodeverdi(kommuneKodeverk, kommunenummer);
//    }
//
//    public String gjettKommunenummer(String kommunenavn) {
//        var kommuneKodeverk = hentKodeverkFraCacheEllerConsumer(KOMMUNER_CACHE_KEY);
//        return finnKodeverdiForFoersteTerm(kommuneKodeverk, kommunenavn);
//    }
//
//    public String getPoststed(String postnummer) {
//        var postnummerKodeverk = hentKodeverkFraCacheEllerConsumer(POSTNUMMER_CACHE_KEY);
//        return finnFoersteTermForKodeverdi(postnummerKodeverk, postnummer);
//    }
//
//    public String getLand(String landkode) {
//        var landkoderKodeverk = hentKodeverkFraCacheEllerConsumer(LANDKODER_CACHE_KEY);
//        var land = finnFoersteTermForKodeverdi(landkoderKodeverk, landkode);
//        return formaterLand(land);
//    }
//
//    private KodeverkDto hentKodeverkFraCacheEllerConsumer(String key) {
//        if (skalBrukeCache()) {
//            var kodeverk = hentFraCache(key);
//            if (kodeverk != null) {
//                return kodeverk;
//            }
//        }
//        switch (key) {
//            case KOMMUNER_CACHE_KEY:
//                return kodeverkFraConsumerEllerForsoekMedCacheSomFallback(key, kodeverkConsumer.hentKommuner());
//            case POSTNUMMER_CACHE_KEY:
//                return kodeverkFraConsumerEllerForsoekMedCacheSomFallback(key, kodeverkConsumer.hentPostnummer());
//            case LANDKODER_CACHE_KEY:
//                return kodeverkFraConsumerEllerForsoekMedCacheSomFallback(key, kodeverkConsumer.hentLandkoder());
//        }
//
//        return null;
//    }
//
//    private boolean skalBrukeCache() {
//        String timeString = redisService.getString(KODEVERK_LAST_POLL_TIME_KEY);
//        if (timeString == null) {
//            return false;
//        }
//        LocalDateTime lastPollTime = LocalDateTime.parse(timeString, ISO_LOCAL_DATE_TIME);
//        return lastPollTime.plusMinutes(MINUTES_TO_PASS_BETWEEN_POLL).isAfter(LocalDateTime.now());
//    }
//
//    private KodeverkDto hentFraCache(String key) {
//        return (KodeverkDto) redisService.get(key, KodeverkDto.class);
//    }
//
//    private KodeverkDto kodeverkFraConsumerEllerForsoekMedCacheSomFallback(String key, KodeverkDto kodeverk) {
//        if (kodeverk != null) {
//            return kodeverk;
//        }
//        var cachedKodeverk = hentFraCache(key);
//        if (cachedKodeverk != null) {
//            logger.info("Kodeverk-client feilet, men bruker cached kodeverk.");
//            return cachedKodeverk;
//        }
//        logger.warn("Kodeverk feiler og cache [{}] er tom", key);
//        return null;
//    }
//
//    private String finnKodeverdiForFoersteTerm(KodeverkDto kodeverk, String term) {
//        if (kodeverk == null) {
//            return null;
//        }
//
//        return kodeverk.getBetydninger().entrySet().stream()
//                .filter(entry -> {
//                    var betydningDtos = entry.getValue();
//                    if (!betydningDtos.isEmpty() && betydningDtos.get(0) != null && betydningDtos.get(0).getBeskrivelser().get(SPRAAKKODE_NB).getTerm() != null) {
//                        return term.equalsIgnoreCase(betydningDtos.get(0).getBeskrivelser().get(SPRAAKKODE_NB).getTerm());
//                    }
//                    return false;
//                })
//                .findFirst()
//                .map(Map.Entry::getKey)
//                .orElse(null);
//    }
//
//    private String finnFoersteTermForKodeverdi(KodeverkDto kodeverk, String kodeverdi) {
//        if (kodeverk != null && kodeverk.getBetydninger().containsKey(kodeverdi)) {
//            return kodeverk.getBetydninger().get(kodeverdi).get(0).getBeskrivelser().get(SPRAAKKODE_NB).getTerm();
//        }
//        return null;
//    }
//
//    private String formaterLand(String land) {
//        if (land == null) {
//            return land;
//        }
//        String formaterMedSpace = setUpperCaseBeforeRegex(land.toLowerCase(), " ");
//        String formaterMedDash = setUpperCaseBeforeRegex(formaterMedSpace, "-");
//        return setUpperCaseBeforeRegex(formaterMedDash, "/");
//    }
//
//    private String setUpperCaseBeforeRegex(String s, String regex) {
//        String[] split = s.split(regex);
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < split.length; i++) {
//            if (i > 0) {
//                sb.append(regex);
//            }
//
//            if (split[i].equals("og")) {
//                sb.append(split[i]);
//            } else {
//                sb.append(split[i].substring(0, 1).toUpperCase());
//                sb.append(split[i].substring(1));
//            }
//        }
//        return sb.toString();
//    }
//}
