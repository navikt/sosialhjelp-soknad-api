package no.nav.sosialhjelp.soknad.v2

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentRef
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseInput
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeid
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Livssituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Utdanning
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.BruttoNetto
import no.nav.sosialhjelp.soknad.v2.okonomi.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.Komponent
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.v2.okonomi.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.Situasjonsendring
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Integrasjonstatus
import no.nav.sosialhjelp.soknad.v2.soknad.Kategori
import no.nav.sosialhjelp.soknad.v2.soknad.Kategorier
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker as MottakerSoknad

fun createValidEmptyJsonInternalSoknad(): JsonInternalSoknad {
    return createJsonInternalSoknadWithInitializedSuperObjects()
        .apply {
            soknad
                .withDriftsinformasjon(JsonDriftsinformasjon().withInntektFraSkatteetatenFeilet(false))
                .withMottaker(MottakerSoknad())
            soknad.data
                .withOkonomi(JsonOkonomi().withOpplysninger(JsonOkonomiopplysninger()))
                .withBegrunnelse(JsonBegrunnelse().withKilde(JsonKildeBruker.BRUKER).withHvaSokesOm(""))
                .withPersonalia(JsonPersonalia())
            soknad.data.personalia
                .withKontonummer(JsonKontonummer().withKilde(JsonKilde.SYSTEM))
                .withNavn(JsonSokernavn().withFornavn("").withMellomnavn("").withEtternavn(""))
                .withPersonIdentifikator(JsonPersonIdentifikator().withVerdi("12345612345"))
        }
}

fun createJsonInternalSoknadWithInitializedSuperObjects(): JsonInternalSoknad =
    JsonInternalSoknad().apply {
        soknad = JsonSoknad().withData(JsonData())
        vedlegg = JsonVedleggSpesifikasjon()
        mottaker = JsonSoknadsmottaker()
        midlertidigAdresse = JsonAdresse()
    }

fun createSituasjonsendring(
    soknadId: UUID,
    hvaErEndret: String? = "Ingenting",
    endring: Boolean? = true,
): Situasjonsendring =
    Situasjonsendring(
        soknadId = soknadId,
        hvaErEndret = hvaErEndret,
        endring = endring,
    )

fun createFamilie(
    soknadId: UUID,
    harForsorgerPlikt: Boolean? = true,
    barnebidrag: Barnebidrag? = Barnebidrag.BEGGE,
    sivilstatus: Sivilstatus? = Sivilstatus.GIFT,
    ansvar: List<Barn> = listOf(createBarn()),
    ektefelle: Ektefelle? = opprettEktefelle(),
) = Familie(
    soknadId = soknadId,
    harForsorgerplikt = harForsorgerPlikt,
    barnebidrag = barnebidrag,
    ansvar = ansvar.associateBy { UUID.randomUUID() },
    sivilstatus = sivilstatus,
    ektefelle = ektefelle,
)

fun opprettEktefelle(): Ektefelle =
    Ektefelle(
        navn = Navn("Kone", "", "Konesen"),
        fodselsdato = "432341",
        personId = SetupLifecycleIntegrationTest.ektefelleId,
        folkeregistrertMedEktefelle = true,
        borSammen = true,
        kildeErSystem = true,
    )

fun opprettSoknadMetadata(
    soknadId: UUID = UUID.randomUUID(),
    status: SoknadStatus = SoknadStatus.OPPRETTET,
    personId: String = AbstractIntegrationTest.userId,
    opprettetDato: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    innsendtDato: LocalDateTime? = null,
    kommunenummer: String? = "1234",
    digisosId: UUID? = UUID.randomUUID(),
): SoknadMetadata =
    SoknadMetadata(
        soknadId = soknadId,
        personId = personId,
        status = status,
        tidspunkt =
            no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt(
                opprettet = opprettetDato,
                sendtInn = innsendtDato,
            ),
        mottakerKommunenummer = kommunenummer,
        digisosId = digisosId,
    )

fun opprettSoknadMetadata(
    id: UUID = UUID.randomUUID(),
    kort: Boolean = false,
): SoknadMetadata =
    SoknadMetadata(
        soknadId = id,
        personId = AbstractIntegrationTest.userId,
        status = SoknadStatus.OPPRETTET,
        soknadType = if (kort) SoknadType.KORT else SoknadType.STANDARD,
        tidspunkt =
            no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt(
                opprettet = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            ),
        mottakerKommunenummer = "1234",
        digisosId = UUID.randomUUID(),
    )

fun opprettSoknad(
    id: UUID = UUID.randomUUID(),
    eierPersonId: String = AbstractIntegrationTest.userId,
    begrunnelse: Begrunnelse = opprettBegrunnelse(),
): Soknad =
    Soknad(
        id = id,
        eierPersonId = eierPersonId,
        begrunnelse = begrunnelse,
    )

