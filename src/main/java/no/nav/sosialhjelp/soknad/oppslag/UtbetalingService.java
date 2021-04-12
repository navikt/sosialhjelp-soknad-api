package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling.Komponent;
import no.nav.sosialhjelp.soknad.oppslag.dto.UtbetalingDto.KomponentDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UtbetalingService {

    private final OppslagConsumer oppslagConsumer;

    public UtbetalingService(OppslagConsumer oppslagConsumer) {
        this.oppslagConsumer = oppslagConsumer;
    }

    public List<Utbetaling> getUtbetalingerSiste40Dager(String ident) {
        var utbetalingDtoList = oppslagConsumer.getUtbetalingerSiste40Dager(ident);
        if (utbetalingDtoList == null) {
            return null;
        }

        return utbetalingDtoList.stream()
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
    }

    private List<Komponent> mapToKomponentList(List<KomponentDto> komponentDtoList) {
        if (komponentDtoList == null) {
            return null;
        }

        return komponentDtoList.stream()
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
    }
}
