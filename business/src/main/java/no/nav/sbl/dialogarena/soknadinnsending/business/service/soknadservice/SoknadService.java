package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SKJEMANUMMER;

@Component
public class SoknadService {

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private EttersendingService ettersendingService;

    @Inject
    private SoknadDataFletter soknadDataFletter;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Transactional
    public String startSoknad(String skjemanummer) {
        return soknadDataFletter.startSoknad(skjemanummer);
    }

    @Transactional
    public void avbrytSoknad(String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        if (soknadUnderArbeidOptional.isPresent()){
            soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeidOptional.get(), eier);
            henvendelseService.avbrytSoknad(soknadUnderArbeidOptional.get().getBehandlingsId(), false);
            soknadMetricsService.avbruttSoknad(SKJEMANUMMER, soknadUnderArbeidOptional.get().erEttersendelse());
        }
    }

    public String startEttersending(String behandlingsIdSoknad) {
        return ettersendingService.start(behandlingsIdSoknad);
    }

    @Transactional
    public void sendSoknad(String behandlingsId) {
        soknadDataFletter.sendSoknad(behandlingsId);
    }
}