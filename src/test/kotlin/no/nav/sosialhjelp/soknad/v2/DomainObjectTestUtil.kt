package no.nav.sosialhjelp.soknad.v2

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.VegAdresse
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Utdanning
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Integrasjonstatus
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.Tidspunkt
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

fun createJsonInternalSoknadWithInitializedSuperObjects(): JsonInternalSoknad {
    return JsonInternalSoknad().apply {
        soknad = JsonSoknad()
        vedlegg = JsonVedleggSpesifikasjon()
        mottaker = JsonSoknadsmottaker()
        midlertidigAdresse = JsonAdresse()
    }
}

fun createFamilie(
    soknadId: UUID,
    harForsorgerPlikt: Boolean? = true,
    barnebidrag: Barnebidrag? = Barnebidrag.BEGGE,
    sivilstatus: Sivilstatus? = Sivilstatus.GIFT,
    ansvar: List<Barn> = listOf(createBarn()),
    ektefelle: Ektefelle? = opprettEktefelle(),
) = Familie(soknadId, harForsorgerPlikt, barnebidrag, sivilstatus, ansvar.associateBy { it.familieKey }, ektefelle)

fun opprettEktefelle(): Ektefelle {
    return Ektefelle(
        navn = Navn("Kone", null, "Konesen"),
        fodselsdato = "432341",
        personId = "1234512345",
        folkeregistrertMedEktefelle = true,
        borSammen = true,
        kildeErSystem = true,
    )
}

fun opprettSoknad(
    id: UUID = UUID.randomUUID(),
    eierPersonId: String = "54352345353",
    opprettet: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    sistEndret: LocalDateTime? = null,
    sendtInn: LocalDateTime? = null,
    begrunnelse: Begrunnelse = opprettBegrunnelse(),
): Soknad {
    return Soknad(
        id = id,
        eierPersonId = eierPersonId,
        tidspunkt = Tidspunkt(opprettet, sistEndret, sendtInn),
        begrunnelse = begrunnelse,
    )
}

fun opprettArbeidsforholdList(
    arbeidsforholdList: List<Arbeidsforhold> =
        listOf(
            opprettArbeidsforhold(),
            opprettArbeidsforhold(arbeidsgivernavn = "Annen arbeidsgive", orgnummer = "0987654321"),
        ),
): List<Arbeidsforhold> {
    return arbeidsforholdList
}

fun opprettArbeidsforhold(
    arbeidsgivernavn: String = "Arbeidsgiversen",
    orgnummer: String? = "1234567890",
    start: String? = "01012010",
    slutt: String? = "01012020",
    fastStillingsprosent: Int? = 100,
    harFastStilling: Boolean? = true,
): Arbeidsforhold {
    return Arbeidsforhold(arbeidsgivernavn, orgnummer, start, slutt, fastStillingsprosent, harFastStilling)
}

fun opprettNavEnhet(
    enhetNr: String = "321321321",
    navn: String = "NAV-kontoret",
    kommunenummer: String = "4314",
    orgnummer: String = "3414513515",
    kommunenavn: String = "Nav-kommunen",
): NavEnhet {
    return NavEnhet(navn, enhetNr, kommunenummer, orgnummer, kommunenavn)
}

fun opprettEier(
    soknadId: UUID,
    statsborgerskap: String? = "Norsk",
    nordiskBoolean: Boolean? = true,
    kontonummer: Kontonummer = Kontonummer(false, null, "43234323432"),
    navn: Navn = opprettNavn(),
): Eier {
    return Eier(
        soknadId = soknadId,
        statsborgerskap = statsborgerskap,
        nordiskBorger = nordiskBoolean,
        navn = navn,
        kontonummer = kontonummer,
    )
}

fun opprettNavn(
    fornavn: String = "Test",
    mellomnavn: String = "Tester",
    etternavn: String = "Testesen",
): Navn {
    return Navn(
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
    )
}

fun createBarn(
    familieKey: UUID = UUID.randomUUID(),
    personId: String = "34243452342",
    navn: Navn = Navn(fornavn = "Navn", etternavn = "Navnesen"),
    fodselsdato: String = "342434",
    borSammen: Boolean = true,
    folkeregistrertSammen: Boolean = true,
    deltBosted: Boolean = false,
    samvarsgrad: Int = 100,
) = Barn(familieKey, personId, navn, fodselsdato, borSammen, folkeregistrertSammen, deltBosted, samvarsgrad)

