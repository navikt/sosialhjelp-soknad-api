package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg.FOLKEREGISTRERT
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.register.fetchers.person.toV2Adresse
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@Component
class FolkeregistrertAdresseResolver(
    private val metadataService: SoknadMetadataService,
    private val personService: PersonService,
    private val hentAdresseService: HentAdresseService,
) {
    fun getCurrentFolkeregistrertAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        savedFolkeregistrertAdresse: Adresse?,
    ): Adresse? {
        // Ikke sjekk folkeregistrertadresse hvis søknaden er opprettet i dag eller hvis valgt ikke er folkeregistrert
        if (LocalDate.now().isEqual(createdDate(soknadId)) || adresseValg != FOLKEREGISTRERT) {
            return savedFolkeregistrertAdresse
        }

        return getCurrentFolkeregistrertAdresse().takeIf { it != savedFolkeregistrertAdresse }
            ?: savedFolkeregistrertAdresse
    }

    private fun getCurrentFolkeregistrertAdresse(): Adresse? =
        personService.hentPerson(personId(), false)?.bostedsadresse?.toV2Adresse(hentAdresseService)

    private fun createdDate(soknadId: UUID) = metadataService.getMetadataForSoknad(soknadId).tidspunkt.opprettet.toLocalDate()
}
