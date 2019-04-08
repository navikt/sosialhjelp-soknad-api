package no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Utbetaling {
    public String type;
    public double netto;
    public double brutto;
    public double skattetrekk;
    public double andreTrekk;

    public String bilagsnummer;
    public LocalDate utbetalingsdato;
    public LocalDate periodeFom;
    public LocalDate periodeTom;
    public List<Komponent> komponenter = new ArrayList<>();
    public String tittel;
    public String orgnummer;

    public static class Komponent {
        public String type;
        public double belop;

        public String satsType;
        public double satsBelop;
        public double satsAntall;
    }
}
