//package no.nav.sosialhjelp.soknad.app.rest.feil
//
//import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
//import no.nav.sosialhjelp.soknad.app.exceptions.EttersendelseSendtForSentException
//import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
//import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
//import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErIkkeAktivertException
//import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
//import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
//import no.nav.sosialhjelp.soknad.app.exceptions.SikkerhetsBegrensningException
//import no.nav.sosialhjelp.soknad.app.exceptions.SoknadUnderArbeidIkkeFunnetException
//import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
//import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
//import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
//import no.nav.sosialhjelp.soknad.pdf.PdfGenereringException
//import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggService.Companion.MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB
//import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
//import no.nav.sosialhjelp.soknad.vedlegg.exceptions.SamletVedleggStorrelseForStorException
//import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import javax.ws.rs.core.MediaType
//import javax.ws.rs.core.Response
//import javax.ws.rs.core.Response.ResponseBuilder
//import javax.ws.rs.ext.ExceptionMapper
//
////@Provider
//@Component
//class ApplicationExceptionMapper : ExceptionMapper<SosialhjelpSoknadApiException> {
//    override fun toResponse(e: SosialhjelpSoknadApiException): Response {
//        val response: ResponseBuilder
//        when (e) {
//            is UgyldigOpplastingTypeException -> {
//                response = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
//                logger.warn("Feilet opplasting", e)
//            }
//            is OpplastingException -> {
//                response = Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE)
//                logger.warn("Feilet opplasting", e)
//            }
//            is SamletVedleggStorrelseForStorException -> {
//                response = Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE)
//                logger.warn("Feilet opplasting. Valgt fil for opplasting gjør at grensen for samlet vedleggstørrelse på ${MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB}MB overskrides.", e)
//            }
//            is AuthorizationException -> {
//                response = Response.status(Response.Status.FORBIDDEN)
//                logger.warn("Ikke tilgang til ressurs", e)
//                return response.type(MediaType.APPLICATION_JSON).entity(Feilmelding(e.id, "Ikke tilgang til ressurs"))
//                    .build()
//            }
//            is IkkeFunnetException -> {
//                response = Response.status(Response.Status.NOT_FOUND)
//                logger.warn("Fant ikke ressurs", e)
//            }
//            is EttersendelseSendtForSentException -> {
//                response = Response.serverError().header(Feilmelding.NO_BIGIP_5XX_REDIRECT, true)
//                logger.info("REST-kall feilet: ${e.message}", e)
//            }
//            is TjenesteUtilgjengeligException -> {
//                response = Response.serverError().header(Feilmelding.NO_BIGIP_5XX_REDIRECT, true)
//                logger.warn("REST-kall feilet: Ekstern tjeneste er utilgjengelig", e)
//            }
//            is SikkerhetsBegrensningException -> {
//                response = Response.serverError().header(Feilmelding.NO_BIGIP_5XX_REDIRECT, true)
//                logger.warn("REST-kall feilet: Sikkerhetsbegrensning", e)
//            }
//            is SendingTilKommuneErMidlertidigUtilgjengeligException -> {
//                logger.error(e.message, e)
//                return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON).entity(
//                    Feilmelding(
//                        "innsending_midlertidig_utilgjengelig",
//                        "Tjenesten er midlertidig utilgjengelig hos kommunen"
//                    )
//                ).build()
//            }
//            is SendingTilKommuneErIkkeAktivertException -> {
//                logger.error(e.message, e)
//                return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON).entity(
//                    Feilmelding("innsending_ikke_aktivert", "Tjenesten er ikke aktivert hos kommunen")
//                ).build()
//            }
//            is SendingTilKommuneUtilgjengeligException -> {
//                logger.error(e.message, e)
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(
//                    Feilmelding("innsending_ikke_tilgjengelig", "Tjenesten er midlertidig ikke tilgjengelig")
//                ).build()
//            }
//            is SoknadenHarNedetidException -> {
//                logger.warn(e.message, e)
//                return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON).entity(
//                    Feilmelding("nedetid", "Søknaden har planlagt nedetid nå")
//                ).build()
//            }
//            is PdfGenereringException -> {
//                logger.error(e.message, e)
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(
//                    Feilmelding("pdf_generering", "Innsending av søknad eller ettersendelse feilet")
//                ).build()
//            }
//            is SoknadUnderArbeidIkkeFunnetException -> {
//                logger.warn(e.message, e)
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(
//                    Feilmelding("unexpected_error", "Noe uventet feilet.")
//                ).build()
//            }
//            is PdlApiException -> {
//                response = Response.serverError().header(Feilmelding.NO_BIGIP_5XX_REDIRECT, true)
//                logger.error("Kall til PDL feilet", e)
//            }
//            else -> {
//                response = Response.serverError().header(Feilmelding.NO_BIGIP_5XX_REDIRECT, true)
//                logger.error("REST-kall feilet", e)
//            }
//        }
//
//        // Mediatypen kan settes til APPLICATION_JSON når vi ikke trenger å støtte IE9 lenger.
//        return response.type(MediaType.TEXT_PLAIN).entity(Feilmelding(e.id, e.message)).build()
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(ApplicationExceptionMapper::class.java)
//    }
//}
