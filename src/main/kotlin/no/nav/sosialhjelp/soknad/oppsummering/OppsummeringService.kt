package no.nav.sosialhjelp.soknad.oppsummering

import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.oppsummering.dto.Oppsummering
import no.nav.sosialhjelp.soknad.oppsummering.steg.ArbeidOgUtdanningSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.BegrunnelseSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.BosituasjonSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.FamiliesituasjonSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.InntektOgFormueSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.OkonomiskeOpplysningerOgVedleggSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.PersonopplysningerSteg
import no.nav.sosialhjelp.soknad.oppsummering.steg.UtgifterOgGjeldSteg

class OppsummeringService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository
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
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, fnr)
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
        return Oppsummering(
            listOf(
                personopplysningerSteg.get(jsonInternalSoknad),
                begrunnelseSteg.get(jsonInternalSoknad),
                arbeidOgUtdanningSteg.get(jsonInternalSoknad),
                familiesituasjonSteg.get(jsonInternalSoknad),
                bosituasjonSteg.get(jsonInternalSoknad),
                inntektOgFormueSteg.get(jsonInternalSoknad),
                utgifterOgGjeldSteg.get(jsonInternalSoknad),
                okonomiskeOpplysningerOgVedleggSteg.get(jsonInternalSoknad, opplastedeVedlegg)
            )
        )
    }

}
