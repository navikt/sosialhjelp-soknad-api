package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.BostedsadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FamilierelasjonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.KontaktadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.OppholdsadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.StatsborgerskapDto;

import java.util.List;

public class PdlPerson {

    private final List<AdressebeskyttelseDto> adressebeskyttelse;

    private final List<BostedsadresseDto> bostedsadresse;

    private final List<OppholdsadresseDto> oppholdsadresse;

    private final List<KontaktadresseDto> kontaktadresse;

    private final List<FamilierelasjonDto> familierelasjoner;

    private final List<NavnDto> navn;

    private final List<SivilstandDto> sivilstand;

    private final List<StatsborgerskapDto> statsborgerskap;

    @JsonCreator
    public PdlPerson(
            @JsonProperty("adressebeskyttelse") List<AdressebeskyttelseDto> adressebeskyttelse,
            @JsonProperty("bostedsadresse") List<BostedsadresseDto> bostedsadresse,
            @JsonProperty("oppholdsadresse") List<OppholdsadresseDto> oppholdsadresse,
            @JsonProperty("kontaktadresse") List<KontaktadresseDto> kontaktadresse,
            @JsonProperty("familierelasjoner") List<FamilierelasjonDto> familierelasjoner,
            @JsonProperty("navn") List<NavnDto> navn,
            @JsonProperty("sivilstand") List<SivilstandDto> sivilstand,
            @JsonProperty("statsborgerskap") List<StatsborgerskapDto> statsborgerskap
    ) {
        this.adressebeskyttelse = adressebeskyttelse;
        this.bostedsadresse = bostedsadresse;
        this.oppholdsadresse = oppholdsadresse;
        this.kontaktadresse = kontaktadresse;
        this.familierelasjoner = familierelasjoner;
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

    public List<FamilierelasjonDto> getFamilierelasjoner() {
        return familierelasjoner;
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
