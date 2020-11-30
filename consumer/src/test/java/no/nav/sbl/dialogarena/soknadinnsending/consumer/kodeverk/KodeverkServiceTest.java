package no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk;

import no.nav.sbl.dialogarena.redis.RedisService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.BeskrivelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.BetydningDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.KodeverkDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.redis.CacheConstants.KODEVERK_LAST_POLL_TIME_KEY;
import static no.nav.sbl.dialogarena.redis.CacheConstants.KOMMUNER_CACHE_KEY;
import static no.nav.sbl.dialogarena.redis.CacheConstants.LANDKODER_CACHE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.BetydningDto.SPRAAKKODE_NB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KodeverkServiceTest {

    private final String kommunenr1 = "1234";
    private final String kommunenr2 = "5678";
    private final String oslo = "Oslo";
    private final String bergen = "Bergen";

    private final String landkode1 = "NOR";
    private final String landkode2 = "SWE";
    private final String norge = "NORGE";
    private final String sverige = "SVERIGE";

    private final String postnummer1 = "0212";
    private final String postnummer2 = "5050";

    @Mock
    private KodeverkConsumer kodeverkConsumer;
    @Mock
    private RedisService redisService;
    @InjectMocks
    private KodeverkService kodeverkService;

    @Test
    public void skalHenteKommunenummerForKommunenavn_fraConsumer() {
        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
        when(kodeverkConsumer.hentKommuner()).thenReturn(kommuneKodeverk());

        var kommunenummer = kodeverkService.gjettKommunenummer(oslo);

        assertThat(kommunenummer).isNotNull();
        assertThat(kommunenummer).isEqualTo(kommunenr1);
    }

    @Test
    public void skalHenteKommunenummerForKommunenavn_fraCache() {
        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(redisService.get(KOMMUNER_CACHE_KEY, KodeverkDto.class)).thenReturn(kommuneKodeverk());

        var kommunenummer = kodeverkService.gjettKommunenummer(bergen);

        assertThat(kommunenummer).isNotNull();
        assertThat(kommunenummer).isEqualTo(kommunenr2);
    }

    @Test
    public void skalFeileHvisConsumerOgCacheGirNull() {
        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(LocalDateTime.now().minusMinutes(61).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        when(redisService.get(KOMMUNER_CACHE_KEY, KodeverkDto.class)).thenReturn(null);
        when(kodeverkConsumer.hentKommuner()).thenReturn(null);

        var kommunenummer = kodeverkService.gjettKommunenummer(oslo);

        assertThat(kommunenummer).isNull();
    }

    @Test
    public void skalFeileHvisTermIkkeFinnes() {
        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
        when(redisService.get(KOMMUNER_CACHE_KEY, KodeverkDto.class)).thenReturn(kommuneKodeverk());

        var kommunenummer = kodeverkService.gjettKommunenummer("ukjentKommunenavn");

        assertThat(kommunenummer).isNull();
    }

    @Test
    public void skalHentePoststedFraPostnummer_fraConsumer() {
        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
        when(kodeverkConsumer.hentPostnummer()).thenReturn(postnummerKodeverk());

        var poststed = kodeverkService.getPoststed(postnummer1);

        assertThat(poststed).isNotNull();
        assertThat(poststed).isEqualTo(oslo);
    }

    @Test
    public void skalHenteLandFraLandkode_fraConsumer() {
        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
        when(kodeverkConsumer.hentLandkoder()).thenReturn(landkoderKodeverk());

        var land = kodeverkService.getLand(landkode1);

        assertThat(land).isNotNull();
        assertThat(land).isEqualTo("Norge");
    }

    @Test
    public void skalFeileHvisKodeverdiIkkeFinnes() {
        when(redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)).thenReturn(null);
        when(redisService.get(LANDKODER_CACHE_KEY, KodeverkDto.class)).thenReturn(landkoderKodeverk());

        var land = kodeverkService.getLand("ukjentLandkode");

        assertThat(land).isNull();
    }

    private KodeverkDto kommuneKodeverk() {
        var now = LocalDate.now();
        return new KodeverkDto(Map.of(
                kommunenr1,
                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(oslo, oslo)))),
                kommunenr2,
                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(bergen, bergen)))))
        );
    }

    private KodeverkDto landkoderKodeverk() {
        var now = LocalDate.now();
        return new KodeverkDto(Map.of(
                landkode1,
                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(norge, norge)))),
                landkode2,
                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(sverige, sverige)))))
        );
    }

    private KodeverkDto postnummerKodeverk() {
        var now = LocalDate.now();
        return new KodeverkDto(Map.of(
                postnummer1,
                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(oslo, oslo)))),
                postnummer2,
                singletonList(new BetydningDto(now, now, Map.of(SPRAAKKODE_NB, new BeskrivelseDto(bergen, bergen)))))
        );
    }
}