package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel;

import java.util.List;

public class BydelFordeling {

    private final String veiadresse;
    private final String gatekode;
    private final List<Husnummerfordeling> husnummerfordeling;
    private final String bydelFra;
    private final String bydelTil;
    private final String bydelsnavnTil;

    public BydelFordeling(
            String veiadresse,
            String gatekode,
            List<Husnummerfordeling> husnummerfordeling,
            String bydelFra,
            String bydelTil,
            String bydelsnavnTil
    ) {
        this.veiadresse = veiadresse;
        this.gatekode = gatekode;
        this.husnummerfordeling = husnummerfordeling;
        this.bydelFra = bydelFra;
        this.bydelTil = bydelTil;
        this.bydelsnavnTil = bydelsnavnTil;
    }

    public String getVeiadresse() {
        return veiadresse;
    }

    public String getGatekode() {
        return gatekode;
    }

    public List<Husnummerfordeling> getHusnummerfordeling() {
        return husnummerfordeling;
    }

    public String getBydelFra() {
        return bydelFra;
    }

    public String getBydelTil() {
        return bydelTil;
    }

    public String getBydelsnavnTil() {
        return bydelsnavnTil;
    }

    static class Husnummerfordeling {
        private final int fra;
        private final int til;
        private final HusnummerfordelingType type;

        public Husnummerfordeling(int fra, int til, HusnummerfordelingType type) {
            this.fra = fra;
            this.til = til;
            this.type = type;
        }

        public int getFra() {
            return fra;
        }

        public int getTil() {
            return til;
        }

        public HusnummerfordelingType getType() {
            return type;
        }
    }

    enum HusnummerfordelingType {
        ODD,
        EVEN,
        ALL
    }
}
