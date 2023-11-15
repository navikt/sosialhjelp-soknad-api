package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.generell

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
import no.nav.sosialhjelp.soknad.nymodell.domene.Navn

fun Kilde.toJsonKilde() = JsonKilde.valueOf(name)

fun Navn.toJsonSoknernavn() = JsonSokernavn()
    .withFornavn(fornavn)
    .withMellomnavn(mellomnavn)
    .withEtternavn(etternavn)