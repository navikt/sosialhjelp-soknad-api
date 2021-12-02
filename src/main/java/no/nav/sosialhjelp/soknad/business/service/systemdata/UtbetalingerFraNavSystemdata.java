package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.NavUtbetalingerService;
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.Komponent;
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;
import static java.lang.Math.round;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class UtbetalingerFraNavSystemdata implements Systemdata {
    public static final Logger log = getLogger(UtbetalingerFraNavSystemdata.class);

    private final OrganisasjonService organisasjonService;
    private final NavUtbetalingerService navUtbetalingerService;

    public UtbetalingerFraNavSystemdata(
            OrganisasjonService organisasjonService,
            NavUtbetalingerService navUtbetalingerService
    ) {
        this.organisasjonService = organisasjonService;
        this.navUtbetalingerService = navUtbetalingerService;
    }

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger = jsonData.getOkonomi().getOpplysninger().getUtbetaling();

        fjernGamleUtbetalinger(okonomiOpplysningUtbetalinger);

        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setUtbetalingerFraNavFeilet(false);
        List<JsonOkonomiOpplysningUtbetaling> systemUtbetalingerNav = innhentNavSystemregistrertInntekt(personIdentifikator);
        if (systemUtbetalingerNav == null) {
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setUtbetalingerFraNavFeilet(true);
        } else {
            okonomiOpplysningUtbetalinger.addAll(systemUtbetalingerNav);
        }
    }

    private void fjernGamleUtbetalinger(List<JsonOkonomiOpplysningUtbetaling> okonomiOpplysningUtbetalinger) {
        okonomiOpplysningUtbetalinger.removeIf(
                utbetaling -> utbetaling.getType().equalsIgnoreCase(UTBETALING_NAVYTELSE));
    }

    private List<JsonOkonomiOpplysningUtbetaling> innhentNavSystemregistrertInntekt(String personIdentifikator) {
        var utbetalinger = navUtbetalingerService.getUtbetalingerSiste40Dager(personIdentifikator);
        if (utbetalinger == null) {
            return null;
        }
        return utbetalinger.stream().map(this::mapToJsonOkonomiOpplysningUtbetaling).collect(Collectors.toList());
    }

    JsonOrganisasjon mapToJsonOrganisasjon(String orgnummer) {
        if (orgnummer == null) return null;

        if (orgnummer.matches("\\d{9}")) {
            return new JsonOrganisasjon()
                    .withNavn(organisasjonService.hentOrgNavn(orgnummer))
                    .withOrganisasjonsnummer(orgnummer);
        }

        if (orgnummer.matches("\\d{11}")) {
            log.info("Utbetalingens opplysningspliktigId er et personnummer. Dette blir ikke inkludert i soknad.json");
        } else {
            log.error("Utbetalingens opplysningspliktigId er verken et organisasjonsnummer eller personnummer: {}. Kontakt skatteetaten.", orgnummer);
        }

        return null;
    }

    private JsonOkonomiOpplysningUtbetaling mapToJsonOkonomiOpplysningUtbetaling(NavUtbetaling navUtbetaling) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withKilde(JsonKilde.SYSTEM)
                .withType(UTBETALING_NAVYTELSE)
                .withTittel(navUtbetaling.getTittel())
                .withBelop(tilIntegerMedAvrunding(String.valueOf(navUtbetaling.getNetto())))
                .withNetto(navUtbetaling.getNetto())
                .withBrutto(navUtbetaling.getBrutto())
                .withSkattetrekk(navUtbetaling.getSkattetrekk())
                .withOrganisasjon(mapToJsonOrganisasjon(navUtbetaling.getOrgnummer()))
                .withAndreTrekk(navUtbetaling.getAndreTrekk())
                .withPeriodeFom(navUtbetaling.getPeriodeFom() != null ? navUtbetaling.getPeriodeFom().toString() : null)
                .withPeriodeTom(navUtbetaling.getPeriodeTom() != null ? navUtbetaling.getPeriodeTom().toString() : null)
                .withUtbetalingsdato(navUtbetaling.getUtbetalingsdato() == null ? null : navUtbetaling.getUtbetalingsdato().toString())
                .withKomponenter(tilUtbetalingskomponentListe(navUtbetaling.getKomponenter()))
                .withOverstyrtAvBruker(false);
    }

    private List<JsonOkonomiOpplysningUtbetalingKomponent> tilUtbetalingskomponentListe(List<Komponent> komponenter) {
        if (komponenter != null) {
            return komponenter.stream().map(komponent ->
                    new JsonOkonomiOpplysningUtbetalingKomponent()
                            .withBelop(komponent.getBelop())
                            .withType(komponent.getType())
                            .withSatsBelop(komponent.getSatsBelop())
                            .withSatsType(komponent.getSatsType())
                            .withSatsAntall(komponent.getSatsAntall()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    static Integer tilIntegerMedAvrunding(String s) {
        Double d = tilDouble(s);
        if (d == null) {
            return null;
        }
        return (int) round(d);
    }

    private static Double tilDouble(String s) {
        if (isBlank(s)) {
            return null;
        }
        s = s.replace(",", ".");
        s = s.replace("\u00A0", "");
        return parseDouble(deleteWhitespace(s));
    }
}
