package no.nav.sosialhjelp.soknad.oppslag.utbetaling;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class UtbetalingDto {

    private final String type;
    private final double netto;
    private final double brutto;
    private final double skattetrekk;
    private final double andreTrekk;
    private final String bilagsnummer;
    private final LocalDate utbetalingsdato;
    private final LocalDate periodeFom;
    private final LocalDate periodeTom;
    private final List<KomponentDto> komponenter;
    private final String tittel;
    private final String orgnummer;

    @JsonCreator
    public UtbetalingDto(
            @JsonProperty("type") String type,
            @JsonProperty("netto") double netto,
            @JsonProperty("brutto") double brutto,
            @JsonProperty("skattetrekk") double skattetrekk,
            @JsonProperty("andreTrekk") double andreTrekk,
            @JsonProperty("bilagsnummer") String bilagsnummer,
            @JsonProperty("utbetalingsdato") LocalDate utbetalingsdato,
            @JsonProperty("periodeFom") LocalDate periodeFom,
            @JsonProperty("periodeTom") LocalDate periodeTom,
            @JsonProperty("komponenter") List<KomponentDto> komponenter,
            @JsonProperty("tittel") String tittel,
            @JsonProperty("orgnummer") String orgnummer) {
        this.type = type;
        this.netto = netto;
        this.brutto = brutto;
        this.skattetrekk = skattetrekk;
        this.andreTrekk = andreTrekk;
        this.bilagsnummer = bilagsnummer;
        this.utbetalingsdato = utbetalingsdato;
        this.periodeFom = periodeFom;
        this.periodeTom = periodeTom;
        this.komponenter = komponenter;
        this.tittel = tittel;
        this.orgnummer = orgnummer;
    }

    public String getType() {
        return type;
    }

    public double getNetto() {
        return netto;
    }

    public double getBrutto() {
        return brutto;
    }

    public double getSkattetrekk() {
        return skattetrekk;
    }

    public double getAndreTrekk() {
        return andreTrekk;
    }

    public String getBilagsnummer() {
        return bilagsnummer;
    }

    public LocalDate getUtbetalingsdato() {
        return utbetalingsdato;
    }

    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }

    public List<KomponentDto> getKomponenter() {
        return komponenter;
    }

    public String getTittel() {
        return tittel;
    }

    public String getOrgnummer() {
        return orgnummer;
    }

    public static class KomponentDto {
        private final String type;
        private final double belop;
        private final String satsType;
        private final double satsBelop;
        private final double satsAntall;

        @JsonCreator
        public KomponentDto(
                @JsonProperty("type") String type,
                @JsonProperty("belop") double belop,
                @JsonProperty("satsType") String satsType,
                @JsonProperty("satsBelop") double satsBelop,
                @JsonProperty("satsAntall") double satsAntall
        ) {
            this.type = type;
            this.belop = belop;
            this.satsType = satsType;
            this.satsBelop = satsBelop;
            this.satsAntall = satsAntall;
        }

        public String getType() {
            return type;
        }

        public double getBelop() {
            return belop;
        }

        public String getSatsType() {
            return satsType;
        }

        public double getSatsBelop() {
            return satsBelop;
        }

        public double getSatsAntall() {
            return satsAntall;
        }
    }
}
