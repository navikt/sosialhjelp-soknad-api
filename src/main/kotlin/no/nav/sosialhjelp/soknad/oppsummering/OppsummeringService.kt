package no.nav.sosialhjelp.soknad.oppsummering

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
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
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentlagerService
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OppsummeringService(
    private val dokumentlagerService: DokumentlagerService,
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
        soknadId: UUID,
    ): Oppsummering {
        return useActiveJsonInternalSoknad(soknadId).let {
            when (it.soknad.data.soknadstype) {
                JsonData.Soknadstype.KORT -> kortSoknadOppsummering(it)
                else -> soknadOppsummering(it, getVedleggInfo(soknadId))
            }
        }
    }

    private fun useActiveJsonInternalSoknad(soknadId: UUID): JsonInternalSoknad {
        return jsonGenerator.createJsonInternalSoknad(soknadId)
    }

    private fun getVedleggInfo(soknadId: UUID) =
        dokumentlagerService.getAllDokumenterMetadata(soknadId).map {
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
}
