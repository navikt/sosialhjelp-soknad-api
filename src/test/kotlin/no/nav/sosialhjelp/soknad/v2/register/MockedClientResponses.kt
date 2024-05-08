package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.arbeid.dto.AnsettelsesperiodeDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsavtaleDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.arbeid.dto.OrganisasjonDto
import no.nav.sosialhjelp.soknad.arbeid.dto.PeriodeDto
import no.nav.sosialhjelp.soknad.arbeid.dto.PersonArbeidDto
import no.nav.sosialhjelp.soknad.organisasjon.dto.NavnDto
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkelNummer
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.BostedsadresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FoedselDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.ForelderBarnRelasjonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import no.nav.sosialhjelp.soknad.personalia.person.dto.StatsborgerskapDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.barn1Fnr
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.barn2Fnr
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.ektefelleFnr
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer1
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer2
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.vegadresseDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

object DefaultValuesForMockedResponses {
    val orgnummer1 = "123456789"
    val orgnummer2 = "987654321"
    val ektefelleFnr = "66666666666"
    val barn1Fnr = "12345612345"
    val barn2Fnr = "98765498765"
    val vegadresseDto =
        VegadresseDto(
            matrikkelId = null,
            adressenavn = "Herborjegveien",
            husnummer = 4,
            husbokstav = null,
            tilleggsnavn = null,
            postnummer = "2750",
            kommunenummer = "0534",
            bruksenhetsnummer = null,
            bydelsnummer = null,
        )
    val matrikkeladresseDto =
        MatrikkeladresseDto(
            matrikkelId = "042450",
            postnummer = "2750",
            tilleggsnavn = null,
            kommunenummer = "2560",
            bruksenhetsnummer = null,
        )
    val kontoDto = KontoDto(kontonummer = "12341212345", utenlandskKontoInfo = null)
}

internal fun defaultResponseFromAaregClient(personId: String): List<ArbeidsforholdDto> {
    return listOf(
        ArbeidsforholdDto(
            ansettelsesperiode =
                AnsettelsesperiodeDto(
                    periode =
                        PeriodeDto(
                            fom = LocalDate.of(2000, 1, 1),
                            tom = LocalDate.of(2009, 12, 31),
                        ),
                ),
            arbeidsavtaler =
                listOf(
                    ArbeidsavtaleDto(stillingsprosent = 100.00),
                ),
            arbeidsforholdId = UUID.randomUUID().toString(),
            arbeidsgiver = OrganisasjonDto(organisasjonsnummer = orgnummer1, type = "fast"),
            arbeidstaker =
                PersonArbeidDto(
                    offentligIdent = personId,
                    aktoerId = UUID.randomUUID().toString(),
                    type = "ansatt",
                ),
        ),
        ArbeidsforholdDto(
            ansettelsesperiode =
                AnsettelsesperiodeDto(
                    periode =
                        PeriodeDto(
                            fom = LocalDate.of(2010, 1, 1),
                            tom = LocalDate.of(2019, 12, 31),
                        ),
                ),
            arbeidsavtaler =
                listOf(
                    ArbeidsavtaleDto(stillingsprosent = 100.00),
                ),
            arbeidsforholdId = UUID.randomUUID().toString(),
            arbeidsgiver = OrganisasjonDto(organisasjonsnummer = orgnummer2, type = "fast"),
            arbeidstaker =
                PersonArbeidDto(
                    offentligIdent = personId,
                    aktoerId = UUID.randomUUID().toString(),
                    type = "ansatt",
                ),
        ),
    )
}

fun defaultResponseFromOrganisasjonClient(orgnummer: String): OrganisasjonNoekkelinfoDto {
    return OrganisasjonNoekkelinfoDto(
        navn =
            NavnDto(
                navnelinje1 = "Et fantastisk firma:${Math.random()}",
                null,
                null,
                null,
                null,
            ),
        organisasjonsnummer = orgnummer,
    )
}

