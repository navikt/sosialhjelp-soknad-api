//package no.nav.sosialhjelp.soknad.consumer.kodeverk;
//
//import no.nav.sosialhjelp.soknad.consumer.kodeverk.dto.BeskrivelseDto;
//import no.nav.sosialhjelp.soknad.consumer.kodeverk.dto.BetydningDto;
//import no.nav.sosialhjelp.soknad.consumer.kodeverk.dto.KodeverkDto;
//import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Map;
//
//import static java.util.Collections.singletonList;
//import static no.nav.sosialhjelp.soknad.consumer.kodeverk.dto.BetydningDto.SPRAAKKODE_NB;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KODEVERK_LAST_POLL_TIME_KEY;
//import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KOMMUNER_CACHE_KEY;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class KodeverkServiceTest {
//
//    private final String kommunenr1 = "1234";
//    private final String kommunenr2 = "5678";
//    private final String oslo = "Oslo";
//    private final String bergen = "Bergen";
//
//    private final String landkode1 = "NOR";
//    private final String landkode2 = "SWE";
//    private final String norge = "NORGE";
//    private final String sverige = "SVERIGE";
//
//    private final String postnummer1 = "0212";
//    private final String postnummer2 = "5050";
//
//    @Mock
//    private KodeverkConsumer kodeverkConsumer;
//    @Mock
//    private RedisService redisService;
//    @InjectMocks
//    private KodeverkService kodeverkService;
//
//    @Test
//    void skalHenteKommunenummerForKommunenavn_fraConsumer() {
//        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
//        when(kodeverkConsumer.hentKommuner()).thenReturn(kommuneKodeverk());
//
//        var kommunenummer = kodeverkService.gjettKommunenummer(oslo);
//
//        assertThat(kommunenummer).isNotNull();
//        assertThat(kommunenummer).isEqualTo(kommunenr1);
//    }
//
//    @Test
//    void skalHenteKommunenummerForKommunenavn_fraCache() {
//        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//        when(redisService.get(KOMMUNER_CACHE_KEY, KodeverkDto.class)).thenReturn(kommuneKodeverk());
//
//        var kommunenummer = kodeverkService.gjettKommunenummer(bergen);
//
//        assertThat(kommunenummer).isNotNull();
//        assertThat(kommunenummer).isEqualTo(kommunenr2);
//    }
//
//    @Test
//    void skalFeileHvisConsumerOgCacheGirNull() {
//        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(LocalDateTime.now().minusMinutes(61).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//        when(redisService.get(KOMMUNER_CACHE_KEY, KodeverkDto.class)).thenReturn(null);
//        when(kodeverkConsumer.hentKommuner()).thenReturn(null);
//
//        var kommunenummer = kodeverkService.gjettKommunenummer(oslo);
//
//        assertThat(kommunenummer).isNull();
//    }
//
//    @Test
//    void skalFeileHvisTermIkkeFinnes() {
//        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
//        when(redisService.get(KOMMUNER_CACHE_KEY, KodeverkDto.class)).thenReturn(kommuneKodeverk());
//
//        var kommunenummer = kodeverkService.gjettKommunenummer("ukjentKommunenavn");
//
//        assertThat(kommunenummer).isNull();
//    }
//
//    @Test
//    void skalHentePoststedFraPostnummer_fraConsumer() {
//        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
//        when(kodeverkConsumer.hentPostnummer()).thenReturn(postnummerKodeverk());
//
//        var poststed = kodeverkService.getPoststed(postnummer1);
//
//        assertThat(poststed).isNotNull();
//        assertThat(poststed).isEqualTo(oslo);
//    }
//
//    @Test
//    void skalHenteLandFraLandkode_fraConsumer() {
//        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
//        when(kodeverkConsumer.hentLandkoder()).thenReturn(landkoderKodeverk());
//
//        var land = kodeverkService.getLand(landkode1);
//
//        assertThat(land).isNotNull();
//        assertThat(land).isEqualTo("Norge");
//    }
//
//    @Test
//    void skalFeileHvisKodeverdiIkkeFinnes() {
//        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
//
//        var land = kodeverkService.getLand("ukjentLandkode");
//
//        assertThat(land).isNull();
//    }
//
//    @Test
//    void skalFormatereLandKorrekt() {
//        var now = LocalDate.now();
//        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
//        when(kodeverkConsumer.hentLandkoder()).thenReturn(
//                new KodeverkDto(Map.of(
//                        "WLF", singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto("WALLIS/FUTUNAØYENE", "WALLIS/FUTUNAØYENE")))),
//                        "STP", singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto("SAO TOME OG PRINCIPE", "SAO TOME OG PRINCIPE")))),
//                        "PNG", singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto("PAPUA NY-GUINEA", "PAPUA NY-GUINEA"))))
//                ))
//        );
//
//        var land = kodeverkService.getLand("WLF");
//        var land2 = kodeverkService.getLand("STP");
//        var land3 = kodeverkService.getLand("PNG");
//
//        assertThat(land).isEqualTo("Wallis/Futunaøyene");
//        assertThat(land2).isEqualTo("Sao Tome og Principe");
//        assertThat(land3).isEqualTo("Papua Ny-Guinea");
//    }
//
//    private KodeverkDto kommuneKodeverk() {
//        var now = LocalDate.now();
//        return new KodeverkDto(Map.of(
//                kommunenr1,
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(oslo, oslo)))),
//                kommunenr2,
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(bergen, bergen)))))
//        );
//    }
//
//    private KodeverkDto landkoderKodeverk() {
//        var now = LocalDate.now();
//        return new KodeverkDto(Map.of(
//                landkode1,
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(norge, norge)))),
//                landkode2,
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(sverige, sverige)))),
//                "WLF",
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto("WALLIS/FUTUNAØYENE", "WALLIS/FUTUNAØYENE")))),
//                "STP",
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto("SAO TOME OG PRINCIPE", "SAO TOME OG PRINCIPE")))),
//                "PNG",
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto("PAPUA NY-GUINEA", "PAPUA NY-GUINEA"))))
//        )
//        );
//    }
//
//    private KodeverkDto postnummerKodeverk() {
//        var now = LocalDate.now();
//        return new KodeverkDto(Map.of(
//                postnummer1,
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(oslo, oslo)))),
//                postnummer2,
//                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(bergen, bergen)))))
//        );
//    }
//}