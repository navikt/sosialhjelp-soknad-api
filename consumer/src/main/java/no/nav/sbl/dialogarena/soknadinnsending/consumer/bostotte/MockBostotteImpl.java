package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.SakerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.VedtakDto;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MockBostotteImpl implements Bostotte {

    private static Map<String, BostotteDto> responses = new HashMap<>();

    public static void setBostotteData(String fnr, String bostotteJson) {
        ObjectMapper mapper = new ObjectMapper();
        BostotteDto bostotteDto = null;
        try {
            bostotteDto = mapper.readValue(bostotteJson, BostotteDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BostotteDto response = responses.get(fnr);
        if (response == null){
            responses.put(fnr, bostotteDto);
        } else {
            responses.replace(fnr, bostotteDto);
        }
    }

    @Override
    public BostotteDto hentBostotte(String personIdentifikator, String token, LocalDate fra, LocalDate til) {
        BostotteDto response = responses.get(personIdentifikator);
        if (response != null){
            return response;
        }
        return hentStandardBostotteDtoMock();
    }

    private BostotteDto hentStandardBostotteDtoMock() {
        String mottaker1 = "KOMMUNE";
        String mottaker2 = "HUSSTAND";
        BigDecimal belop1 = BigDecimal.valueOf(10000);
        BigDecimal belop2 = BigDecimal.valueOf(20000);
        LocalDate utbetalingsDato = LocalDate.now();
        LocalDate saksDato1 = LocalDate.now().minusDays(3);
        LocalDate saksDato2 = LocalDate.now().minusDays(33);
        String saksStatus1 = "VEDTATT";
        String saksStatus2 = "UNDER_BEHANDLING";
        String rolle1 = "HOVEDPERSON";
        String rolle2 = "BIPERSON";
        String vedtaksKode = "V03";
        String vedtaksBeskrivelse = "Avslag - For høy inntekt";

        UtbetalingerDto utbetalingerDto1 = new UtbetalingerDto()
                .with(mottaker1, belop1, utbetalingsDato);
        UtbetalingerDto utbetalingerDto2 = new UtbetalingerDto()
                .with(mottaker2, belop2, utbetalingsDato);
        VedtakDto vedtakDto = new VedtakDto()
                .with(vedtaksKode, vedtaksBeskrivelse);
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
