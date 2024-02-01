package no.nav.sosialhjelp.soknad.v2

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.v2.adresse.Adresse
import no.nav.sosialhjelp.soknad.v2.adresse.AdresserSoknad
import no.nav.sosialhjelp.soknad.v2.adresse.BrukerInputAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.adresse.VegAdresse
import no.nav.sosialhjelp.soknad.v2.brukerdata.AdresseValg
import no.nav.sosialhjelp.soknad.v2.brukerdata.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.brukerdata.BeskrivelseAvAnnet
import no.nav.sosialhjelp.soknad.v2.brukerdata.Brukerdata
import no.nav.sosialhjelp.soknad.v2.brukerdata.KontoInformasjonBruker
import no.nav.sosialhjelp.soknad.v2.brukerdata.Samtykke
import no.nav.sosialhjelp.soknad.v2.brukerdata.SamtykkeType
import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.NavEnhet
import no.nav.sosialhjelp.soknad.v2.soknad.Navn
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

fun createJsonInternalSoknadWithInitializedSuperObjects(): JsonInternalSoknad {
    return JsonInternalSoknad().apply {
        soknad = JsonSoknad()
        vedlegg = JsonVedleggSpesifikasjon()
        mottaker = JsonSoknadsmottaker()
        midlertidigAdresse = JsonAdresse()
    }
}

fun createSoknad(
    id: UUID? = null,
    eier: Eier = createEier(),
    innsendingstidspunkt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
): Soknad {
    return Soknad(
        id = id,
        eier = eier,
        innsendingstidspunkt = innsendingstidspunkt,
//        navEnhet = opprettNavEnhet()
    )
}

fun opprettNavEnhet(
    enhetNr: String = "321321321",
    navn: String = "NAV-kontoret",
    kommunenummer: String = "4314",
    orgnummer: String = "3414513515"
): NavEnhet {
    return NavEnhet(enhetNr, navn, kommunenummer, orgnummer)
}

fun createEier(
    personId: String = "12345612345",
    statsborgerskap: String = "Norsk",
    nordiskBoolean: Boolean = true,
    kontonummer: String = "12341212345",
    navn: Navn = createNavn(),
    telefonnummer: String = "1234567123"
): Eier {
    return Eier(
        personId = personId,
        statsborgerskap = statsborgerskap,
        nordiskBorger = nordiskBoolean,
        kontonummer = kontonummer,
        navn = navn,
        telefonnummer = telefonnummer
    )
}

fun createNavn(
    fornavn: String = "Test",
    mellomnavn: String = "Tester",
    etternavn: String = "Testesen"
): Navn {
    return Navn(
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}

fun opprettMidlertidigAdresse(
    adresselinjer: List<String> = listOf(
        "Bortiheiavegen 6",
        "2855 Poststed",
        "Norge"
    )
): Adresse {
    return UstrukturertAdresse(adresse = adresselinjer)
}

fun createSoknadNoId(
    eier: Eier = opprettEier(),
    tidspunkt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
): Soknad {
    return Soknad(eier = eier, innsendingstidspunkt = tidspunkt)
}

fun opprettEier(
    personId: String = "1234567890",
    navn: Navn = opprettNavn(),
    statsborgerskap: String? = "Norsk",
    nordiskBorger: Boolean? = true,
    telefonnummer: String? = "94342312",
    kontonummer: String? = "12341212345"
): Eier {
    return Eier(
        personId = personId,
        navn = navn,
        statsborgerskap = statsborgerskap,
        nordiskBorger = nordiskBorger,
        telefonnummer = telefonnummer,
        kontonummer = kontonummer,
    )
}

fun opprettAdresserSoknad(
    soknadId: UUID,
    midlertidigAdresse: Adresse = opprettMidlertidigAdresse(),
    folkeregistrertAdresse: Adresse = opprettFolkeregistrertAdresse(),
    brukerInput: BrukerInputAdresse = opprettBrukerInputAdresse()
): AdresserSoknad {
    return AdresserSoknad(
        soknadId = soknadId,
        midlertidigAdresse = midlertidigAdresse,
        folkeregistrertAdresse = folkeregistrertAdresse,
        brukerInput = brukerInput
    )
}

fun opprettBrukerInputAdresse(
    valgtAdresse: AdresseValg = AdresseValg.FOLKEREGISTRERT,
    adresseBruker: Adresse = opprettMatrikkelAdresse()
): BrukerInputAdresse {
    return BrukerInputAdresse(
        valgtAdresse = valgtAdresse,
        brukerAdresse = adresseBruker
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
        kommunenummer, gaardsnummer, bruksnummer, festenummer, seksjonsnummer, undernummer
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
    husbokstav: String? = "b"
): Adresse {
    return VegAdresse(
        landkode, kommunenummer, adresselinjer, bolignummer,
        postnummer, poststed, gatenavn, husnummer, husbokstav
    )
}

fun opprettNavn(
    fornavn: String = "Test",
    mellomnavn: String? = "Tester",
    etternavn: String = "Testesen",
): Navn {
    return Navn(
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}

fun opprettBrukerdata(
    soknadId: UUID,
    telefonnummer: String? = "91423231",
    kommentarTilArbeidsforhold: String = "Jeg er glad i jobben min",
    kontoInformasjon: KontoInformasjonBruker = KontoInformasjonBruker(
        kontonummer = "1234567890",
        harIkkeKonto = null,
    ),
    begrunnelse: Begrunnelse = opprettBegrunnelse(),
    samtykker: Set<Samtykke> = opprettSamtykker(),
    beskrivelseAvAnnet: BeskrivelseAvAnnet = opprettBeskrivelseAvAnnet(),
): Brukerdata {
    return Brukerdata(
        soknadId, begrunnelse, kontoInformasjon, kommentarTilArbeidsforhold,
        telefonnummer, samtykker, beskrivelseAvAnnet
    )
}

fun opprettBegrunnelse(
    hvorforSoke: String = "Trenger penger",
    hvaSokesOm: String = "Jeg søker om penger",
): Begrunnelse {
    return Begrunnelse(hvorforSoke, hvaSokesOm)
}

fun opprettSamtykker(
    samtykker: Set<Samtykke> = setOf(
        Samtykke(SamtykkeType.BOSTOTTE, null, null),
        Samtykke(SamtykkeType.UTBETALING_SKATTEETATEN, null, null)
    )
): Set<Samtykke> {
    return samtykker
}

fun opprettBeskrivelseAvAnnet(
    barneutgifter: String? = "Masse barneutgifter",
    verdier: String? = "Ikke så mye verdier",
    sparing: String? = "Har lite sparing",
    utbetalinger: String? = "Det er få utbetalinger",
    boutgifter: String? = "Sjukt med boutgifter"
): BeskrivelseAvAnnet {
    return BeskrivelseAvAnnet(barneutgifter, verdier, sparing, utbetalinger, boutgifter)
}
