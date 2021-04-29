package no.nav.sosialhjelp.soknad.oppslag.utbetaling;

import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling.Komponent;
import no.nav.sosialhjelp.soknad.oppslag.OppslagConsumer;
import no.nav.sosialhjelp.soknad.oppslag.utbetaling.UtbetalingDto.KomponentDto;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class UtbetalingService {

    private static final Logger logger = getLogger(UtbetalingService.class);

    private final OppslagConsumer oppslagConsumer;

    public UtbetalingService(OppslagConsumer oppslagConsumer) {
        this.oppslagConsumer = oppslagConsumer;
    }

    public List<Utbetaling> getUtbetalingerSiste40Dager(String ident) {
        var utbetalingDtoList = oppslagConsumer.getUtbetalingerSiste40Dager(ident);
        if (utbetalingDtoList == null) {
            return null;
        }

        var utbetalinger = utbetalingDtoList.stream()
                .map(dto -> {
                    var utbetaling = new Utbetaling();
                    utbetaling.type = dto.getType();
                    utbetaling.netto = dto.getNetto();
                    utbetaling.brutto = dto.getBrutto();
                    utbetaling.skattetrekk = dto.getSkattetrekk();
                    utbetaling.andreTrekk = dto.getAndreTrekk();
                    utbetaling.bilagsnummer = dto.getBilagsnummer();
                    utbetaling.utbetalingsdato = dto.getUtbetalingsdato();
                    utbetaling.periodeFom = dto.getPeriodeFom();
                    utbetaling.periodeTom = dto.getPeriodeTom();
                    utbetaling.komponenter = mapToKomponentList(dto.getKomponenter());
                    utbetaling.tittel = dto.getTittel();
                    utbetaling.orgnummer = dto.getOrgnummer();
                    return utbetaling;
                })
                .collect(Collectors.toList());
        logger.info("Antall navytelser utbetaling {}", utbetalinger.size());
        return utbetalinger;
    }

    private List<Komponent> mapToKomponentList(List<KomponentDto> komponentDtoList) {
        if (komponentDtoList == null) {
            return null;
        }

        var komponenter = komponentDtoList.stream()
                .map(dto -> {
                    var komponent = new Komponent();
                    komponent.type = dto.getType();
                    komponent.belop = dto.getBelop();
                    komponent.satsType = dto.getSatsType();
                    komponent.satsBelop = dto.getSatsBelop();
                    komponent.satsAntall = dto.getSatsAntall();
                    return komponent;
                })
                .collect(Collectors.toList());
        logger.info("Antall navytelser komponent {}", komponenter.size());
        return komponenter;
    }
}
