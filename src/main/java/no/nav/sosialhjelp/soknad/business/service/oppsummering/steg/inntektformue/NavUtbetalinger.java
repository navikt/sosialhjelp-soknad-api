package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;

public class NavUtbetalinger {

    public Avsnitt getAvsnitt(JsonOkonomiopplysninger opplysninger, JsonDriftsinformasjon driftsinformasjon) {
        var utbetalingerFraNavFeilet = Boolean.TRUE.equals(driftsinformasjon.getUtbetalingerFraNavFeilet());

        return new Avsnitt.Builder()
                .withTittel("navytelser.sporsmal")
                .withSporsmal(navUtbetalingerSporsmal(opplysninger, utbetalingerFraNavFeilet))
                .build();
    }

    private List<Sporsmal> navUtbetalingerSporsmal(JsonOkonomiopplysninger opplysninger, boolean utbetalingerFraNavFeilet) {
        if (utbetalingerFraNavFeilet) {
            // På grunn av systemfeil klarte vi ikke å hente ned informasjon om ytelser fra NAV.
            return singletonList(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.kontaktproblemer")
                            .withErUtfylt(true)
                            .build()
            );
        }
        var harNavUtbetalinger = opplysninger.getUtbetaling() != null && opplysninger.getUtbetaling().stream().anyMatch(utbetaling -> UTBETALING_NAVYTELSE.equals(utbetaling.getType()));
        if (!harNavUtbetalinger) {
            // Vi har ingen registrerte utbetalinger på deg fra NAV den siste måneden.
            return singletonList(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.ingen.true")
                            .withErUtfylt(true)
                            .build()
            );
        }

        // 1 eller flere utbetalinger
        return opplysninger.getUtbetaling().stream()
                .filter(utbetaling -> UTBETALING_NAVYTELSE.equals(utbetaling.getType()))
                .map(utbetaling -> {
                    var map = new LinkedHashMap<String, String>();
                    map.put("utbetalinger.utbetaling.type.label", utbetaling.getTittel());
                    map.put("utbetalinger.utbetaling.netto.label", utbetaling.getNetto().toString());
                    map.put("utbetalinger.utbetaling.brutto.label", utbetaling.getBrutto().toString());
                    map.put("utbetalinger.utbetaling.utbetalingsdato.label", utbetaling.getUtbetalingsdato());

                    return new Sporsmal.Builder()
                            .withTittel("utbetalinger.utbetaling.sporsmal")
                            .withErUtfylt(true)
                            .withFelt(
                                    singletonList(
                                            new Felt.Builder()
                                                    .withType(Type.SYSTEMDATA_MAP)
                                                    .withLabelSvarMap(map)
                                                    .build()
                                    )
                            )
                            .build();
                })
                .collect(Collectors.toList());
    }
}
