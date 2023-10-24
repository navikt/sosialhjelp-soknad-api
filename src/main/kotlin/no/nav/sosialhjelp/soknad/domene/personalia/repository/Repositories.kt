package no.nav.sosialhjelp.soknad.domene.personalia.repository

import no.nav.sosialhjelp.soknad.domene.personalia.Kontonummer
import no.nav.sosialhjelp.soknad.domene.personalia.Telefonnummer
import org.springframework.data.repository.Repository
import java.util.*

@org.springframework.stereotype.Repository
interface TelefonnummerRepository: TelefonFragmentRepository, Repository<Telefonnummer, UUID>
@org.springframework.stereotype.Repository
interface KontonummerRepository: KontoFragmentRepository, Repository<Kontonummer, UUID>
