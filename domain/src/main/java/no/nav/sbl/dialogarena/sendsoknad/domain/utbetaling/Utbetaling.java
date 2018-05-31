package no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Utbetaling {
    public String type;
    public double netto;
    public double brutto;
    public double skatteTrekk;
    public double andreTrekk;

    public String bilagsNummer;
    public Periode periode;
    public LocalDate utbetalingsDato;
    public List<Komponent> komponenter = new ArrayList<>();

    public static class Periode {
        public LocalDate fom;
        public LocalDate tom;
    }

    public static class Komponent {
        public String type;
        public double belop;

        public String satsType;
        public double satsBelop;
        public double satsAntall;
    }
}
