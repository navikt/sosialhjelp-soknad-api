package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.slf4j.Logger;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.whereLimit;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * marker alle metoder som transactional. Alle operasjoner vil skje i en
 * transactional write context. Read metoder kan overstyre dette om det trengs.
 */
@Named("hendelseRepository")
@Component
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)
public class HendelseRepositoryJdbc extends NamedParameterJdbcDaoSupport implements HendelseRepository {

    private static final Logger logger = getLogger(HendelseRepositoryJdbc.class);

    public HendelseRepositoryJdbc() {
    }

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    public void registrerOpprettetHendelse(WebSoknad soknad) {
        insertHendelse(soknad.getBrukerBehandlingId(), OPPRETTET.name(), soknad.getVersjon() , soknad.getskjemaNummer());
    }

    public void registrerHendelse(WebSoknad soknad, HendelseType hendelse) {
        insertHendelse(soknad.getBrukerBehandlingId(), hendelse.name(), soknad.getVersjon() , soknad.getskjemaNummer());
    }

    public void registrerMigrertHendelse(WebSoknad soknad) {
        insertHendelse(soknad.getBrukerBehandlingId(), MIGRERT.name(), soknad.getVersjon(), soknad.getskjemaNummer());
    }

    @Override
    public void registrerAutomatiskAvsluttetHendelse(String behandlingsId) {
        insertHendelse(behandlingsId, AVBRUTT_AUTOMATISK.name(), null, null);
    }

    public Integer hentVersjon(String behandlingsId) {
        String gyldigHendelseTyper = " hendelse_type in ('"+ OPPRETTET.name() + "','" + MIGRERT.name() + "')";
        String sql = "SELECT * FROM (SELECT versjon FROM hendelse WHERE" + gyldigHendelseTyper + "  AND behandlingsid = ? ORDER BY hendelse_tidspunkt DESC)" + whereLimit(1);
        return getJdbcTemplate().queryForObject(sql, new Object[] {behandlingsId}, Integer.class);
    }

    public Collection<String> hentBehandlingsIdForIkkeAvsluttede(int dagerGammel) {

        String avsluttetHendelse = "HENDELSE_TYPE in ('"+ AVBRUTT_AUTOMATISK.name() +"','" + AVBRUTT_AV_BRUKER.name() +"','" + INNSENDT.name() + "')";
        String gyldigeHendelseTyper = " HENDELSE_TYPE in ('"+ OPPRETTET.name() + "','" + MIGRERT.name() + "','" + LAGRET_I_HENVENDELSE.name() + "')";

        String sqlSubSelect1 = " ( SELECT H2.BEHANDLINGSID FROM HENDELSE H2 " +
                " WHERE H1.BEHANDLINGSID = H2.BEHANDLINGSID " +
                " AND HENDELSE_TIDSPUNKT > CURRENT_TIMESTAMP - NUMTODSINTERVAL(?,'DAY')  " +
                " AND " + gyldigeHendelseTyper  +  " ) ";

        String sqlSubSelect2 =  " ( SELECT H3.BEHANDLINGSID FROM HENDELSE H3 " +
                " WHERE H1.BEHANDLINGSID = H3.BEHANDLINGSID AND " + avsluttetHendelse + " ) ";

        String sql = " SELECT BEHANDLINGSID FROM HENDELSE H1 WHERE " + gyldigeHendelseTyper +
                " AND NOT EXISTS " +
                sqlSubSelect1 +
                " AND NOT EXISTS " +
                sqlSubSelect2;

        HashSet<String> resultSet = new HashSet<>();
        resultSet.addAll(getJdbcTemplate().queryForList(sql,String.class,dagerGammel));

        return resultSet;
    }

    private void insertHendelse(String behandlingsid, String hendelse_type, Integer versjon, String skjemanummer){
        getJdbcTemplate()
                .update("update hendelse set SIST_HENDELSE = 0 where BEHANDLINGSID = ? AND SIST_HENDELSE=1", behandlingsid);
        getJdbcTemplate()
                .update("insert into hendelse (BEHANDLINGSID, HENDELSE_TYPE, HENDELSE_TIDSPUNKT, VERSJON, SKJEMANUMMER, SIST_HENDELSE)" +
                            " values (?,?,CURRENT_TIMESTAMP,?,?, 1)",
                        behandlingsid,
                        hendelse_type,
                        versjon,
                        skjemanummer);
    }


}
