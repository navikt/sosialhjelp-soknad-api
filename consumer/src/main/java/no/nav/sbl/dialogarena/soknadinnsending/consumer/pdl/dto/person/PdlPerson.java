package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import java.util.List;

public class PdlPerson {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

    private final List<FamilierelasjonDto> familierelasjoner;

    private final List<FoedselDto> foedsel;

    private final List<NavnDto> navn;

    private final List<KjoennDto> kjoenn;

    private final List<SivilstandDto> sivilstand;

    private final List<StatsborgerskapDto> statsborgerskap;

    public PdlPerson(List<AdressebeskyttelseDto> adressebeskyttelse, List<FamilierelasjonDto> familierelasjoner, List<FoedselDto> foedsel, List<NavnDto> navn, List<KjoennDto> kjoenn, List<SivilstandDto> sivilstand, List<StatsborgerskapDto> statsborgerskap) {
        this.adressebeskyttelse = adressebeskyttelse;
        this.familierelasjoner = familierelasjoner;
        this.foedsel = foedsel;
        this.navn = navn;
        this.kjoenn = kjoenn;
        this.sivilstand = sivilstand;
        this.statsborgerskap = statsborgerskap;
    }

    public List<AdressebeskyttelseDto> getAdressebeskyttelse() {
        return adressebeskyttelse;
    }

    public List<FamilierelasjonDto> getFamilierelasjoner() {
        return familierelasjoner;
    }

    public List<FoedselDto> getFoedsel() {
        return foedsel;
    }

    public List<NavnDto> getNavn() {
        return navn;
    }

    public List<KjoennDto> getKjoenn() {
        return kjoenn;
    }

    public List<SivilstandDto> getSivilstand() {
        return sivilstand;
    }

    public List<StatsborgerskapDto> getStatsborgerskap() {
        return statsborgerskap;
    }
}
