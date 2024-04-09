package no.nav.sosialhjelp.soknad.v2.json

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBorSammenMed
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling.Mottaker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg.HendelseType
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
fun <T> copyJsonClass(json: T): T {
    return mapper.readValue(
        mapper.writeValueAsString(json),
        json!!::class.java
    )
}

fun createJsonInternalSoknad(): JsonInternalSoknad {
    return JsonInternalSoknad().apply {
        soknad = createJsonSoknad()
        vedlegg = createJsonVedleggSpesifikasjon()
        mottaker = createJsonSoknadsmottakerInternal()
        midlertidigAdresse = createGateAdresse()
    }
}

fun createJsonSoknadsmottakerInternal(): JsonSoknadsmottaker {
    return JsonSoknadsmottaker().apply {
        this.navEnhetsnavn = "NAV-enhet"
        this.organisasjonsnummer = "123456789"
    }
}

fun createJsonSoknad(): JsonSoknad {
    return JsonSoknad().apply {
        this.data = createJsonData()
        this.mottaker = createJsonSoknadsmottaker()
        this.driftsinformasjon = createJsonDriftsInformasjon()
        this.innsendingstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()
    }
}

fun createJsonData(): JsonData {
    return JsonData().apply {
        this.personalia = createJsonPersonalia()
        this.familie = createJsonFamilie()
        this.arbeid = createJsonArbeid()
        this.okonomi = creaJsonOkonomi()
        this.begrunnelse = createJsonBegrunnelse()
        this.bosituasjon = createJsonBosituasjon()
        this.utdanning = createJsonUtdanning()
    }
}

fun createJsonPersonalia(): JsonPersonalia {
    return JsonPersonalia().apply {
        this.personIdentifikator = JsonPersonIdentifikator().withKilde(JsonPersonIdentifikator.Kilde.SYSTEM).withVerdi("04325512412")
        this.nordiskBorger = JsonNordiskBorger().withKilde(SYSTEM).withVerdi(true)
        this.navn = JsonSokernavn().withKilde(JsonSokernavn.Kilde.SYSTEM).withFornavn("Ola").withEtternavn("Nordmann")
        this.folkeregistrertAdresse = createGateAdresse()
        this.oppholdsadresse = createMatrikkelAdresse()
        this.kontonummer = JsonKontonummer().withKilde(SYSTEM).withVerdi("12341212345").withHarIkkeKonto(false)
        this.statsborgerskap = JsonStatsborgerskap().withKilde(SYSTEM).withVerdi("NOR")
        this.telefonnummer = JsonTelefonnummer().withKilde(SYSTEM).withVerdi("94231341")
        this.postadresse = createPostboksAdresse()
    }
}

fun createJsonFamilie(): JsonFamilie? {
    return JsonFamilie().apply {
        this.forsorgerplikt = createJsonForsorgerplikt()
        this.sivilstatus = createJsonSivilstatus()
    }
}

fun createJsonSivilstatus(): JsonSivilstatus {
    return JsonSivilstatus().apply {
        this.kilde = SYSTEM
        this.status = JsonSivilstatus.Status.GIFT
        this.ektefelle = createEktefelle()
        this.borSammenMed = true
        this.ektefelleHarDiskresjonskode = false
        this.folkeregistrertMedEktefelle = true
    }
}

fun createEktefelle(): JsonEktefelle {
    return JsonEktefelle().apply {
        this.personIdentifikator = "12345678901"
        this.fodselsdato = "1990-01-01"
        this.navn = JsonNavn().withFornavn("Kari").withEtternavn("Nordmann")
    }
}

fun createJsonForsorgerplikt(): JsonForsorgerplikt {
    return JsonForsorgerplikt().apply {
        this.harForsorgerplikt = JsonHarForsorgerplikt().withKilde(SYSTEM).withVerdi(true)
        this.barnebidrag = JsonBarnebidrag().withKilde(JsonKildeBruker.BRUKER).withVerdi(JsonBarnebidrag.Verdi.INGEN)
        this.ansvar = createJsonAnsvarListe()
    }
}

fun createJsonAnsvarListe(): List<JsonAnsvar> {
    return listOf(
        createJsonAnsvar("01011054251", "2010-01-01", "Jentebarn", "Nordmann"),
        createJsonAnsvar("01011542323", "2015-01-01", "Guttebarn", "Nordmann")
    )
}

fun createJsonAnsvar(personId: String, fodselsDato: String, fornavn: String, etternavn: String): JsonAnsvar {
    return JsonAnsvar().apply {
        this.barn = createJsonBarn(personId, fodselsDato, fornavn, etternavn)
        this.borSammenMed = JsonBorSammenMed().withKilde(JsonKildeBruker.BRUKER).withVerdi(true)
        this.erFolkeregistrertSammen = JsonErFolkeregistrertSammen().withKilde(JsonKildeSystem.SYSTEM).withVerdi(true)
        this.harDeltBosted = JsonHarDeltBosted().withKilde(JsonKildeBruker.BRUKER).withVerdi(false)
        this.samvarsgrad = JsonSamvarsgrad().withKilde(JsonKildeBruker.BRUKER).withVerdi(100)
    }
}

