package no.nav.sosialhjelp.soknad.oppsummering

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
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
class OppsummeringService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
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

    fun hentOppsummering(
        fnr: String,
        behandlingsId: String,
    ): Oppsummering {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, fnr)
        val jsonInternalSoknad =
            soknadUnderArbeid.jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke generere oppsummeringsside hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        val skalSendesMedDigisosApi = soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(behandlingsId)
        if (soknadUnderArbeid.jsonInternalSoknad?.vedlegg?.vedlegg?.isEmpty() == null) {
            log.info("Oppdaterer vedleggsforventninger for soknad $behandlingsId fra oppsummeringssiden, ettersom side 8 ble hoppet over")
            if (skalSendesMedDigisosApi) {
                // todo: oppdater vedleggsforventninger ut fra mellomlagrede vedlegg?
            }
        }

        val vedleggInfo = mellomlagringService.getAllVedlegg(behandlingsId).map {
            OppsummeringVedleggInfo(it.filnavn, it.filId)
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
                okonomiskeOpplysningerOgVedleggSteg.get(jsonInternalSoknad, vedleggInfo),
            ),
        )
    }

    companion object {
        private val log = getLogger(OppsummeringService::class.java)
    }
}
