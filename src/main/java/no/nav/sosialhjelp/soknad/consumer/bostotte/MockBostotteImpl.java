package no.nav.sosialhjelp.soknad.consumer.bostotte;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.BostotteDto;
import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.BostotteMottaker;
import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.BostotteRolle;
import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.BostotteStatus;
import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.SakerDto;
import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.UtbetalingerDto;
import no.nav.sosialhjelp.soknad.consumer.bostotte.dto.VedtakDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MockBostotteImpl implements Bostotte {
    private static final Logger logger = LoggerFactory.getLogger(MockBostotteImpl.class);

    private static final Map<String, BostotteDto> responses = new HashMap<>();
    private static final Map<String, Boolean> personnummereSomSkalFeile = new HashMap<>();

    public static void setBostotteData(String fnr, String bostotteJson) {
        ObjectMapper mapper = new ObjectMapper();
        BostotteDto bostotteDto = null;
        try {
            bostotteDto = mapper.readValue(bostotteJson, BostotteDto.class);
        } catch (IOException e) {
            logger.error("Problemer med å tolke json stingen til mocken!", e);
        }
        BostotteDto response = responses.get(fnr);
        if (response == null) {
            responses.put(fnr, bostotteDto);
        } else {
            responses.replace(fnr, bostotteDto);
        }
    }

    public static void settPersonnummerSomSkalFeile(String fnr, boolean skalFeile) {
        Boolean funnet = personnummereSomSkalFeile.get(fnr);
        if (funnet == null) {
            personnummereSomSkalFeile.put(fnr, skalFeile);
        } else {
            personnummereSomSkalFeile.replace(fnr, skalFeile);
        }
    }

    @Override
    public BostotteDto hentBostotte(String personIdentifikator, String token, LocalDate fra, LocalDate til) {
        Boolean feilet = personnummereSomSkalFeile.get(personIdentifikator);
        if (feilet != null && feilet) {
            return null;
        }
        BostotteDto response = responses.get(personIdentifikator);
        if (response != null) {
            return response;
        }
        return hentStandardBostotteDtoMock();
    }

    private BostotteDto hentStandardBostotteDtoMock() {
        BostotteMottaker mottaker1 = BostotteMottaker.KOMMUNE;
        BostotteMottaker mottaker2 = BostotteMottaker.HUSSTAND;
        BigDecimal belop1 = BigDecimal.valueOf(10000);
        BigDecimal belop2 = BigDecimal.valueOf(20000);
        LocalDate utbetalingsDato = LocalDate.now();
        LocalDate saksDato1 = LocalDate.now().minusDays(3);
        LocalDate saksDato2 = LocalDate.now().minusDays(33);
        BostotteStatus saksStatus1 = BostotteStatus.VEDTATT;
        BostotteStatus saksStatus2 = BostotteStatus.UNDER_BEHANDLING;
        BostotteRolle rolle1 = BostotteRolle.HOVEDPERSON;
        BostotteRolle rolle2 = BostotteRolle.BIPERSON;
        String vedtaksKode = "V03";
        String vedtaksBeskrivelse = "Avslag - For høy inntekt";
        JsonBostotteSak.Vedtaksstatus vedtaksstatus = JsonBostotteSak.Vedtaksstatus.AVSLAG;

        UtbetalingerDto utbetalingerDto1 = new UtbetalingerDto()
                .with(mottaker1, belop1, utbetalingsDato, BostotteRolle.HOVEDPERSON);
        UtbetalingerDto utbetalingerDto2 = new UtbetalingerDto()
                .with(mottaker2, belop2, utbetalingsDato, BostotteRolle.HOVEDPERSON);
        VedtakDto vedtakDto = new VedtakDto()
                .with(vedtaksKode, vedtaksBeskrivelse, vedtaksstatus.toString());
        SakerDto sakerDto1 = new SakerDto()
                .with(saksDato1.getMonthValue(), saksDato1.getYear(), saksStatus1, vedtakDto, rolle1);
        SakerDto sakerDto2 = new SakerDto()
                .with(saksDato2.getMonthValue(), saksDato2.getYear(), saksStatus2, null, rolle2);

        return new BostotteDto()
                .withUtbetaling(utbetalingerDto1)
                .withUtbetaling(utbetalingerDto2)
                .withSak(sakerDto1)
                .withSak(sakerDto2);
    }
}
