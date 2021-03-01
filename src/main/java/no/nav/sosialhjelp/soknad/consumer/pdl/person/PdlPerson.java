package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.BostedsadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.ForelderBarnRelasjonDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.KontaktadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.NavnDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.OppholdsadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.StatsborgerskapDto;

import java.util.List;

public class PdlPerson {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

    private final List<BostedsadresseDto> bostedsadresse;

    private final List<OppholdsadresseDto> oppholdsadresse;

    private final List<KontaktadresseDto> kontaktadresse;

    private final List<ForelderBarnRelasjonDto> forelderBarnRelasjon;

    private final List<NavnDto> navn;

    private final List<SivilstandDto> sivilstand;

    private final List<StatsborgerskapDto> statsborgerskap;

    @JsonCreator
    public PdlPerson(
            @JsonProperty("adressebeskyttelse") List<AdressebeskyttelseDto> adressebeskyttelse,
            @JsonProperty("bostedsadresse") List<BostedsadresseDto> bostedsadresse,
            @JsonProperty("oppholdsadresse") List<OppholdsadresseDto> oppholdsadresse,
            @JsonProperty("kontaktadresse") List<KontaktadresseDto> kontaktadresse,
            @JsonProperty("forelderBarnRelasjon") List<ForelderBarnRelasjonDto> forelderBarnRelasjon,
            @JsonProperty("navn") List<NavnDto> navn,
            @JsonProperty("sivilstand") List<SivilstandDto> sivilstand,
            @JsonProperty("statsborgerskap") List<StatsborgerskapDto> statsborgerskap
    ) {
        this.adressebeskyttelse = adressebeskyttelse;
        this.bostedsadresse = bostedsadresse;
        this.oppholdsadresse = oppholdsadresse;
        this.kontaktadresse = kontaktadresse;
        this.forelderBarnRelasjon = forelderBarnRelasjon;
        this.navn = navn;
        this.sivilstand = sivilstand;
        this.statsborgerskap = statsborgerskap;
    }

    public List<AdressebeskyttelseDto> getAdressebeskyttelse() {
        return adressebeskyttelse;
    }

    public List<BostedsadresseDto> getBostedsadresse() {
        return bostedsadresse;
    }

    public List<OppholdsadresseDto> getOppholdsadresse() {
        return oppholdsadresse;
    }

    public List<KontaktadresseDto> getKontaktadresse() {
        return kontaktadresse;
    }

    public List<ForelderBarnRelasjonDto> getForelderBarnRelasjon() {
        return forelderBarnRelasjon;
    }

    public List<NavnDto> getNavn() {
        return navn;
    }

    public List<SivilstandDto> getSivilstand() {
        return sivilstand;
    }

    public List<StatsborgerskapDto> getStatsborgerskap() {
        return statsborgerskap;
    }

}