fun defaultResponseFromHentPerson(
    sivilstandDto: List<SivilstandDto>? = defaultSivilstandList(),
    vegAdresseDto: VegadresseDto? = vegadresseDto,
    matrikkeladresseDto: MatrikkeladresseDto? = null,
): PersonDto {
    return PersonDto(
        bostedsadresse =
            listOf(
                BostedsadresseDto(
                    coAdressenavn = null,
                    vegadresse = vegAdresseDto,
                    matrikkeladresse = matrikkeladresseDto,
                    ukjentBosted = null,
                ),
            ),
        oppholdsadresse = null,
        forelderBarnRelasjon = null,
        navn =
            listOf(
                no.nav.sosialhjelp.soknad.personalia.person.dto.NavnDto(
                    fornavn = "Et fornavn",
                    mellomnavn = null,
                    etternavn = "Et etternavn",
                    metadata =
                        MetadataDto(
                            master = "PDL",
                            endringer =
                                listOf(
                                    EndringDto(
                                        kilde = "PDL",
                                        registrert = LocalDateTime.of(2000, 5, 4, 12, 0, 0),
                                        type = "",
                                    ),
                                ),
                        ),
                    folkeregistermetadata = null,
                ),
            ),
        sivilstand = sivilstandDto,
        statsborgerskap =
            listOf(
                StatsborgerskapDto("NOR"),
                StatsborgerskapDto("DNK"),
            ),
    )
}

fun defaultSivilstandList() =
    listOf(
        SivilstandDto(
            type = SivilstandType.GIFT,
            relatertVedSivilstand = ektefelleFnr,
            metadata =
                MetadataDto(
                    master = "FREG",
                    endringer =
                        listOf(
                            EndringDto(
                                kilde = "PDL",
                                registrert = LocalDateTime.of(2010, 4, 5, 0, 0, 0),
                                type = "",
                            ),
                        ),
                ),
            folkeregistermetadata = null,
        ),
    )

fun defaultResponseHentPersonWithEktefelleOgBarn(): PersonDto {
    return defaultResponseFromHentPerson()
        .copy(
            forelderBarnRelasjon =
                listOf(
                    ForelderBarnRelasjonDto(
                        relatertPersonsIdent = barn1Fnr,
                        relatertPersonsRolle = "BARN",
                        minRolleForPerson = "FAR",
                    ),
                    ForelderBarnRelasjonDto(
                        relatertPersonsIdent = barn2Fnr,
                        relatertPersonsRolle = "BARN",
                        minRolleForPerson = "FAR",
                    ),
                ),
        )
}

fun defaultResponseFromHentEktefelle(
    fnr: String,
    vegAdresse: VegadresseDto? = null,
): EktefelleDto {
    return EktefelleDto(
        adressebeskyttelse = null,
        bostedsadresse =
            listOf(
                BostedsadresseDto(
                    coAdressenavn = null,
                    vegadresse = vegAdresse,
                    matrikkeladresse = null,
                    ukjentBosted = null,
                ),
            ),
        foedsel =
            listOf(
                FoedselDto(foedselsdato = LocalDate.of(1999, 8, 21)),
            ),
        navn =
            listOf(
                no.nav.sosialhjelp.soknad.personalia.person.dto.NavnDto(
                    fornavn = "Ektefelle",
                    mellomnavn = null,
                    etternavn = "Ektefellesen",
                    metadata =
                        MetadataDto(
                            master = "PDL",
                            endringer =
                                listOf(
                                    EndringDto(
                                        kilde = "PDL",
                                        registrert = LocalDateTime.of(2010, 4, 5, 0, 0, 0),
                                        type = "",
                                    ),
                                ),
                        ),
                    folkeregistermetadata = null,
                ),
            ),
    )
}

fun defaultResponseFromHentMatrikkelAdresse(): no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkeladresseDto {
    return no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkeladresseDto(
        undernummer = null,
        matrikkelnummer =
            MatrikkelNummer(
                kommunenummer = "3215",
                gaardsnummer = "04",
                bruksnummer = "121",
                festenummer = null,
                seksjonsnummer = null,
            ),
        bydel = null,
    )
}

fun defaultResponseFromHentBarn(
    fnr: String,
    vegAdresseDto: VegadresseDto = vegadresseDto,
    year: Int,
): BarnDto {
    return BarnDto(
        adressebeskyttelse = null,
        bostedsadresse = listOf(BostedsadresseDto(null, vegAdresseDto, null, null)),
        folkeregisterpersonstatus = null,
        foedsel = listOf(FoedselDto(foedselsdato = LocalDate.of(year, 5, 12))),
        navn =
            listOf(
                no.nav.sosialhjelp.soknad.personalia.person.dto.NavnDto(
                    fornavn = "Barn $fnr",
                    mellomnavn = null,
                    etternavn = "Barnetternavn $fnr",
                    metadata =
                        MetadataDto(
                            master = "PDL",
                            endringer =
                                listOf(
                                    EndringDto(
                                        kilde = "PDL",
                                        registrert = LocalDateTime.of(2006, 5, 15, 0, 0, 0),
                                        type = "",
                                    ),
                                ),
                        ),
                    folkeregistermetadata = null,
                ),
            ),
    )
}