fun opprettMidlertidigAdresse(
    adresselinjer: List<String> =
        listOf(
            "Bortiheiavegen 6",
            "2855 Poststed",
            "Norge",
        ),
): Adresse {
    return UstrukturertAdresse(adresse = adresselinjer)
}

fun opprettKontakt(
    soknadId: UUID,
    telefonnummer: Telefonnummer = Telefonnummer("98766554", "12345678"),
    adresser: Adresser =
        Adresser(
            folkeregistrertAdresse = opprettFolkeregistrertAdresse(),
            midlertidigAdresse = opprettMatrikkelAdresse(),
            brukerAdresse = opprettMidlertidigAdresse(),
            adressevalg = AdresseValg.FOLKEREGISTRERT,
        ),
    navEnhet: NavEnhet = opprettNavEnhet(),
): Kontakt {
    return Kontakt(soknadId, telefonnummer, adresser, navEnhet)
}

fun opprettAdresser(
    midlertidigAdresse: Adresse = opprettMidlertidigAdresse(),
    folkeregistrertAdresse: Adresse = opprettFolkeregistrertAdresse(),
    brukerAdresse: Adresse = opprettMatrikkelAdresse(),
): Adresser {
    return Adresser(
        adressevalg = AdresseValg.FOLKEREGISTRERT,
        midlertidigAdresse = midlertidigAdresse,
        folkeregistrertAdresse = folkeregistrertAdresse,
        brukerAdresse = brukerAdresse,
    )
}

fun opprettMatrikkelAdresse(
    kommunenummer: String = "5432",
    gaardsnummer: String = "231",
    bruksnummer: String = "31",
    festenummer: String? = null,
    seksjonsnummer: String? = null,
    undernummer: String? = null,
): MatrikkelAdresse {
    return MatrikkelAdresse(
        kommunenummer,
        gaardsnummer,
        bruksnummer,
        festenummer,
        seksjonsnummer,
        undernummer,
    )
}

fun opprettFolkeregistrertAdresse(
    landkode: String = "NO",
    kommunenummer: String? = "2944",
    adresselinjer: List<String> = listOf("Underetasjen", "Bak huset"),
    bolignummer: String? = "7",
    postnummer: String? = "2933",
    poststed: String? = "Poststedet",
    gatenavn: String? = "Vegadresseveien",
    husnummer: String? = "8",
    husbokstav: String? = "b",
): Adresse {
    return VegAdresse(
        landkode,
        kommunenummer,
        adresselinjer,
        bolignummer,
        postnummer,
        poststed,
        gatenavn,
        husnummer,
        husbokstav,
    )
}

fun opprettIntegrasjonstatus(
    soknadId: UUID,
    feilUtbetalingerNav: Boolean = false,
    feilInntektSkatteetaten: Boolean = false,
    feilStotteHusbanken: Boolean = false,
): Integrasjonstatus {
    return Integrasjonstatus(
        soknadId = soknadId,
        feilUtbetalingerNav = feilUtbetalingerNav,
        feilInntektSkatteetaten = feilInntektSkatteetaten,
        feilStotteHusbanken = feilStotteHusbanken,
    )
}

fun opprettLivssituasjon(
    soknadId: UUID,
    arbeid: Arbeid = opprettArbeid(),
    utdanning: Utdanning = opprettUtdanning(),
    bosituasjon: Bosituasjon = opprettBosituasjon(),
): Livssituasjon {
    return Livssituasjon(soknadId, arbeid, utdanning, bosituasjon)
}

fun opprettArbeid(
    kommentar: String? = "Jeg liker jobb",
    arbeidsforhold: List<Arbeidsforhold> = opprettArbeidsforholdList(),
): Arbeid {
    return Arbeid(kommentar, arbeidsforhold)
}

fun opprettUtdanning(
    erStudent: Boolean = true,
    studentGrad: Studentgrad = Studentgrad.HELTID,
): Utdanning {
    return Utdanning(erStudent, studentGrad)
}

fun opprettBegrunnelse(
    hvorforSoke: String = "Trenger penger",
    hvaSokesOm: String = "Jeg søker om penger",
): Begrunnelse {
    return Begrunnelse(hvorforSoke, hvaSokesOm)
}

fun opprettBosituasjon(
    botype: Botype = Botype.EIER,
    antallPersoner: Int = 3,
): Bosituasjon {
    return Bosituasjon(botype, antallPersoner)
}
