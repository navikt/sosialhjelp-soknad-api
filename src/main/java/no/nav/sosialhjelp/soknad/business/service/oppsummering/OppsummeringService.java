package no.nav.sosialhjelp.soknad.business.service.oppsummering;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.ArbeidOgUtdanningSteg;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.BegrunnelseSteg;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.BosituasjonSteg;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.FamiliesituasjonSteg;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.InntektOgFormueSteg;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OkonomiskeOpplysningerOgVedleggSteg;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.PersonopplysningerSteg;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.UtgifterOgGjeldSteg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Oppsummering;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
public class OppsummeringService {

    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    private final PersonopplysningerSteg personopplysningerSteg;
    private final BegrunnelseSteg begrunnelseSteg;
    private final ArbeidOgUtdanningSteg arbeidOgUtdanningSteg;
    private final FamiliesituasjonSteg familiesituasjonSteg;
    private final BosituasjonSteg bosituasjonSteg;
    private final InntektOgFormueSteg inntektOgFormueSteg;
    private final UtgifterOgGjeldSteg utgifterOgGjeldSteg;
    private final OkonomiskeOpplysningerOgVedleggSteg okonomiskeOpplysningerOgVedleggSteg;

    public OppsummeringService(
            SoknadUnderArbeidRepository soknadUnderArbeidRepository
    ) {
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;

        this.personopplysningerSteg = new PersonopplysningerSteg();
        this.begrunnelseSteg = new BegrunnelseSteg();
        this.arbeidOgUtdanningSteg = new ArbeidOgUtdanningSteg();
        this.familiesituasjonSteg = new FamiliesituasjonSteg();
        this.bosituasjonSteg = new BosituasjonSteg();
        this.inntektOgFormueSteg = new InntektOgFormueSteg();
        this.utgifterOgGjeldSteg = new UtgifterOgGjeldSteg();
        this.okonomiskeOpplysningerOgVedleggSteg = new OkonomiskeOpplysningerOgVedleggSteg();
    }

    public Oppsummering hentOppsummering(String fnr, String behandlingsId) {
        var soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, fnr);
        var jsonInternalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        return new Oppsummering(
                Arrays.asList(
                        personopplysningerSteg.get(jsonInternalSoknad),
                        begrunnelseSteg.get(jsonInternalSoknad),
//                        arbeidOgUtdanningSteg.get(jsonInternalSoknad),
//                        familiesituasjonSteg.get(jsonInternalSoknad),
//                        bosituasjonSteg.get(jsonInternalSoknad),
//                        inntektOgFormueSteg.get(jsonInternalSoknad),
                        utgifterOgGjeldSteg.get(jsonInternalSoknad)//,
//                        okonomiskeOpplysningerOgVedleggSteg.get(jsonInternalSoknad)
                ));
    }


}
