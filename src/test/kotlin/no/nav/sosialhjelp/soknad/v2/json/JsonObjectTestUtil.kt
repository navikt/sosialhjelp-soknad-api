package no.nav.sosialhjelp.soknad.v2.json

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon

fun createEmptyJsonInternalSoknad(
    eier: String,
    kortSoknad: Boolean,
): JsonInternalSoknad =
    JsonInternalSoknad()
        .withSoknad(
            JsonSoknad()
                .withData(
                    JsonData()
                        .withSoknadstype(if (kortSoknad) JsonData.Soknadstype.KORT else JsonData.Soknadstype.STANDARD)
                        .withPersonalia(
                            JsonPersonalia()
                                .withPersonIdentifikator(
                                    JsonPersonIdentifikator()
                                        .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                        .withVerdi(eier),
                                ).withNavn(
                                    JsonSokernavn()
                                        .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                        .withFornavn("")
                                        .withMellomnavn("")
                                        .withEtternavn(""),
                                ).withKontonummer(
                                    JsonKontonummer()
                                        .withKilde(SYSTEM),
                                ),
                        )
                        .withFamilie(
                            JsonFamilie()
                                .withForsorgerplikt(JsonForsorgerplikt()),
                        ).let { if (kortSoknad) it.withKortSoknadFelter() else it.withStandardSoknadFelter() },
                ).withMottaker(
                    no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker()
                        .withNavEnhetsnavn("")
                        .withEnhetsnummer(""),
                ).withDriftsinformasjon(
                    JsonDriftsinformasjon()
                        .withUtbetalingerFraNavFeilet(false)
                        .withInntektFraSkatteetatenFeilet(false)
                        .withStotteFraHusbankenFeilet(false),
                ).withKompatibilitet(ArrayList()),
        ).withVedlegg(
            if (kortSoknad) {
                JsonVedleggSpesifikasjon().withVedlegg(
                    mutableListOf(
                        JsonVedlegg().withType("kort").withTilleggsinfo("behov"),
                        JsonVedlegg().withType("faktura").withTilleggsinfo("barnehage"),
                        JsonVedlegg().withType("faktura").withTilleggsinfo("sfo"),
                        JsonVedlegg().withType("husbanken").withTilleggsinfo("vedtak"),
                        JsonVedlegg().withType("husleiekontrakt").withTilleggsinfo("husleiekontrakt"),
                        JsonVedlegg().withType("kontooversikt").withTilleggsinfo("annet"),
                        JsonVedlegg().withType("lonnslipp").withTilleggsinfo("arbeid"),
                        JsonVedlegg().withType("faktura").withTilleggsinfo("strom"),
                        JsonVedlegg().withType("student").withTilleggsinfo("vedtak"),
                        JsonVedlegg().withType("annet").withTilleggsinfo("annet").withStatus("LastetOpp"),
                    ),
                )
            } else {
                JsonVedleggSpesifikasjon()
            },
        )

fun JsonData.withStandardSoknadFelter(): JsonData =
    withSoknadstype(JsonData.Soknadstype.STANDARD)
        .withArbeid(JsonArbeid())
        .withUtdanning(
            JsonUtdanning()
                .withKilde(BRUKER),
        ).withBegrunnelse(
            JsonBegrunnelse()
                .withKilde(JsonKildeBruker.BRUKER)
                .withHvorforSoke("")
                .withHvaSokesOm(""),
        ).withBosituasjon(
            JsonBosituasjon()
                .withKilde(JsonKildeBruker.BRUKER),
        ).withOkonomi(
            JsonOkonomi()
                .withOpplysninger(
                    JsonOkonomiopplysninger()
                        .withUtbetaling(ArrayList())
                        .withUtgift(ArrayList())
                        .withBostotte(JsonBostotte())
                        .withBekreftelse(ArrayList()),
                ).withOversikt(
                    JsonOkonomioversikt()
                        .withInntekt(ArrayList())
                        .withUtgift(ArrayList())
                        .withFormue(ArrayList()),
                ),
        )

fun JsonData.withKortSoknadFelter(): JsonData =
    withArbeid(JsonArbeid())
        .withBegrunnelse(
            JsonBegrunnelse()
                .withKilde(JsonKildeBruker.BRUKER)
                .withHvaSokesOm(""),
        ).withOkonomi(JsonOkonomi().withOpplysninger(JsonOkonomiopplysninger().withUtbetaling(ArrayList()).withBostotte(JsonBostotte()).withBekreftelse(ArrayList())))
