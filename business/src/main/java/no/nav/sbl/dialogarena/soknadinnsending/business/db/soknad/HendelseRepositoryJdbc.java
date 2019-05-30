package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

    public void registrerOpprettetHendelse(String behandlingsId, Integer versjon) {
        insertHendelse(behandlingsId, OPPRETTET.name(), versjon, "NAV 35-18.01");
    }

    public void registrerHendelse(String behandlingsId, HendelseType hendelse, Integer versjon) {
        insertHendelse(behandlingsId, hendelse.name(), versjon, "NAV 35-18.01");
    }

    @Override
    public void registrerAutomatiskAvsluttetHendelse(String behandlingsId) {
        insertHendelse(behandlingsId, AVBRUTT_AUTOMATISK.name(), null, null);
    }

    @Transactional(readOnly = true)
    public Integer hentVersjon(String behandlingsId) {
        try {
            Object[] args = {OPPRETTET.name(), MIGRERT.name(), behandlingsId};
            return getJdbcTemplate().queryForObject(
                    "SELECT * FROM (" + "SELECT versjon FROM hendelse WHERE (hendelse_type = ? or hendelse_type = ?) AND behandlingsid = ?" + " ORDER BY hendelse_tidspunkt DESC)"
                            + whereLimit(1),
                    args, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            /**
             * Dersom det ikke finnes noen hendelser hvor det er satt versjon antar vi at soknader er opprettet før
             * migrering var en mulighet og gir den dermed defaultversjonen. Når man skal gjøre en migrering
             * vil det være viktig å avstemme søknadene under arbeid i henvendelse med innholdet i hendelsene for å sjekke
             * de ligger i hendelsetabellen med rett versjon
             **/
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