fun createJsonBarn(personId: String, fodselsDato: String, fornavn: String, etternavn: String): JsonBarn {
    return JsonBarn().apply {
        this.kilde = SYSTEM
        this.personIdentifikator = personId
        this.fodselsdato = fodselsDato
        this.navn = JsonNavn().withFornavn(fornavn).withEtternavn(etternavn)
        this.harDiskresjonskode = false
    }
}

fun createJsonArbeid(): JsonArbeid? {
    return JsonArbeid().apply {
        this.kommentarTilArbeidsforhold = JsonKommentarTilArbeidsforhold().withKilde(JsonKildeBruker.BRUKER).withVerdi("Jeg er veldig glad i å jobbe")
        this.forhold = createJsonArbeidsforholdList()
    }
}

fun createJsonArbeidsforholdList(): List<JsonArbeidsforhold> {
    return listOf(
        createJsonArbeidsforhold("Skatteetaten", "2010-01-01", "2015-12-31"),
        createJsonArbeidsforhold("NAV", "2016-01-01", null)
    )
}

fun createJsonArbeidsforhold(arbeidsgivernavn: String, start: String, slutt: String?): JsonArbeidsforhold {
    return JsonArbeidsforhold().apply {
        this.kilde = SYSTEM
        this.arbeidsgivernavn = arbeidsgivernavn
        this.fom = start
        this.tom = slutt
        this.overstyrtAvBruker = false
        this.stillingsprosent = 100
        this.stillingstype = Stillingstype.FAST
    }
}

fun creaJsonOkonomi(): JsonOkonomi {
    return JsonOkonomi().apply {
        this.oversikt = createOkonomiOversikt()
        this.opplysninger = createOkonomiOpplysning()
    }
}

fun createOkonomiOpplysning(): JsonOkonomiopplysninger {
    return JsonOkonomiopplysninger().apply {
        this.utbetaling = createUtbetalingList()
        this.utgift = createUtgiftList()
        this.bostotte = createJsonBostotte()
        this.bekreftelse = createOkonomiBekreftelseList()
        this.beskrivelseAvAnnet = createOkonomiBeskrivelseAvAnnet()
    }
}

fun createUtbetalingList(): List<JsonOkonomiOpplysningUtbetaling> {
    return listOf(
        createJsonOkonomiOpplysningUtbetaling(BRUKER),
        createJsonOkonomiOpplysningUtbetaling(SYSTEM)
    )
}

fun createJsonOkonomiOpplysningUtbetaling(kide: JsonKilde): JsonOkonomiOpplysningUtbetaling {
    return JsonOkonomiOpplysningUtbetaling().apply {
        this.kilde = kilde
        this.type = "Lønn"
        this.tittel = "Lønnsslipp"
        this.organisasjon = JsonOrganisasjon().withNavn("NAV").withOrganisasjonsnummer("123456789")
        this.belop = 10000
        this.netto = 8000.00
        this.brutto = 10000.00
        this.skattetrekk = 2000.00
        this.andreTrekk = 0.00
        this.utbetalingsdato = "2021-01-01"
        this.periodeFom = "2021-01-01"
        this.periodeTom = "2021-01-31"
        this.komponenter = createOpplysningUtbetalingKomponentList()
        this.overstyrtAvBruker = false
        this.mottaker = Mottaker.HUSSTAND
    }
}

fun createOpplysningUtbetalingKomponentList(): List<JsonOkonomiOpplysningUtbetalingKomponent> {
    return listOf(
        JsonOkonomiOpplysningUtbetalingKomponent().apply {
            this.type = "Utbetaling"
            this.belop = 14000.00
            this.satsType = "Prosent"
            this.satsBelop = 0.25
            this.satsAntall = 4.00
        },
        JsonOkonomiOpplysningUtbetalingKomponent().apply {
            this.type = "Utbetaling"
            this.belop = 9000.00
            this.satsType = "Prosent"
            this.satsBelop = 0.25
            this.satsAntall = 2.00
        }
    )
}

fun createJsonOkonomiOpplysningUtbetalingKomponent(
    type: String,
    sum: Double
): JsonOkonomiOpplysningUtbetalingKomponent {
    return JsonOkonomiOpplysningUtbetalingKomponent().apply {
        this.type = type
        this.belop = sum
        this.satsType = "Prosent"
        this.satsBelop = 0.25
        this.satsAntall = 4.00
    }
}

