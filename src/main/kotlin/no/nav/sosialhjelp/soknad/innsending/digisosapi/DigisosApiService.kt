package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.springframework.stereotype.Component
import java.util.Collections
import java.util.UUID
import java.util.concurrent.Future

@Component
class DigisosApiService(
    private val readClient: ReadClient,
    private val sendSoknadClient: SendSoknadClient,
    private val krypteringService: KrypteringService,
    private val dokumentlagerClient: DokumentlagerClient,
) {
    private val fiksX509Certificate get() = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()

    fun sendSoknad(
        tilleggsinformasjonJson: String,
        soknadJson: String,
        vedleggJson: String,
        pdfDokumenter: List<FilOpplasting>,
        kommunenummer: String,
        soknadId: UUID,
    ): SendSoknadResponse {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(pdfDokumenter.size))
        val response: SendSoknadResponse
        try {
            response =
                sendSoknadClient.lastOppFiler(
                    soknadJson,
                    tilleggsinformasjonJson,
                    vedleggJson,
                    pdfDokumenter.map { dokument: FilOpplasting ->
                        FilOpplasting(
                            metadata = dokument.metadata,
                            data = krypteringService.krypter(dokument.data, krypteringFutureList, fiksX509Certificate),
                        )
                    },
                    kommunenummer,
                    soknadId,
                )
            waitForFutures(krypteringFutureList)
        } finally {
            krypteringFutureList
                .filter { !it.isDone && !it.isCancelled }
                .forEach { it.cancel(true) }
        }
        return response
    }

    fun getSoknaderForUser(): List<DigisosSak> = readClient.getSoknader()

    fun getInnsynsfilForSoknad(
        fiksDigisosId: String,
        dokumentId: String,
    ): JsonDigisosSoker = readClient.getInnsynsfil(fiksDigisosId, dokumentId)

    fun getDigisosIdsStatusMottatt(digisosIds: List<UUID>): List<UUID> {
        return if (digisosIds.isEmpty()) {
            emptyList()
        } else {
            readClient
                .getStatusForSoknader(digisosIds).statusListe
                .filter { it.levertFagsystem }
                .map { it.digisosId }
        }
    }
}