fun opprettArbeidsforholdList(
    arbeidsforholdList: List<Arbeidsforhold> =
        listOf(
            opprettArbeidsforhold(),
            opprettArbeidsforhold(arbeidsgivernavn = "Annen arbeidsgive", orgnummer = "0987654321"),
        ),
): List<Arbeidsforhold> = arbeidsforholdList

fun opprettArbeidsforhold(
    arbeidsgivernavn: String = "Arbeidsgiversen",
    orgnummer: String? = "1234567890",
    start: LocalDate? = LocalDate.now().minusYears(4),
    slutt: LocalDate? = null,
    fastStillingsprosent: Int? = 100,
    harFastStilling: Boolean? = true,
): Arbeidsforhold = Arbeidsforhold(arbeidsgivernavn, orgnummer, start, slutt, fastStillingsprosent, harFastStilling)

fun opprettNavEnhet(
    enhetNr: String = "321321321",
    navn: String = "NAV-kontoret",
    kommunenummer: String = "5555",
    orgnummer: String? = null,
    kommunenavn: String = "Nav-kommunen",
): NavEnhet = NavEnhet(navn, enhetNr, kommunenummer, orgnummer, kommunenavn)

fun opprettEier(
    soknadId: UUID,
    statsborgerskap: String? = "Norsk",
    nordiskBoolean: Boolean? = true,
    kontonummer: Kontonummer = Kontonummer(false, null, "43234323432"),
    navn: Navn = opprettNavn(),
): Eier =
    Eier(
        soknadId = soknadId,
        statsborgerskap = statsborgerskap,
        nordiskBorger = nordiskBoolean,
        navn = navn,
        kontonummer = kontonummer,
    )

fun opprettNavn(
    fornavn: String = "Test",
    mellomnavn: String = "Tester",
    etternavn: String = "Testesen",
): Navn =
    Navn(
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
    )

fun createBarn(
    personId: String = SetupLifecycleIntegrationTest.barnPersonId,
    navn: Navn = Navn(fornavn = "Navn", etternavn = "Navnesen"),
    fodselsdato: String = "342434",
    borSammen: Boolean = true,
    folkeregistrertSammen: Boolean = true,
    deltBosted: Boolean = false,
    samvarsgrad: Int = 100,
) = Barn(personId, navn, fodselsdato, borSammen, folkeregistrertSammen, deltBosted, samvarsgrad)

fun opprettMidlertidigAdresse(
    adresselinjer: List<String> =
        listOf(
            "Bortiheiavegen 6",
            "2855 Poststed",
            "Norge",
        ),
): Adresse = UstrukturertAdresse(adresse = adresselinjer)

fun opprettKontakt(
    soknadId: UUID,
    telefonnummer: Telefonnummer = Telefonnummer("98766554", "+4798664534"),
    adresser: Adresser =
        Adresser(
            folkeregistrert = opprettFolkeregistrertAdresse(),
            midlertidig = opprettMatrikkelAdresse(),
            fraBruker = opprettMidlertidigAdresse(),
            adressevalg = AdresseValg.FOLKEREGISTRERT,
        ),
    navEnhet: NavEnhet = opprettNavEnhet(),
): Kontakt = Kontakt(soknadId, telefonnummer, adresser, navEnhet)

