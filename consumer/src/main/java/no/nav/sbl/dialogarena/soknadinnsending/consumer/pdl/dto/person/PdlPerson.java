package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import java.util.List;

public class PdlPerson {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

//    List<FamilierelasjonDto> familierelasjoner;

    private final List<FoedselDto> foedsel;

    private final List<NavnDto> navn;

    private final List<KjoennDto> kjoenn;

    private final List<SivilstandDto> sivilstand;

    private final List<StatsborgerskapDto> statsborgerskap;

//    private String fornavn;
//    private String mellomnavn;
//    private String etternavn;
//    private String sammensattNavn;
//    private LocalDate fodselsdato; // brukes ikke
//    private String fnr;
//    private String alder;
//    private String kjonn;
//    private String sivilstatus;
//    private String diskresjonskode;
//    private String statsborgerskap;
//    private Ektefelle ektefelle;


    public PdlPerson(List<AdressebeskyttelseDto> adressebeskyttelse, List<FoedselDto> foedsel, List<NavnDto> navn, List<KjoennDto> kjoenn, List<SivilstandDto> sivilstand, List<StatsborgerskapDto> statsborgerskap) {
        this.adressebeskyttelse = adressebeskyttelse;
        this.foedsel = foedsel;
        this.navn = navn;
        this.kjoenn = kjoenn;
        this.sivilstand = sivilstand;
        this.statsborgerskap = statsborgerskap;
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
