package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag.Verdi
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBorSammenMed
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addInntektIfNotPresentInOversikt
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addUtgiftIfNotPresentInOversikt
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.removeInntektIfPresentInOversikt
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.removeUtgiftIfPresentInOversikt
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.fulltNavn
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.getPersonnummerFromFnr
import no.nav.sosialhjelp.soknad.personalia.familie.dto.AnsvarFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.BarnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/familie/forsorgerplikt")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class ForsorgerpliktRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val textService: TextService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GET
    open fun hentForsorgerplikt(@PathParam("behandlingsId") behandlingsId: String): ForsorgerpliktFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        val jsonForsorgerplikt = soknad.soknad.data.familie.forsorgerplikt

        return mapToForsorgerpliktFrontend(jsonForsorgerplikt)
    }

    @PUT
    open fun updateForsorgerplikt(
        @PathParam("behandlingsId") behandlingsId: String,
        forsorgerpliktFrontend: ForsorgerpliktFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val forsorgerplikt = soknad.jsonInternalSoknad.soknad.data.familie.forsorgerplikt

        updateBarnebidrag(forsorgerpliktFrontend, soknad, forsorgerplikt)
        updateAnsvarAndHarForsorgerplikt(forsorgerpliktFrontend, soknad, forsorgerplikt)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun updateBarnebidrag(
        forsorgerpliktFrontend: ForsorgerpliktFrontend,
        soknad: SoknadUnderArbeid,
        forsorgerplikt: JsonForsorgerplikt
    ) {
        val barnebidragType = "barnebidrag"
        val oversikt = soknad.jsonInternalSoknad.soknad.data.okonomi.oversikt
        val inntekter = oversikt.inntekt
        val utgifter = oversikt.utgift
        if (forsorgerpliktFrontend.barnebidrag != null) {
            if (forsorgerplikt.barnebidrag == null) {
                forsorgerplikt.barnebidrag =
                    JsonBarnebidrag().withKilde(JsonKildeBruker.BRUKER).withVerdi(forsorgerpliktFrontend.barnebidrag)
            } else {
                forsorgerplikt.barnebidrag.verdi = forsorgerpliktFrontend.barnebidrag
            }
            val tittelMottar = textService.getJsonOkonomiTittel("opplysninger.familiesituasjon.barnebidrag.mottar")
            val tittelBetaler = textService.getJsonOkonomiTittel("opplysninger.familiesituasjon.barnebidrag.betaler")
            when (forsorgerpliktFrontend.barnebidrag) {
                Verdi.BEGGE -> {
                    addInntektIfNotPresentInOversikt(inntekter, barnebidragType, tittelMottar)
                    addUtgiftIfNotPresentInOversikt(utgifter, barnebidragType, tittelBetaler)
                }
                Verdi.BETALER -> {
                    removeInntektIfPresentInOversikt(inntekter, barnebidragType)
                    addUtgiftIfNotPresentInOversikt(utgifter, barnebidragType, tittelBetaler)
                }
                Verdi.MOTTAR -> {
                    addInntektIfNotPresentInOversikt(inntekter, barnebidragType, tittelMottar)
                    removeUtgiftIfPresentInOversikt(utgifter, barnebidragType)
                }
                Verdi.INGEN -> {
                    removeInntektIfPresentInOversikt(inntekter, barnebidragType)
                    removeUtgiftIfPresentInOversikt(utgifter, barnebidragType)
                }
            }
        } else {
            forsorgerplikt.barnebidrag = null
            removeInntektIfPresentInOversikt(inntekter, barnebidragType)
            removeUtgiftIfPresentInOversikt(utgifter, barnebidragType)
        }
    }

    private fun updateAnsvarAndHarForsorgerplikt(
        forsorgerpliktFrontend: ForsorgerpliktFrontend,
        soknad: SoknadUnderArbeid,
        forsorgerplikt: JsonForsorgerplikt
    ) {
        val systemAnsvar: MutableList<JsonAnsvar> =
            if (forsorgerplikt.ansvar == null) mutableListOf() else forsorgerplikt.ansvar.filter { it.barn.kilde == JsonKilde.SYSTEM }.toMutableList()
        if (forsorgerpliktFrontend.ansvar != null) {
            for (ansvarFrontend in forsorgerpliktFrontend.ansvar) {
                for (ansvar in systemAnsvar) {
                    if (ansvar.barn.personIdentifikator == ansvarFrontend?.barn?.fodselsnummer) {
                        setBorSammenDeltBostedAndSamvarsgrad(ansvarFrontend, ansvar)
                    }
                }
            }
        }
        if (forsorgerplikt.harForsorgerplikt != null && forsorgerplikt.harForsorgerplikt.kilde == JsonKilde.BRUKER) {
            forsorgerplikt.harForsorgerplikt = JsonHarForsorgerplikt().withKilde(JsonKilde.SYSTEM).withVerdi(false)
            removeBarneutgifterFromSoknad(soknad)
        }
        forsorgerplikt.ansvar = if (systemAnsvar.isEmpty()) null else systemAnsvar
    }

    private fun setBorSammenDeltBostedAndSamvarsgrad(ansvarFrontend: AnsvarFrontend?, ansvar: JsonAnsvar) {
        ansvar.borSammenMed =
            if (ansvarFrontend?.borSammenMed == null) null else JsonBorSammenMed()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(ansvarFrontend.borSammenMed)
        ansvar.harDeltBosted =
            if (ansvarFrontend?.harDeltBosted == null) null else JsonHarDeltBosted()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(ansvarFrontend.harDeltBosted)
        ansvar.samvarsgrad =
            if (ansvarFrontend?.samvarsgrad == null) null else JsonSamvarsgrad()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(ansvarFrontend.samvarsgrad)
    }

    private fun removeBarneutgifterFromSoknad(soknad: SoknadUnderArbeid) {
        val okonomi = soknad.jsonInternalSoknad.soknad.data.okonomi
        val opplysningerBarneutgifter = okonomi.opplysninger.utgift
        val oversiktBarneutgifter = okonomi.oversikt.utgift
        okonomi.opplysninger.bekreftelse.removeIf { bekreftelse: JsonOkonomibekreftelse -> bekreftelse.type == "barneutgifter" }
        removeUtgiftIfPresentInOversikt(oversiktBarneutgifter, "barnehage")
        removeUtgiftIfPresentInOversikt(oversiktBarneutgifter, "sfo")
        OkonomiMapper.removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "barnFritidsaktiviteter")
        OkonomiMapper.removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "barnTannregulering")
        OkonomiMapper.removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "annenBarneutgift")
    }

    private fun mapToForsorgerpliktFrontend(jsonForsorgerplikt: JsonForsorgerplikt): ForsorgerpliktFrontend {
        val ansvar: List<AnsvarFrontend?>? =
            if (jsonForsorgerplikt.ansvar == null) null else jsonForsorgerplikt.ansvar
                .filter { it.barn.kilde == JsonKilde.SYSTEM }
                .map { mapToAnsvarFrontend(it) }

        return ForsorgerpliktFrontend(
            harForsorgerplikt = if (jsonForsorgerplikt.harForsorgerplikt == null) null else jsonForsorgerplikt.harForsorgerplikt.verdi,
            barnebidrag = if (jsonForsorgerplikt.barnebidrag == null) null else jsonForsorgerplikt.barnebidrag.verdi,
            ansvar = ansvar
        )
    }

    private fun mapToAnsvarFrontend(jsonAnsvar: JsonAnsvar?): AnsvarFrontend? {
        return if (jsonAnsvar == null) {
            null
        } else AnsvarFrontend(
            barn = mapToBarnFrontend(jsonAnsvar.barn),
            borSammenMed = if (jsonAnsvar.borSammenMed == null) null else jsonAnsvar.borSammenMed.verdi,
            erFolkeregistrertSammen = if (jsonAnsvar.erFolkeregistrertSammen == null) null else jsonAnsvar.erFolkeregistrertSammen.verdi,
            harDeltBosted = if (jsonAnsvar.harDeltBosted == null) null else jsonAnsvar.harDeltBosted.verdi,
            samvarsgrad = if (jsonAnsvar.samvarsgrad == null) null else jsonAnsvar.samvarsgrad.verdi
        )
    }

    private fun mapToBarnFrontend(barn: JsonBarn?): BarnFrontend? {
        return if (barn == null) {
            null
        } else BarnFrontend(
            navn = NavnFrontend(barn.navn.fornavn, barn.navn.mellomnavn, barn.navn.etternavn, fulltNavn(barn.navn)),
            fodselsdato = barn.fodselsdato,
            personnummer = getPersonnummerFromFnr(barn.personIdentifikator),
            fodselsnummer = barn.personIdentifikator
        )
    }
}
