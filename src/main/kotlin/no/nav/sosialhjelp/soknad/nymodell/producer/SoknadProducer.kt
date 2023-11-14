package no.nav.sosialhjelp.soknad.nymodell.producer

import java.util.*

interface SoknadProducer<T> {
    fun produceNew(soknadId: UUID): T
    fun produceFrom(soknadId: UUID, obj: T): T
}