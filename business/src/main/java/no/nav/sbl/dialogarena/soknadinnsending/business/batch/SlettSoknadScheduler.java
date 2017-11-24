package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.SOKNAD_SLETTET;

@Service
public class SlettSoknadScheduler {

    @Inject
    private SoknadRepository soknadRepository;
    @Inject
    private FillagerService fillagerService;
    @Inject
    private HenvendelseService henvendelseService;



    public void settSoknadAvsluttet(String behandlingsId) {
        soknadRepository.insertHendelse(behandlingsId,SOKNAD_SLETTET.name());
    }
}
