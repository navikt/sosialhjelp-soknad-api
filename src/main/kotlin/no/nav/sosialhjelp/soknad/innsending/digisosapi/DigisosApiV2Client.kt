package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService

interface DigisosApiV2Client {
    fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: String,
        token: String?
    ): String
}

class DigisosApiV2ClientImpl(
    private val digisosApiEndpoint: String,
    private val integrasjonsidFiks: String,
    private val integrasjonpassordFiks: String,
    private val kommuneInfoService: KommuneInfoService,
    private val dokumentlagerClient: DokumentlagerClient,
    private val serviceUtils: ServiceUtils
) : DigisosApiV2Client {

    override fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: String,
        token: String?
    ): String {
        // todo implement
        //  send soknad til "$digisosApiEndpoint/digisos/api/v2/soknader/$kommunenummer/$behandlingsId"
        return ""
    }
}
