package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
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
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addInntektIfNotPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.addUtgiftIfNotPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeInntektIfPresentInOversikt
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.removeUtgiftIfPresentInOversikt
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.fulltNavn
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.getPersonnummerFromFnr
import no.nav.sosialhjelp.soknad.personalia.familie.dto.AnsvarFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.BarnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/familie/forsorgerplikt", produces = [MediaType.APPLICATION_JSON_VALUE])
class ForsorgerpliktRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val textService: TextService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GetMapping
    fun hentForsorgerplikt(
        @PathVariable("behandlingsId") behandlingsId: String
    ): ForsorgerpliktFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = eier()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val jsonForsorgerplikt = soknad.soknad.data.familie.forsorgerplikt

        return mapToForsorgerpliktFrontend(jsonForsorgerplikt)
    }

    @PutMapping
    fun updateForsorgerplikt(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody forsorgerpliktFrontend: ForsorgerpliktFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val forsorgerplikt = jsonInternalSoknad.soknad.data.familie.forsorgerplikt

        updateBarnebidrag(forsorgerpliktFrontend, jsonInternalSoknad, forsorgerplikt)
        updateAnsvarAndHarForsorgerplikt(forsorgerpliktFrontend, jsonInternalSoknad, forsorgerplikt)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun updateBarnebidrag(
        forsorgerpliktFrontend: ForsorgerpliktFrontend,
        jsonInternalSoknad: JsonInternalSoknad,
        forsorgerplikt: JsonForsorgerplikt
    ) {
        val barnebidragType = "barnebidrag"
        val oversikt = jsonInternalSoknad.soknad.data.okonomi.oversikt
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
        jsonInternalSoknad: JsonInternalSoknad,
        forsorgerplikt: JsonForsorgerplikt
    ) {
        val systemAnsvar: List<JsonAnsvar> =
            when (forsorgerplikt.ansvar) {
                null -> listOf()
                else -> forsorgerplikt.ansvar.filter { it.barn.kilde == JsonKilde.SYSTEM }.toList()
            }

        for (ansvarFrontend in forsorgerpliktFrontend.ansvar) {
            for (ansvar in systemAnsvar) {
                if (ansvar.barn.personIdentifikator == ansvarFrontend.barn?.fodselsnummer) {
                    setBorSammenDeltBostedAndSamvarsgrad(ansvarFrontend, ansvar)
                }
            }
        }

        if (forsorgerplikt.harForsorgerplikt != null && forsorgerplikt.harForsorgerplikt.kilde == JsonKilde.BRUKER) {
            forsorgerplikt.harForsorgerplikt = JsonHarForsorgerplikt().withKilde(JsonKilde.SYSTEM).withVerdi(false)
            removeBarneutgifterFromSoknad(jsonInternalSoknad)
        }

        forsorgerplikt.ansvar = systemAnsvar.takeIf { it.isNotEmpty() }
    }

    private fun setBorSammenDeltBostedAndSamvarsgrad(ansvarFrontend: AnsvarFrontend?, ansvar: JsonAnsvar) {
        ansvar.borSammenMed = ansvarFrontend?.borSammenMed?.let {
            JsonBorSammenMed()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(it)
        }
        ansvar.harDeltBosted = ansvarFrontend?.harDeltBosted?.let {
            JsonHarDeltBosted()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(it)
        }
        ansvar.samvarsgrad = ansvarFrontend?.samvarsgrad?.let {
            JsonSamvarsgrad()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(it)
        }
    }

    private fun removeBarneutgifterFromSoknad(jsonInternalSoknad: JsonInternalSoknad) {
        val okonomi = jsonInternalSoknad.soknad.data.okonomi
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
        val ansvar: List<AnsvarFrontend> = jsonForsorgerplikt.ansvar
            ?.filter { it.barn.kilde == JsonKilde.SYSTEM }
            ?.filterNotNull()
            ?.map { mapToAnsvarFrontend(it) }
            .orEmpty()

        return ForsorgerpliktFrontend(
            harForsorgerplikt = jsonForsorgerplikt.harForsorgerplikt?.verdi,
            barnebidrag = jsonForsorgerplikt.barnebidrag?.verdi,
            ansvar = ansvar
        )
    }

    private fun mapToAnsvarFrontend(jsonAnsvar: JsonAnsvar): AnsvarFrontend = AnsvarFrontend(
        barn = jsonAnsvar.barn?.let { mapToBarnFrontend(it) },
        borSammenMed = jsonAnsvar.borSammenMed?.verdi,
        erFolkeregistrertSammen = jsonAnsvar.erFolkeregistrertSammen?.verdi,
        harDeltBosted = jsonAnsvar.harDeltBosted?.verdi,
        samvarsgrad = jsonAnsvar.samvarsgrad?.verdi
    )

    private fun mapToBarnFrontend(barn: JsonBarn): BarnFrontend = BarnFrontend(
        navn = NavnFrontend(barn.navn.fornavn, barn.navn.mellomnavn, barn.navn.etternavn, fulltNavn(barn.navn)),
        fodselsdato = barn.fodselsdato,
        personnummer = getPersonnummerFromFnr(barn.personIdentifikator),
        fodselsnummer = barn.personIdentifikator
    )
}
