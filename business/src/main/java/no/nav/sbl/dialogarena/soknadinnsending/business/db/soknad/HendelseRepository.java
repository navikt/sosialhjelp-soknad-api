package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;

import java.util.List;

public interface HendelseRepository {

    void registrerOpprettetHendelse(String behandlingsId, Integer versjon);

    void registrerAutomatiskAvsluttetHendelse(String behandlingsId);

    void registrerHendelse(String behandlingsId, HendelseType hendelse, Integer versjon);

    Integer hentVersjon(String behandlingsId);

    List<String> hentSoknaderUnderArbeidEldreEnn(int antallDager);
}
