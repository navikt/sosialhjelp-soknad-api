package no.nav.sosialhjelp.soknad.oppsummering

import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.oppsummering.dto.Oppsummering
import no.nav.sosialhjelp.soknad.oppsummering.steg.ArbeidOgUtdanningSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.BegrunnelseSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.BosituasjonSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.FamiliesituasjonSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.InntektOgFormueSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.OkonomiskeOpplysningerOgVedleggSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.OkonomiskeOpplysningerOgVedleggSteg.OppsummeringVedleggInfo
import no.nav.sosialhjelp.soknad.oppsummering.steg.PersonopplysningerSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.UtgifterOgGjeldSteg
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import java.util.function.Predicate

@Component
class OppsummeringService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val mellomlagringService: MellomlagringService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
) {
    private val personopplysningerSteg = PersonopplysningerSteg()
    private val begrunnelseSteg = BegrunnelseSteg()
    private val arbeidOgUtdanningSteg = ArbeidOgUtdanningSteg()
    private val familiesituasjonSteg = FamiliesituasjonSteg()
    private val bosituasjonSteg = BosituasjonSteg()
    private val inntektOgFormueSteg = InntektOgFormueSteg()
    private val utgifterOgGjeldSteg = UtgifterOgGjeldSteg()
    private val okonomiskeOpplysningerOgVedleggSteg = OkonomiskeOpplysningerOgVedleggSteg()

    fun hentOppsummering(fnr: String, behandlingsId: String): Oppsummering {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, fnr)
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke generere oppsummeringsside hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        val skalSendesMedDigisosApi = soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(behandlingsId)
        if (soknadUnderArbeid.jsonInternalSoknad?.vedlegg?.vedlegg?.isEmpty() == null) {
            log.info("Oppdaterer vedleggsforventninger for soknad $behandlingsId fra oppsummeringssiden, ettersom side 8 ble hoppet over")
            if (skalSendesMedDigisosApi) {
                // todo: oppdater vedleggsforventninger ut fra mellomlagrede vedlegg?
            } else {
                oppdaterVedleggsforventninger(soknadUnderArbeid, fnr)
            }
        }

        val vedleggInfo = if (skalSendesMedDigisosApi) {
            mellomlagringService.getAllVedlegg(behandlingsId).map {
                OppsummeringVedleggInfo(it.filnavn, it.filId)
            }
        } else {
            opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, fnr).map {
                OppsummeringVedleggInfo(it.filnavn, it.uuid)
            }
        }

        return Oppsummering(
            listOf(
                personopplysningerSteg.get(jsonInternalSoknad),
                begrunnelseSteg.get(jsonInternalSoknad),
                arbeidOgUtdanningSteg.get(jsonInternalSoknad),
                familiesituasjonSteg.get(jsonInternalSoknad),
                bosituasjonSteg.get(jsonInternalSoknad),
                inntektOgFormueSteg.get(jsonInternalSoknad),
                utgifterOgGjeldSteg.get(jsonInternalSoknad),
                okonomiskeOpplysningerOgVedleggSteg.get(jsonInternalSoknad, vedleggInfo)
            )
        )
    }

    private fun oppdaterVedleggsforventninger(soknadUnderArbeid: SoknadUnderArbeid, eier: String) {
        val jsonVedleggs = JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
        val paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknadUnderArbeid.jsonInternalSoknad)
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, eier)

        fjernIkkePaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg, opplastedeVedlegg)

        jsonVedleggs.addAll(
            paakrevdeVedlegg
                .filter { isNotInList(jsonVedleggs).test(it) }
                .map {
                    it
                        .withStatus(Vedleggstatus.VedleggKreves.toString())
                        .withHendelseType(JsonVedlegg.HendelseType.SOKNAD)
                }
        )

        soknadUnderArbeid.jsonInternalSoknad?.vedlegg = JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
    }

    private fun fjernIkkePaakrevdeVedlegg(
        jsonVedleggs: MutableList<JsonVedlegg>,
        paakrevdeVedlegg: List<JsonVedlegg>,
        opplastedeVedlegg: List<OpplastetVedlegg>
    ) {
        val ikkeLengerPaakrevdeVedlegg = jsonVedleggs.filter { isNotInList(paakrevdeVedlegg).test(it) }.toMutableList()

        excludeTypeAnnetAnnetFromList(ikkeLengerPaakrevdeVedlegg)
        jsonVedleggs.removeAll(ikkeLengerPaakrevdeVedlegg)
        for (ikkePaakrevdVedlegg in ikkeLengerPaakrevdeVedlegg) {
            for (oVedlegg in opplastedeVedlegg) {
                if (isSameType(ikkePaakrevdVedlegg, oVedlegg)) {
                    opplastetVedleggRepository.slettVedlegg(oVedlegg.uuid, oVedlegg.eier)
                }
            }
        }
    }

    private fun isNotInList(jsonVedleggs: List<JsonVedlegg>): Predicate<JsonVedlegg> {
        return Predicate<JsonVedlegg> { v: JsonVedlegg ->
            jsonVedleggs.none { it.type == v.type && it.tilleggsinfo == v.tilleggsinfo }
        }
    }

    private fun excludeTypeAnnetAnnetFromList(jsonVedleggs: MutableList<JsonVedlegg>) {
        jsonVedleggs.removeAll(
            jsonVedleggs.filter { it.type == "annet" && it.tilleggsinfo == "annet" }
        )
    }

    private fun isSameType(jsonVedlegg: JsonVedlegg, opplastetVedlegg: OpplastetVedlegg): Boolean {
        return opplastetVedlegg.vedleggType.sammensattType == jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo
    }

    companion object {
        private val log = getLogger(OppsummeringService::class.java)
    }
}
