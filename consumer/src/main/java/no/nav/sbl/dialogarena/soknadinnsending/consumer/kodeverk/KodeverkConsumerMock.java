package no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk;


import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.BeskrivelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.BetydningDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.KodeverkDto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.dto.BetydningDto.SPRAAKKODE_NB;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KodeverkConsumerMock {

    // kan muligens fjerne postnummer og kommuner her
    static final Map<String, List<BetydningDto>> postnummer =  new HashMap<>();
    static final Map<String, List<BetydningDto>> kommuner =  new HashMap<>();
    static final Map<String, List<BetydningDto>> landkoder =  new HashMap<>();

    public KodeverkConsumer kodeverkConsumerMock() {
        KodeverkConsumer mock = mock(KodeverkConsumer.class);

        when(mock.hentPostnummer()).thenReturn(new KodeverkDto(postnummer));
        when(mock.hentKommuner()).thenReturn(new KodeverkDto(kommuner));
        when(mock.hentLandkoder()).thenReturn(new KodeverkDto(landkoder));

        return mock;
    }

    static void leggTilPostnummer(String postnummer) {
        landkoder.put(postnummer, Collections.singletonList(defaultPostnummerBetydning()));
    }

    static void leggTilKommune(String kommune) {
        landkoder.put(kommune, Collections.singletonList(defaultKommuneBetydning()));
    }

    static void leggTilLandkode(String landkode) {
        landkoder.put(landkode, Collections.singletonList(defaultLandkodeBetydning()));
    }

    private static BetydningDto defaultPostnummerBetydning() {
        return new BetydningDto(
                LocalDate.of(2000,1,1),
                LocalDate.of(9999,1,1),
                Map.of(SPRAAKKODE_NB, new BeskrivelseDto("Oslo", "Oslo"))
        );
    }

    private static BetydningDto defaultKommuneBetydning() {
        return new BetydningDto(
                LocalDate.of(2000,1,1),
                LocalDate.of(9999,1,1),
                Map.of(SPRAAKKODE_NB, new BeskrivelseDto("Oslo kommune", "Oslo kommune"))
        );
    }

    private static BetydningDto defaultLandkodeBetydning() {
        return new BetydningDto(
                LocalDate.of(2000,1,1),
                LocalDate.of(9999,1,1),
                Map.of(SPRAAKKODE_NB, new BeskrivelseDto("NORGE", "NORGE"))
        );
    }
}
