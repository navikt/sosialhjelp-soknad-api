package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class SlettSoknadScheduler {

    @Inject
    private SoknadRepository soknadRepository;
    @Inject
    private FillagerService fillagerService;
    @Inject
    private HenvendelseService henvendelseService;



    public void settSoknadAvsluttet(WebSoknad soknad) {
        soknadRepository.slettSoknad(soknad, HendelseType.SOKNAD_SLETTET);
    }
}
