package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.arbeid.dto.AnsettelsesdetaljerDtoV2
import no.nav.sosialhjelp.soknad.arbeid.dto.AnsettelsesformDto
import no.nav.sosialhjelp.soknad.arbeid.dto.AnsettelsesperiodeDtoV2
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDtoV2
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsstedDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsstedType
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidstakerDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidstakerIdentDto
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidstakerIdentType
import no.nav.sosialhjelp.soknad.arbeid.dto.IdentInfoDto
import no.nav.sosialhjelp.soknad.arbeid.dto.IdentInfoType
import no.nav.sosialhjelp.soknad.arbeid.dto.OpplysningspliktigDto
import no.nav.sosialhjelp.soknad.arbeid.dto.OpplysningspliktigType
import no.nav.sosialhjelp.soknad.arbeid.dto.RapporteringsmaanederDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenResponse
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.SakDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.UtbetalingDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.VedtakDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteMottaker
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteRolle
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteStatus
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Aktoer
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Periode
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Utbetaling
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Ytelse
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Ytelseskomponent
import no.nav.sosialhjelp.soknad.organisasjon.dto.NavnDto
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkelNummer
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.BostedsadresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FoedselsdatoDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.ForelderBarnRelasjonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import no.nav.sosialhjelp.soknad.personalia.person.dto.StatsborgerskapDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.barn1Fnr
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.barn2Fnr
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.ektefelleFnr
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer1
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer2
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.vegadresseDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling as UtbetalingSkatteetaten

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

