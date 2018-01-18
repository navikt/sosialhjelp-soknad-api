package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.toDate;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.whereLimit;


@Component
@Transactional
public class HendelseRepositoryJdbc extends NamedParameterJdbcDaoSupport implements HendelseRepository {

    @Autowired
    private Clock clock;

    public HendelseRepositoryJdbc() {
    }


    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    public void registrerOpprettetHendelse(WebSoknad soknad) {
        insertHendelse(soknad.getBrukerBehandlingId(), OPPRETTET.name(), soknad.getVersjon(), soknad.getskjemaNummer());
    }

    public void registrerHendelse(WebSoknad soknad, HendelseType hendelse) {
        insertHendelse(soknad.getBrukerBehandlingId(), hendelse.name(), soknad.getVersjon(), soknad.getskjemaNummer());
    }

    public void registrerMigrertHendelse(WebSoknad soknad) {
        insertHendelse(soknad.getBrukerBehandlingId(), MIGRERT.name(), soknad.getVersjon(), soknad.getskjemaNummer());
    }

    @Override
    public void registrerAutomatiskAvsluttetHendelse(String behandlingsId) {
        insertHendelse(behandlingsId, AVBRUTT_AUTOMATISK.name(), null, null);
    }

    @Transactional(readOnly = true)
    public Integer hentVersjon(String behandlingsId) {
        String selector = "SELECT versjon FROM hendelse WHERE (hendelse_type = ? or hendelse_type = ?) AND behandlingsid = ?";
        Boolean finnesOpprettetEllerMigrertHendelse = getJdbcTemplate().queryForObject(
                "EXISTS (" + selector + ")",
                new String[]{OPPRETTET.name(), behandlingsId}, Boolean.class);

        if(finnesOpprettetEllerMigrertHendelse){
            Object[] args = {OPPRETTET.name(), MIGRERT.name(), behandlingsId};
            return getJdbcTemplate().queryForObject(
                    "SELECT * FROM (" + selector + " ORDER BY hendelse_tidspunkt DESC)"
                            + whereLimit(1),
                    args, Integer.class);
        }
        else {
            return new Integer(KravdialogInformasjon.DefaultOppsett.VERSJON);
        }
    }


    @Transactional(readOnly = true)
    public List<String> hentSoknaderUnderArbeidEldreEnn(int antallDager) {
        List<String> avsluttetHendelser = Stream.of(AVBRUTT_AUTOMATISK, INNSENDT, AVBRUTT_AV_BRUKER).map(HendelseType::name).collect(toList());

        List<String> soknaderUnderArbeid = getJdbcTemplate()
                .queryForList("select BEHANDLINGSID from hendelse " +
                        "where SIST_HENDELSE=1 " +
                        "and not ( HENDELSE_TYPE = ? or HENDELSE_TYPE = ? or HENDELSE_TYPE = ?)" +
                        "and HENDELSE_TIDSPUNKT < " + toDate(antallDager), String.class, avsluttetHendelser.toArray());


        return soknaderUnderArbeid;
    }

    private void insertHendelse(String behandlingsid, String hendelse_type, Integer versjon, String skjemanummer) {
        getJdbcTemplate()
                .update("update hendelse set SIST_HENDELSE = 0 where BEHANDLINGSID = ? AND SIST_HENDELSE=1", behandlingsid);
        getJdbcTemplate()
                .update("insert into hendelse (BEHANDLINGSID, HENDELSE_TYPE, HENDELSE_TIDSPUNKT, VERSJON, SKJEMANUMMER, SIST_HENDELSE)" +
                                " values (?,?,?,?,?, 1)",
                        behandlingsid,
                        hendelse_type,
                        new Timestamp(clock.instant().toEpochMilli()),
                        versjon,
                        skjemanummer);
    }


}
