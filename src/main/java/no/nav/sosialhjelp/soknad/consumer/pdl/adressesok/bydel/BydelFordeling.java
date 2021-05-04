package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BydelFordeling {

    private final String veiadresse;
    private final String gatekode;
    private final List<Husnummerfordeling> husnummerfordeling;
    private final String bydelFra;
    private final String bydelTil;
    private final String bydelsnavnTil;

    @JsonCreator
    public BydelFordeling(
            @JsonProperty("veiadresse") String veiadresse,
            @JsonProperty("gatekode") String gatekode,
            @JsonProperty("husnummerfordeling") List<Husnummerfordeling> husnummerfordeling,
            @JsonProperty("bydelFra") String bydelFra,
            @JsonProperty("bydelTil") String bydelTil,
            @JsonProperty("bydelsnavnTil") String bydelsnavnTil
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

        @JsonCreator
        public Husnummerfordeling(
                @JsonProperty("fra") int fra,
                @JsonProperty("til") int til,
                @JsonProperty("type") HusnummerfordelingType type
        ) {
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
