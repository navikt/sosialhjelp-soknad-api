//package no.nav.sosialhjelp.soknad.consumer.kodeverk.dto;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import java.time.LocalDate;
//import java.util.Map;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class BetydningDto {
//
//    public static final String SPRAAKKODE_NB = "nb";
//
//    private final LocalDate gyldigFra;
//    private final LocalDate gyldigTil;
//    private final Map<String, BeskrivelseDto> beskrivelser;
//
//    @JsonCreator
//    public BetydningDto(
//            @JsonProperty("gyldigFra") LocalDate gyldigFra,
//            @JsonProperty("gyldigTil") LocalDate gyldigTil,
//            @JsonProperty("beskrivelser") Map<String, BeskrivelseDto> beskrivelser
//    ) {
//        this.gyldigFra = gyldigFra;
//        this.gyldigTil = gyldigTil;
//        this.beskrivelser = beskrivelser;
//    }
//
//    public LocalDate getGyldigFra() {
//        return gyldigFra;
//    }
//
//    public LocalDate getGyldigTil() {
//        return gyldigTil;
//    }
//
//    public Map<String, BeskrivelseDto> getBeskrivelser() {
//        return beskrivelser;
//    }
//}
