package no.nav.sosialhjelp.soknad.oppsummering

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
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
import no.nav.sosialhjelp.soknad.oppsummering.steg.kort.ArbeidOgFamilieSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.kort.BehovSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.kort.SituasjonsendringSteg
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OppsummeringService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val mellomlagringService: MellomlagringService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val jsonGenerator: JsonInternalSoknadGenerator,
) {
    private val personopplysningerSteg = PersonopplysningerSteg()
    private val begrunnelseSteg = BegrunnelseSteg()
    private val arbeidOgUtdanningSteg = ArbeidOgUtdanningSteg()
    private val familiesituasjonSteg = FamiliesituasjonSteg()
    private val bosituasjonSteg = BosituasjonSteg()
    private val inntektOgFormueSteg = InntektOgFormueSteg()
    private val utgifterOgGjeldSteg = UtgifterOgGjeldSteg()
    private val okonomiskeOpplysningerOgVedleggSteg = OkonomiskeOpplysningerOgVedleggSteg()
    private val situasjonsendringSteg = SituasjonsendringSteg()
    private val arbeidOgFamilieSteg = ArbeidOgFamilieSteg()
    private val behovSteg = BehovSteg()

    fun hentOppsummering(
        fnr: String,
        behandlingsId: String,
    ): Oppsummering {
        val jsonInternalSoknad =
            if (ControllerToNewDatamodellProxy.nyDatamodellAktiv) {
                jsonGenerator.createJsonInternalSoknad(UUID.fromString(behandlingsId))
            } else {
                val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, fnr)
                val jsonInternalSoknad =
                    soknadUnderArbeid.jsonInternalSoknad
                        ?: throw IllegalStateException("Kan ikke generere oppsummeringsside hvis SoknadUnderArbeid.jsonInternalSoknad er null")

                if (soknadUnderArbeid.jsonInternalSoknad?.vedlegg?.vedlegg?.isEmpty() == null) {
                    log.info("Oppdaterer vedleggsforventninger for soknad $behandlingsId fra oppsummeringssiden, ettersom side 8 ble hoppet over")
                }
                jsonInternalSoknad
            }

        return if (jsonInternalSoknad.soknad.data.soknadstype == JsonData.Soknadstype.KORT) {
            kortSoknadOppsummering(jsonInternalSoknad)
        } else {
            soknadOppsummering(jsonInternalSoknad, getVedleggInfo(behandlingsId))
        }
    }

    private fun getVedleggInfo(behandlingsId: String) =
        mellomlagringService.getAllVedlegg(behandlingsId).map {
            OppsummeringVedleggInfo(it.filnavn, it.filId)
        }

    private fun kortSoknadOppsummering(json: JsonInternalSoknad): Oppsummering {
        return Oppsummering(
            listOf(
                personopplysningerSteg.get(json),
                behovSteg.get(json),
                arbeidOgFamilieSteg.get(json),
                situasjonsendringSteg.get(json),
            ),
        )
    }

    private fun soknadOppsummering(
        json: JsonInternalSoknad,
        vedleggInfo: List<OppsummeringVedleggInfo>,
    ): Oppsummering {
        return Oppsummering(
            listOf(
                personopplysningerSteg.get(json),
                begrunnelseSteg.get(json),
                arbeidOgUtdanningSteg.get(json),
                familiesituasjonSteg.get(json),
                bosituasjonSteg.get(json),
                inntektOgFormueSteg.get(json),
                utgifterOgGjeldSteg.get(json),
                okonomiskeOpplysningerOgVedleggSteg.get(json, vedleggInfo),
            ),
        )
    }

    companion object {
        private val log = getLogger(OppsummeringService::class.java)
    }
}
