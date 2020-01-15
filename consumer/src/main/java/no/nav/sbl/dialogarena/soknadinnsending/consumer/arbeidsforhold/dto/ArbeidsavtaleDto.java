package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArbeidsavtaleDto {

    private double antallTimerPrUke;
    private String arbeidstidsordning;
    private double beregnetAntallTimerPrUke;
    private BruksperiodeDto bruksperiode;
    private GyldighetsperiodeDto gyldighetsperiode;
    private String sistLoennsendring;
    private String sistStillingsendring;
//    private SporingsinformasjonDto sporingsinformasjon;
    private double stillingsprosent;
    private String yrke;

    public ArbeidsavtaleDto(double antallTimerPrUke, String arbeidstidsordning, double beregnetAntallTimerPrUke, BruksperiodeDto bruksperiode, GyldighetsperiodeDto gyldighetsperiode, String sistLoennsendring, String sistStillingsendring, double stillingsprosent, String yrke) {
        this.antallTimerPrUke = antallTimerPrUke;
        this.arbeidstidsordning = arbeidstidsordning;
        this.beregnetAntallTimerPrUke = beregnetAntallTimerPrUke;
        this.bruksperiode = bruksperiode;
        this.gyldighetsperiode = gyldighetsperiode;
        this.sistLoennsendring = sistLoennsendring;
        this.sistStillingsendring = sistStillingsendring;
        this.stillingsprosent = stillingsprosent;
        this.yrke = yrke;
    }

    public double getAntallTimerPrUke() {
        return antallTimerPrUke;
    }

    public String getArbeidstidsordning() {
        return arbeidstidsordning;
    }

    public double getBeregnetAntallTimerPrUke() {
        return beregnetAntallTimerPrUke;
    }

    public BruksperiodeDto getBruksperiode() {
        return bruksperiode;
    }

    public GyldighetsperiodeDto getGyldighetsperiode() {
        return gyldighetsperiode;
    }

    public String getSistLoennsendring() {
        return sistLoennsendring;
    }

    public String getSistStillingsendring() {
        return sistStillingsendring;
    }

    public double getStillingsprosent() {
        return stillingsprosent;
    }

    public String getYrke() {
        return yrke;
    }
}
