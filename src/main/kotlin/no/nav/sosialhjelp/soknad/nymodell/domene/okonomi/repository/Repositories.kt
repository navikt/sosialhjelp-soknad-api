package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.repository

import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Bostotte
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.OneOfManyObject
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.Utgift
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@NoRepositoryBean
interface OkonomiRepository<T: OneOfManyObject>: ListCrudRepository<T, UUID> {
    fun findAllBySoknadId(soknadId: UUID): List<T>
}

@Repository
interface UtgiftRepository: UpsertRepository<Utgift>, OkonomiRepository<Utgift>

@Repository
interface InntektRepository: UpsertRepository<Inntekt>, OkonomiRepository<Inntekt>

@Repository
interface FormueRepository: UpsertRepository<Formue>, OkonomiRepository<Formue>

@Repository
interface BostotteRepository: UpsertRepository<Bostotte>, OkonomiRepository<Bostotte>

@Repository
interface BekreftelseRepository: UpsertRepository<Bekreftelse>, OkonomiRepository<Bekreftelse>