fun createOkonomiBeskrivelseAvAnnet(): JsonOkonomibeskrivelserAvAnnet {
    return JsonOkonomibeskrivelserAvAnnet().apply {
        this.kilde = JsonKildeBruker.BRUKER
        this.utbetaling = "Jeg har fått lønn fra NAV"
        this.verdi = "Jeg har flere biler"
        this.barneutgifter = "Jeg har betalt barnebidrag"
        this.boutgifter = "Jeg har betalt husleie"
        this.sparing = "Jeg har spart penger"
    }
}

fun createOkonomiBekreftelseList(): List<JsonOkonomibekreftelse>? {
    return listOf(
        JsonOkonomibekreftelse().apply {
            this.kilde = BRUKER
            this.type = "Lønn"
            this.tittel = "Lønnsslipp"
            this.verdi = true
            this.bekreftelsesDato = "2021-01-01"
        },
        JsonOkonomibekreftelse().apply {
            this.kilde = BRUKER
            this.type = "Lønn"
            this.tittel = "Lønnsslipp"
            this.verdi = true
            this.bekreftelsesDato = "2021-01-01"
        }
    )
}

fun createJsonBostotte(): JsonBostotte {
    return JsonBostotte().apply {
        this.saker = listOf(
            JsonBostotteSak().apply {
                this.kilde = JsonKildeSystem.SYSTEM
                this.type = "Bostøtte"
                this.status = "Innvilget"
                this.beskrivelse = "Bostøtte innvilget"
                this.dato = "2021-01-01"
                this.vedtaksstatus = JsonBostotteSak.Vedtaksstatus.INNVILGET
            },
            JsonBostotteSak().apply {
                this.kilde = JsonKildeSystem.SYSTEM
                this.type = "Bostøtte"
                this.status = "Avvist"
                this.beskrivelse = "Bostøtte Avvist"
                this.dato = "2018-01-01"
                this.vedtaksstatus = JsonBostotteSak.Vedtaksstatus.AVSLAG
            }
        )
    }
}

fun createUtgiftList(): List<JsonOkonomiOpplysningUtgift> {
    return listOf(
        JsonOkonomiOpplysningUtgift().apply {
            this.kilde = BRUKER
            this.type = "Bil"
            this.tittel = "Bilutgifter"
            this.belop = 1000
            this.overstyrtAvBruker = false
        },
        JsonOkonomiOpplysningUtgift().apply {
            this.kilde = BRUKER
            this.type = "Båt"
            this.tittel = "Båtutgifter"
            this.belop = 2400
            this.overstyrtAvBruker = false
        }
    )
}

fun createOkonomiOversikt(): JsonOkonomioversikt {
    return JsonOkonomioversikt().apply {
        this.utgift = createOkonomiOversiktUtgiftList()
        this.inntekt = createOkonomiOversiktInntektList()
        this.formue = createOkonomiOversiktFormueList()
    }
}

fun createOkonomiOversiktUtgiftList(): List<JsonOkonomioversiktUtgift> {
    return listOf(
        JsonOkonomioversiktUtgift().apply {
            this.kilde = BRUKER
            this.type = "Bil"
            this.tittel = "Bilutgifter"
            this.belop = 1000
            this.overstyrtAvBruker = false
        },
        JsonOkonomioversiktUtgift().apply {
            this.kilde = BRUKER
            this.type = "Båt"
            this.tittel = "Båtutgifter"
            this.belop = 5000
            this.overstyrtAvBruker = false
        }
    )
}

fun createOkonomiOversiktInntektList(): List<JsonOkonomioversiktInntekt> {
    return listOf(
        JsonOkonomioversiktInntekt().apply {
            this.kilde = BRUKER
            this.type = "Lønn"
            this.tittel = "Lønnsslipp"
            this.brutto = 10000
            this.netto = 8000
            this.overstyrtAvBruker = false
        },
        JsonOkonomioversiktInntekt().apply {
            this.kilde = BRUKER
            this.type = "Barnetrygd"
            this.tittel = "Barnetrygd"
            this.brutto = 1000
            this.netto = 800
            this.overstyrtAvBruker = false
        }
    )
}

fun createOkonomiOversiktFormueList(): List<JsonOkonomioversiktFormue> {
    return listOf(
        JsonOkonomioversiktFormue().apply {
            this.kilde = BRUKER
            this.type = "Bolig"
            this.tittel = "Bolig"
            this.belop = 1000000
            this.overstyrtAvBruker = false
        },
        JsonOkonomioversiktFormue().apply {
            this.kilde = BRUKER
            this.type = "Bil"
            this.tittel = "Bil"
            this.belop = 50000
            this.overstyrtAvBruker = false
        }
    )
}