internal fun defaultResponseFromAaregClient(personId: String): List<ArbeidsforholdDtoV2> {
    return listOf(
        ArbeidsforholdDto(
            ansettelsesperiode =
                AnsettelsesperiodeDto(
                    periode =
                        PeriodeDto(
                            fom = LocalDate.now().minusYears(2),
                            tom = LocalDate.now(),
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
                            fom = LocalDate.now().minusYears(8),
                            tom = null,
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

internal fun defaultResponseFromAaregClientV2(personId: String): List<ArbeidsforholdDtoV2> {
    return listOf(
        ArbeidsforholdDtoV2(
            id = UUID.randomUUID().toString(),
            ansettelsesperiode =
                AnsettelsesperiodeDtoV2(
                    startdato = LocalDate.now().minusMonths(2),
                    sluttdato = null,
                ),
            ansettelsesdetaljer =
                listOf(
                    AnsettelsesdetaljerDtoV2(
                        avtaltStillingsprosent = 100.00,
                        ansettelsesform =
                            AnsettelsesformDto(
                                kode = "fast",
                                beskrivelse = "Fast ansatt",
                            ),
                        rapporteringsmaaneder =
                            RapporteringsmaanederDto(
                                fra = "2024-01",
                                til = null,
                            ),
                    ),
                ),
            opplysningspliktig =
                OpplysningspliktigDto(
                    type = OpplysningspliktigType.Hovedenhet,
                    identer =
                        listOf(
                            IdentInfoDto(
                                type = IdentInfoType.ORGANISASJONSNUMMER,
                                ident = orgnummer1,
                                gjeldende = true,
                            ),
                        ),
                ),
            arbeidstaker =
                ArbeidstakerDto(
                    identer =
                        listOf(
                            ArbeidstakerIdentDto(
                                type = ArbeidstakerIdentType.FOLKEREGISTERIDENT,
                                ident = personId,
                                gjeldende = true,
                            ),
                        ),
                ),
            arbeidssted =
                ArbeidsstedDto(
                    type = ArbeidsstedType.Underenhet,
                    identer =
                        listOf(
                            IdentInfoDto(
                                type = IdentInfoType.ORGANISASJONSNUMMER,
                                ident = orgnummer1,
                                gjeldende = true,
                            ),
                        ),
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
        foedselsdato = listOf(FoedselsdatoDto(foedselsdato = LocalDate.of(1990, 1, 1), 1990)),
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
        foedselsdato =
            listOf(
                FoedselsdatoDto(foedselsdato = LocalDate.of(1999, 8, 21), 1999),
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
    offsetYear: Int,
): BarnDto {
    val yearOfBirth = LocalDate.now().year.minus(offsetYear)
    return BarnDto(
        adressebeskyttelse = null,
        bostedsadresse = listOf(BostedsadresseDto(null, vegAdresseDto, null, null)),
        folkeregisterpersonstatus = null,
        foedselsdato = listOf(FoedselsdatoDto(foedselsdato = LocalDate.of(yearOfBirth, 5, 12), 1970)),
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

fun defaultResponseFromNavUtbetalingerClient(): UtbetalDataDto {
    val navn = "Navn Navnesen"
    return UtbetalDataDto(
        utbetalinger =
            listOf(
                Utbetaling(
                    posteringsdato = null,
                    utbetaltTil = Aktoer("PERSON", "123456123451234", navn),
                    utbetalingNettobeloep = null,
                    utbetalingsdato = LocalDate.now().minusDays(10),
                    forfallsdato = null,
                    utbetalingsmelding = null,
                    utbetaltTilKonto = null,
                    utbetalingsmetode = null,
                    utbetalingsstatus = null,
                    ytelseListe =
                        listOf(
                            Ytelse(
                                ytelsestype = "Utbetaling 1 Ytelse 1",
                                ytelsesperiode = Periode(LocalDate.now().minusMonths(2), LocalDate.now()),
                                ytelseNettobeloep = BigDecimal(5000.0),
                                rettighetshaver = Aktoer("PERSON", "1234", navn),
                                skattsum = BigDecimal(1300.0),
                                trekksum = BigDecimal(600.0),
                                ytelseskomponentersum = BigDecimal(2500.0),
                                skattListe = null,
                                trekkListe = null,
                                bilagsnummer = "13123421",
                                refundertForOrg = null,
                                ytelseskomponentListe =
                                    listOf(
                                        Ytelseskomponent(
                                            ytelseskomponenttype = "Utbetaling 1 Ytelse 1 Komponent 1",
                                            satsbeloep = BigDecimal(852.0),
                                            satstype = "Timelønn",
                                            satsantall = 2.0,
                                            ytelseskomponentbeloep = BigDecimal(555.0),
                                        ),
                                        Ytelseskomponent(
                                            ytelseskomponenttype = "Utbetaling 1 Ytelse 1 Komponent 2",
                                            satsbeloep = BigDecimal(666.0),
                                            satstype = "Timelønn",
                                            satsantall = 2.0,
                                            ytelseskomponentbeloep = BigDecimal(333.0),
                                        ),
                                    ),
                            ),
                            Ytelse(
                                ytelsestype = "Utbetaling 1 Ytelse 2",
                                ytelsesperiode = Periode(LocalDate.now().minusMonths(4), LocalDate.now().minusMonths(2)),
                                ytelseNettobeloep = BigDecimal(5000.0),
                                rettighetshaver = Aktoer("PERSON", "1234", navn),
                                skattsum = BigDecimal(1200.0),
                                trekksum = BigDecimal(300.0),
                                ytelseskomponentersum = BigDecimal(2400.0),
                                skattListe = null,
                                trekkListe = null,
                                bilagsnummer = "1312342112",
                                refundertForOrg = null,
                                ytelseskomponentListe =
                                    listOf(
                                        Ytelseskomponent(
                                            ytelseskomponenttype = "Utbetaling 1 Ytelse 2 Komponent 1",
                                            satsbeloep = BigDecimal(812.0),
                                            satstype = "Timelønn",
                                            satsantall = 3.0,
                                            ytelseskomponentbeloep = BigDecimal(555.0),
                                        ),
                                    ),
                            ),
                        ),
                ),
                Utbetaling(
                    posteringsdato = null,
                    utbetaltTil = Aktoer("PERSON", "31212152", navn),
                    utbetalingNettobeloep = null,
                    utbetalingsdato = LocalDate.now().minusDays(10),
                    forfallsdato = null,
                    utbetalingsmelding = null,
                    utbetaltTilKonto = null,
                    utbetalingsmetode = null,
                    utbetalingsstatus = null,
                    ytelseListe =
                        listOf(
                            Ytelse(
                                ytelsestype = "Utbetaling 2 Ytelse 1",
                                ytelsesperiode = Periode(LocalDate.now().minusMonths(2), LocalDate.now()),
                                ytelseNettobeloep = BigDecimal(5000.0),
                                rettighetshaver = Aktoer("PERSON", "1234", navn),
                                skattsum = BigDecimal(1300.0),
                                trekksum = BigDecimal(600.0),
                                ytelseskomponentersum = BigDecimal(2500.0),
                                skattListe = null,
                                trekkListe = null,
                                bilagsnummer = "13123421",
                                refundertForOrg = null,
                                ytelseskomponentListe =
                                    listOf(
                                        Ytelseskomponent(
                                            ytelseskomponenttype = "Utbetaling 2 Ytelse 1 Komponent 1",
                                            satsbeloep = BigDecimal(852.0),
                                            satstype = "Timelønn",
                                            satsantall = 2.0,
                                            ytelseskomponentbeloep = BigDecimal(555.0),
                                        ),
                                    ),
                            ),
                            Ytelse(
                                ytelsestype = "Utbetaling 2 Ytelse 2",
                                ytelsesperiode = Periode(LocalDate.now().minusMonths(4), LocalDate.now().minusMonths(2)),
                                ytelseNettobeloep = BigDecimal(5000.0),
                                rettighetshaver = Aktoer("PERSON", "1234", navn),
                                skattsum = BigDecimal(1200.0),
                                trekksum = BigDecimal(300.0),
                                ytelseskomponentersum = BigDecimal(2400.0),
                                skattListe = null,
                                trekkListe = null,
                                bilagsnummer = "1312342112",
                                refundertForOrg = null,
                                ytelseskomponentListe =
                                    listOf(
                                        Ytelseskomponent(
                                            ytelseskomponenttype = "Utbetaling 2 Ytelse 1 Komponent 1",
                                            satsbeloep = BigDecimal(812.0),
                                            satstype = "Timelønn",
                                            satsantall = 3.0,
                                            ytelseskomponentbeloep = BigDecimal(555.0),
                                        ),
                                    ),
                            ),
                        ),
                ),
            ),
        feilet = false,
    )
}

fun defaultResponseForSkattbarInntektService(): List<UtbetalingSkatteetaten> {
    return listOf(
        UtbetalingSkatteetaten(
            type = "Lønn fra arbeidsgiver 1",
            brutto = 5000.0,
            skattetrekk = 1333.0,
            periodeFom = LocalDate.now().minusMonths(1),
            periodeTom = LocalDate.now(),
            tittel = "Lønn",
            orgnummer = "12345123",
        ),
        UtbetalingSkatteetaten(
            type = "Lønn fra arbeidsgiver 2",
            brutto = 6000.0,
            skattetrekk = 1633.0,
            periodeFom = LocalDate.now().minusMonths(1),
            periodeTom = LocalDate.now(),
            tittel = "Lønn",
            orgnummer = "98765432",
        ),
    )
}

fun defaultResponseForHusbankenClient(): HusbankenResponse.Success {
    return HusbankenResponse.Success(
        BostotteDto(
            saker =
                listOf(
                    SakDto(
                        mnd = LocalDate.now().month.value,
                        ar = LocalDate.now().year,
                        status = BostotteStatus.UNDER_BEHANDLING,
                        rolle = BostotteRolle.HOVEDPERSON,
                        vedtak =
                            VedtakDto(
                                kode = "Kode for Vedtak",
                                beskrivelse = "beskrivelse om vedtak",
                                type = Vedtaksstatus.INNVILGET.name,
                            ),
                    ),
                    SakDto(
                        mnd = LocalDate.now().month.value,
                        ar = LocalDate.now().year,
                        status = BostotteStatus.VEDTATT,
                        rolle = BostotteRolle.HOVEDPERSON,
                        vedtak =
                            VedtakDto(
                                kode = "En annen kode for vedtak",
                                beskrivelse = "En annen beskrivelse om vedtak",
                                type = Vedtaksstatus.AVVIST.name,
                            ),
                    ),
                ),
            utbetalinger =
                listOf(
                    UtbetalingDto(
                        utbetalingsdato = LocalDate.now(),
                        belop = BigDecimal(5000.0),
                        mottaker = BostotteMottaker.HUSSTAND,
                        rolle = BostotteRolle.HOVEDPERSON,
                    ),
                    UtbetalingDto(
                        utbetalingsdato = LocalDate.now(),
                        belop = BigDecimal(6000.0),
                        mottaker = BostotteMottaker.HUSSTAND,
                        rolle = BostotteRolle.HOVEDPERSON,
                    ),
                ),
        ),
    )
}
