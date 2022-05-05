package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService

interface DigisosApiV2Client {

}

class DigisosApiV2ClientImpl(
    private val digisosApiEndpoint: String,
    private val integrasjonsidFiks: String,
    private val integrasjonpassordFiks: String,
    private val kommuneInfoService: KommuneInfoService,
    private val dokumentlagerClient: DokumentlagerClient,
    private val serviceUtils: ServiceUtils
) : DigisosApiV2Client {

    // todo implement
    //  send soknad til "$digisosApiEndpoint/digisos/api/v2/soknader/$kommunenummer/$behandlingsId"
}
