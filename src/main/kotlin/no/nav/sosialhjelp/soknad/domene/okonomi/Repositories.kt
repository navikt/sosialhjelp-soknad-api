package no.nav.sosialhjelp.soknad.domene.okonomi

import no.nav.sosialhjelp.soknad.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.domene.soknad.SoknadBubbleObject
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@NoRepositoryBean
interface OkonomiRepository<T: SoknadBubbleObject>: ListCrudRepository<T, UUID>

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
