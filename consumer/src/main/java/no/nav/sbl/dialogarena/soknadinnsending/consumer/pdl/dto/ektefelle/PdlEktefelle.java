package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FoedselDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.NavnDto;

import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto.Gradering.UGRADERT;

public class PdlEktefelle {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

    private final List<FoedselDto> foedsel;

    private final List<NavnDto> navn;

    @JsonCreator
    public PdlEktefelle(
            @JsonProperty("adressebeskyttelse") List<AdressebeskyttelseDto> adressebeskyttelse,
            @JsonProperty("foedsel") List<FoedselDto> foedsel,
            @JsonProperty("navn") List<NavnDto> navn
    ) {
        this.adressebeskyttelse = adressebeskyttelse;
        this.foedsel = foedsel;
        this.navn = navn;
    }

    public List<AdressebeskyttelseDto> getAdressebeskyttelse() {
        return adressebeskyttelse;
    }

    public List<FoedselDto> getFoedsel() {
        return foedsel;
    }

    public List<NavnDto> getNavn() {
        return navn;
    }

    public boolean harAdressebeskyttelse() {
        return this.adressebeskyttelse != null
                && !this.adressebeskyttelse.isEmpty()
                && !this.adressebeskyttelse.stream().allMatch(adressebeskyttelseDto -> UGRADERT.equals(adressebeskyttelseDto.getGradering()));
    }
}
