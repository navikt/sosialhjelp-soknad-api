package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import java.util.Optional

interface SoknadUnderArbeidRepository {
    fun opprettSoknad(soknadUnderArbeid: SoknadUnderArbeid, eier: String): Long?
    fun hentSoknad(soknadId: Long, eier: String): Optional<SoknadUnderArbeid>
    fun hentSoknad(behandlingsId: String?, eier: String): SoknadUnderArbeid
    fun hentSoknadOptional(behandlingsId: String?, eier: String): Optional<SoknadUnderArbeid>
    fun hentEttersendingMedTilknyttetBehandlingsId(tilknyttetBehandlingsId: String, eier: String): Optional<SoknadUnderArbeid>
    fun oppdaterSoknadsdata(soknadUnderArbeid: SoknadUnderArbeid, eier: String)
    fun oppdaterInnsendingStatus(soknadUnderArbeid: SoknadUnderArbeid, eier: String)
    fun slettSoknad(soknadUnderArbeid: SoknadUnderArbeid, eier: String)
}
