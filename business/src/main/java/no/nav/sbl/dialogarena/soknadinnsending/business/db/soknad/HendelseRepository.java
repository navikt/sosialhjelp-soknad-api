package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Collection;

public interface HendelseRepository {

    void registrerOpprettetHendelse(WebSoknad soknad);

    void registrerMigrertHendelse(WebSoknad soknad);

    void registrerAutomatiskAvsluttetHendelse(String behandlingsId);

    void registrerHendelse(WebSoknad soknad, HendelseType hendelse);

    Integer hentVersjon(String behandlingsId);

    Collection<String> hentBehandlingsIdForIkkeAvsluttede(int dagerGammel);
}