fun createJsonBegrunnelse(): JsonBegrunnelse {
    return JsonBegrunnelse().apply {
        this.kilde = JsonKildeBruker.BRUKER
        this.hvaSokesOm = "Jeg søker om bostøtte"
        this.hvorforSoke = "Jeg trenger bostøtte for å betale husleie"
    }
}

fun createJsonBosituasjon(): JsonBosituasjon {
    return JsonBosituasjon().apply {
        this.kilde = JsonKildeBruker.BRUKER
        this.botype = JsonBosituasjon.Botype.EIER
        this.antallPersoner = 3
    }
}

fun createJsonUtdanning(): JsonUtdanning {
    return JsonUtdanning().apply {
        this.kilde = BRUKER
        this.erStudent = true
        this.studentgrad = JsonUtdanning.Studentgrad.HELTID
    }
}

fun createJsonDriftsInformasjon(): JsonDriftsinformasjon {
    return JsonDriftsinformasjon().apply {
        this.stotteFraHusbankenFeilet = false
        this.utbetalingerFraNavFeilet = false
        this.inntektFraSkatteetatenFeilet = false
    }
}

fun createJsonVedleggSpesifikasjon(): JsonVedleggSpesifikasjon {
    return JsonVedleggSpesifikasjon().apply {
        this.vedlegg = listOf(
            JsonVedlegg().apply {
                this.type = "Søknad"
                this.status = "Innsendt"
                this.hendelseType = HendelseType.BRUKER
                this.tilleggsinfo = "Søknad om bostøtte"
                this.hendelseReferanse = UUID.randomUUID().toString()
                this.filer = listOf(
                    JsonFiler().apply {
                        this.sha512 = UUID.randomUUID().toString()
                        this.filnavn = "soknad.pdf"
                    },
                    JsonFiler().apply {
                        this.sha512 = UUID.randomUUID().toString()
                        this.filnavn = "vedlegg.pdf"
                    }
                )
            },
            JsonVedlegg().apply {
                this.type = "Inntekt"
                this.status = "Innsendt"
                this.hendelseType = HendelseType.BRUKER
                this.tilleggsinfo = "Lønnsslipp"
                this.hendelseReferanse = UUID.randomUUID().toString()
                this.filer = listOf(
                    JsonFiler().apply {
                        this.sha512 = UUID.randomUUID().toString()
                        this.filnavn = "lonnsslipp_mai.pdf"
                    },
                    JsonFiler().apply {
                        this.sha512 = UUID.randomUUID().toString()
                        this.filnavn = "lonnsslipp_juni.pdf"
                    }
                )
            }
        )
    }
}

fun createJsonSoknadsmottaker(): no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker {
    return no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker().apply {
        this.navEnhetsnavn = "NAV-enhet"
        this.enhetsnummer = "123456789"
        this.kommunenummer = "1234"
    }
}

fun createGateAdresse(): JsonGateAdresse {
    return JsonGateAdresse().apply {
        this.kilde = SYSTEM
        this.type = JsonAdresse.Type.GATEADRESSE
        this.adresseValg = JsonAdresseValg.FOLKEREGISTRERT
        this.kommunenummer = "1234"
        this.poststed = "Oslo"
        this.adresselinjer = listOf("Adresselinje 1", "Adresselinje 2")
        this.bolignummer = "123"
        this.gatenavn = "Gateveien"
        this.husnummer = "12"
        this.husbokstav = "A"
        this.landkode = "NO"
        this.postnummer = "1234"
    }
}

fun createMatrikkelAdresse(): JsonMatrikkelAdresse {
    return JsonMatrikkelAdresse().apply {
        this.kilde = SYSTEM
        this.type = JsonAdresse.Type.MATRIKKELADRESSE
        this.adresseValg = JsonAdresseValg.FOLKEREGISTRERT
        this.kommunenummer = "1234"
        this.gaardsnummer = "123"
        this.bruksnummer = "123"
        this.seksjonsnummer = "123"
        this.festenummer = "123"
        this.undernummer = "123"
    }
}

fun createUstrukturertAdresse(): JsonUstrukturertAdresse {
    return JsonUstrukturertAdresse().apply {
        this.kilde = SYSTEM
        this.type = JsonAdresse.Type.USTRUKTURERT
        this.adresseValg = JsonAdresseValg.FOLKEREGISTRERT
        this.adresse = listOf(
            "Ustrukturert-veien",
            "xxxx",
            "xxxx Ustrukturert"
        )
    }
}

fun createPostboksAdresse(): JsonPostboksAdresse {
    return JsonPostboksAdresse().apply {
        this.kilde = SYSTEM
        this.type = JsonAdresse.Type.POSTBOKS
        this.adresseValg = JsonAdresseValg.MIDLERTIDIG
        this.postnummer = "1234"
        this.postboks = "1234"
        this.poststed = "Oslo"
    }
}
