package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createDefaultHttpClient
import no.nav.sosialhjelp.soknad.app.client.config.soknadJacksonMapper
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.metrics.Vedleggstatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.StringToOpplysningTypeConverter
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.UUID

@Component
class UploadClient(
    @param:Value("\${sosialhjelp_upload_url}") private val uploadUrl: String,
    @param:Value("\${sosialhjelp_upload_audience}") private val uploadAudience: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient: WebClient =
        webClientBuilder
            .configureWebClientBuilder(createDefaultHttpClient())
            .baseUrl(uploadUrl)
            .codecs { it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(soknadJacksonMapper)) }
            .build()

    fun getVedleggSpesifikasjon(soknadId: UUID): JsonVedleggSpesifikasjon {
        val userToken = SubjectHandlerUtils.getToken()
        val tokenXToken = texasService.exchangeToken(userToken, IdentityProvider.TOKENX, uploadAudience)

        val spec =
            webClient
                .get()
                .uri("/sosialhjelp/upload/vedlegg/{soknadId}", soknadId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $tokenXToken")
                .retrieve()
                .bodyToMono<VedleggSpesifikasjon>()
                .block() ?: throw IllegalStateException("Fikk null-respons fra sosialhjelp-upload for soknadId $soknadId")
        return JsonVedleggSpesifikasjon().withVedlegg(
            spec.vedlegg.mapNotNull { vedlegg ->
                if (vedlegg.kategori == null) return@mapNotNull null
                val opplysningType = StringToOpplysningTypeConverter.convert(vedlegg.kategori)
                val (type, tilleggsinfo) =
                    VedleggType[opplysningType].let {
                        it.getTypeString() to it.getTilleggsinfoString()
                    }
                JsonVedlegg()
                    .withType(type)
                    .withTilleggsinfo(tilleggsinfo)
                    .withStatus(Vedleggstatus.LastetOpp.toString())
                    .withHendelseType(if (opplysningType.isUtgiftTypeAnnet()) JsonVedlegg.HendelseType.BRUKER else JsonVedlegg.HendelseType.SOKNAD)
                    .withHendelseReferanse(if (opplysningType.isUtgiftTypeAnnet()) null else UUID.randomUUID().toString())
                    .withFiler(
                        vedlegg.filer.map { fil ->
                            JsonFiler()
                                .withFilnavn(fil.filnavn)
                                .withSha512(fil.sha512)
                        },
                    )
            },
        )
    }
}

data class VedleggSpesifikasjon(
    val vedlegg: List<Vedlegg>,
)

data class Vedlegg(
    val kategori: String? = null,
    val filer: List<Fil>,
)

data class Fil(
    val filnavn: String,
    val sha512: String? = null,
)

private fun OpplysningType.isUtgiftTypeAnnet() = this == UtgiftType.UTGIFTER_ANDRE_UTGIFTER
