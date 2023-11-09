package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.generell

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde

fun Kilde.toJsonKilde() = JsonKilde.valueOf(name)