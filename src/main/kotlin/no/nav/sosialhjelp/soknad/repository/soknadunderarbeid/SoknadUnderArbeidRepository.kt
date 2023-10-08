package no.nav.sosialhjelp.soknad.repository.soknadunderarbeid

interface SoknadUnderArbeidRepository {
    fun opprettSoknad(soknadUnderArbeid: SoknadUnderArbeid, eier: String): Long?
    fun hentSoknad(soknadId: Long, eier: String): SoknadUnderArbeid?
    fun hentSoknad(behandlingsId: String?, eier: String): SoknadUnderArbeid
    fun hentSoknadNullable(behandlingsId: String?, eier: String): SoknadUnderArbeid?
    fun hentEttersendingMedTilknyttetBehandlingsId(tilknyttetBehandlingsId: String, eier: String): SoknadUnderArbeid?
    fun oppdaterSoknadsdata(soknadUnderArbeid: SoknadUnderArbeid, eier: String)
    fun oppdaterInnsendingStatus(soknadUnderArbeid: SoknadUnderArbeid, eier: String)
    fun slettSoknad(soknadUnderArbeid: SoknadUnderArbeid, eier: String)
}
