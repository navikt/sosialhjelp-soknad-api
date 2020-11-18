package no.nav.sbl.dialogarena.rest.feil;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.EttersendelseSendtForSentException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.IkkeFunnetException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SamletVedleggStorrelseForStorException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.PdlApiException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.sosialhjelp.SendingTilKommuneErIkkeAktivertException;
import no.nav.sbl.sosialhjelp.SendingTilKommuneErMidlertidigUtilgjengeligException;
import no.nav.sbl.sosialhjelp.SendingTilKommuneUtilgjengeligException;
import no.nav.sbl.sosialhjelp.SoknadenHarNedetidException;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenereringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.serverError;
import static javax.ws.rs.core.Response.status;
import static no.nav.sbl.dialogarena.rest.feil.Feilmelding.NO_BIGIP_5XX_REDIRECT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.OpplastetVedleggService.MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<SosialhjelpSoknadApiException> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationExceptionMapper.class);

    @Override
    public Response toResponse(SosialhjelpSoknadApiException e) {
        Response.ResponseBuilder response;
        if (e instanceof UgyldigOpplastingTypeException) {
            response = status(UNSUPPORTED_MEDIA_TYPE);
            logger.warn("Feilet opplasting", e);
        } else if (e instanceof OpplastingException) {
            response = status(REQUEST_ENTITY_TOO_LARGE);
            logger.warn("Feilet opplasting", e);
        } else if (e instanceof SamletVedleggStorrelseForStorException) {
            response = status(REQUEST_ENTITY_TOO_LARGE);
            logger.warn("Feilet opplasting. Valgt fil for opplasting gjør at grensen for samlet vedleggstørrelse på " + MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB + "MB overskrides.", e);
        } else if (e instanceof AuthorizationException) {
            response = status(FORBIDDEN);
            logger.warn("Ikke tilgang til ressurs", e);
            return response.type(APPLICATION_JSON).entity(new Feilmelding(e.getId(), "Ikke tilgang til ressurs")).build();
        } else if (e instanceof IkkeFunnetException) {
            response = status(NOT_FOUND);
            logger.warn("Fant ikke ressurs", e);
        } else if (e instanceof EttersendelseSendtForSentException) {
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
            logger.info("REST-kall feilet: " + e.getMessage(), e);
        } else if (e instanceof TjenesteUtilgjengeligException) {
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
            logger.warn("REST-kall feilet: Ekstern tjeneste er utilgjengelig", e);
        } else if (e instanceof SikkerhetsBegrensningException) {
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
            logger.warn("REST-kall feilet: Sikkerhetsbegrensning", e);
        } else if (e instanceof SendingTilKommuneErMidlertidigUtilgjengeligException) {
            logger.error(e.getMessage(), e);
            return status(SERVICE_UNAVAILABLE).type(APPLICATION_JSON).entity(new Feilmelding("innsending_midlertidig_utilgjengelig", "Tjenesten er midlertidig utilgjengelig hos kommunen")).build();
        } else if (e instanceof SendingTilKommuneErIkkeAktivertException) {
            logger.error(e.getMessage(), e);
            return status(SERVICE_UNAVAILABLE).type(APPLICATION_JSON).entity(new Feilmelding("innsending_ikke_aktivert", "Tjenesten er ikke aktivert hos kommunen")).build();
        } else if (e instanceof SendingTilKommuneUtilgjengeligException) {
            logger.error(e.getMessage(), e);
            return status(SERVICE_UNAVAILABLE).type(APPLICATION_JSON).entity(new Feilmelding("innsending_ikke_tilgjengelig", "Tjenesten er midlertidig ikke tilgjengelig")).build();
        }else if (e instanceof SoknadenHarNedetidException) {
            logger.warn(e.getMessage(), e);
            return status(SERVICE_UNAVAILABLE).type(APPLICATION_JSON).entity(new Feilmelding("nedetid", "Søknaden har planlagt nedetid nå")).build();
        } else if (e instanceof PdfGenereringException) {
            logger.error(e.getMessage(), e);
            return status(INTERNAL_SERVER_ERROR).type(APPLICATION_JSON).entity(new Feilmelding("pdf_generering", "Innsending av søknad eller ettersendelse feilet")).build();
        } else if (e instanceof PdlApiException){
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
            logger.warn("Kall til PDL feilet", e);
        } else {
            response = serverError().header(NO_BIGIP_5XX_REDIRECT, true);
            logger.error("REST-kall feilet", e);
        }

        // Mediatypen kan settes til APPLICATION_JSON når vi ikke trenger å støtte IE9 lenger.
        return response.type(TEXT_PLAIN).entity(new Feilmelding(e.getId(), e.getMessage())).build();
    }
}
