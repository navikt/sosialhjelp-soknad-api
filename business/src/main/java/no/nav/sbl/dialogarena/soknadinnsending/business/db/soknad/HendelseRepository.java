package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.List;

public interface HendelseRepository {

    void registrerOpprettetHendelse(WebSoknad soknad);

    void registrerMigrertHendelse(WebSoknad soknad);

    void registrerAutomatiskAvsluttetHendelse(String behandlingsId);

    void registrerHendelse(WebSoknad soknad, HendelseType hendelse);

    Integer hentVersjon(String behandlingsId);

    List<String> hentSoknaderUnderArbeidEldreEnn(int antallDager);
}