fun opprettMatrikkelAdresse(
    kommunenummer: String = "5432",
    gaardsnummer: String = "231",
    bruksnummer: String = "31",
    festenummer: String? = null,
    seksjonsnummer: String? = null,
    undernummer: String? = null,
): MatrikkelAdresse =
    MatrikkelAdresse(
        kommunenummer,
        gaardsnummer,
        bruksnummer,
        festenummer,
        seksjonsnummer,
        undernummer,
    )

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
): Adresse =
    VegAdresse(
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

fun opprettFolkeregistrertAdresseInput(
    landkode: String = "NO",
    kommunenummer: String? = "2944",
    adresselinjer: List<String> = listOf("Underetasjen", "Bak huset"),
    bolignummer: String? = "7",
    postnummer: String? = "2933",
    poststed: String? = "Poststedet",
    gatenavn: String? = "Vegadresseveien",
    husnummer: String? = "8",
    husbokstav: String? = "b",
): AdresseInput =
    VegAdresse(
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

fun opprettIntegrasjonstatus(
    soknadId: UUID,
    feilUtbetalingerNav: Boolean = false,
    feilInntektSkatteetaten: Boolean = false,
    feilStotteHusbanken: Boolean = false,
): Integrasjonstatus =
    Integrasjonstatus(
        soknadId = soknadId,
        feilUtbetalingerNav = feilUtbetalingerNav,
        feilInntektSkatteetaten = feilInntektSkatteetaten,
        feilStotteHusbanken = feilStotteHusbanken,
    )

fun opprettLivssituasjon(
    soknadId: UUID,
    arbeid: Arbeid = opprettArbeid(),
    utdanning: Utdanning = opprettUtdanning(),
    bosituasjon: Bosituasjon = opprettBosituasjon(),
): Livssituasjon = Livssituasjon(soknadId, arbeid, utdanning, bosituasjon)

fun opprettArbeid(
    kommentar: String? = "Jeg liker jobb",
    arbeidsforhold: List<Arbeidsforhold> = opprettArbeidsforholdList(),
): Arbeid = Arbeid(kommentar, arbeidsforhold)

fun opprettUtdanning(
    erStudent: Boolean = true,
    studentGrad: Studentgrad = Studentgrad.HELTID,
): Utdanning = Utdanning(erStudent, studentGrad)

fun opprettBegrunnelse(
    hvorforSoke: String = "Trenger penger",
    hvaSokesOm: String = "Jeg søker om penger",
    kategorier: Set<Kategori> = emptySet(),
): Begrunnelse =
    Begrunnelse(
        hvorforSoke = hvorforSoke,
        hvaSokesOm = hvaSokesOm,
        kategorier = Kategorier(definerte = kategorier),
    )

fun opprettBosituasjon(
    botype: Botype = Botype.EIER,
    antallPersoner: Int = 3,
): Bosituasjon = Bosituasjon(botype, antallPersoner)

fun opprettOkonomi(soknadId: UUID): Okonomi =
    Okonomi(
        soknadId = soknadId,
        inntekter = createInntekter(),
        utgifter = createUtgifter(),
        formuer = createFormuer(),
        bekreftelser = createBekreftelser(),
        bostotteSaker = createBostotteSaker(),
    )

fun createInntekter(): Set<Inntekt> =
    setOf(
        Inntekt(
            type = InntektType.JOBB,
            inntektDetaljer =
                OkonomiDetaljer(
                    detaljer =
                        listOf(
                            BruttoNetto(brutto = 40.0, netto = 20.0),
                            BruttoNetto(brutto = 60.0, netto = 30.0),
                        ),
                ),
        ),
        Inntekt(
            type = InntektType.UTBETALING_NAVYTELSE,
            inntektDetaljer =
                OkonomiDetaljer(
                    detaljer =
                        listOf(
                            UtbetalingMedKomponent(
                                utbetaling = Utbetaling(brutto = 123.0, utbetalingsdato = LocalDate.now()),
                                komponenter =
                                    listOf(
                                        Komponent(type = "Komponent 1", satsBelop = 400.0),
                                    ),
                                tittel = "Barnetrygd",
                            ),
                        ),
                ),
        ),
    )

fun createUtgifter(): Set<Utgift> =
    setOf(
        Utgift(
            type = UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
            utgiftDetaljer =
                OkonomiDetaljer(
                    detaljer =
                        listOf(
                            Belop(belop = 400.0),
                        ),
                ),
        ),
    )

fun createFormuer(): Set<Formue> =
    setOf(
        Formue(
            type = FormueType.FORMUE_BRUKSKONTO,
            formueDetaljer =
                OkonomiDetaljer(
                    detaljer =
                        listOf(
                            Belop(belop = 123.0),
                        ),
                ),
        ),
        Formue(
            type = FormueType.VERDI_KJORETOY,
            formueDetaljer =
                OkonomiDetaljer(
                    detaljer =
                        listOf(
                            Belop(belop = 500000.0),
                        ),
                ),
        ),
    )

fun createBekreftelser(): Set<Bekreftelse> =
    setOf(
        Bekreftelse(
            type = BekreftelseType.BEKREFTELSE_SPARING,
            verdi = true,
        ),
    )

fun createBostotteSaker(): List<BostotteSak> =
    listOf(
        BostotteSak(
            LocalDate.now(),
            BostotteStatus.UNDER_BEHANDLING,
            "Beskrivelse av bostotte",
            null,
        ),
        BostotteSak(
            LocalDate.now(),
            BostotteStatus.VEDTATT,
            "Annen beskrivelse av Bostotte",
            Vedtaksstatus.AVVIST,
        ),
    )

fun opprettDokumentasjon(
    id: UUID = UUID.randomUUID(),
    soknadId: UUID,
    status: DokumentasjonStatus = DokumentasjonStatus.LASTET_OPP,
    type: OpplysningType = UtgiftType.UTGIFTER_STROM,
    dokumenter: Set<DokumentRef> = opprettDokumenter(dokumentIds = listOf(UUID.randomUUID())),
): Dokumentasjon = Dokumentasjon(id, soknadId, type, status, dokumenter)

fun opprettDokumenter(dokumentIds: List<UUID>): Set<DokumentRef> =
    dokumentIds
        .map {
            DokumentRef(
                dokumentId = it,
                filnavn = "utskrift_brukskonto$dokumentIds.pdf",
            )
        }
        .toSet()
