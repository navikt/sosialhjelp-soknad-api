//package no.nav.sosialhjelp.soknad.business.db.repositories.oppgave;
//
//import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave;
//import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksData;
//import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksResultat;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.inject.Inject;
//import javax.sql.DataSource;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//import static no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave.Status.FEILET;
//import static no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave.Status.KLAR;
//import static no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave.Status.UNDER_ARBEID;
//import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.limit;
//import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.selectNextSequenceValue;
//import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.tidTilTimestamp;
//import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.timestampTilTid;
//
//@Component
//@Transactional
//public class OppgaveRepositoryJdbc extends NamedParameterJdbcDaoSupport implements OppgaveRepository {
//
//    private RowMapper<Oppgave> oppgaveRowMapper = (rs, rowNum) -> {
//        Oppgave oppgave = new Oppgave();
//        oppgave.id = rs.getLong("id");
//        oppgave.behandlingsId = rs.getString("behandlingsid");
//        oppgave.type = rs.getString("type");
//        oppgave.status = Oppgave.Status.valueOf(rs.getString("status"));
//        oppgave.steg = rs.getInt("steg");
//        oppgave.oppgaveData = Oppgave.JAXB.unmarshal(rs.getString("oppgavedata"), FiksData.class);
//        oppgave.oppgaveResultat = Oppgave.JAXB.unmarshal(rs.getString("oppgaveresultat"), FiksResultat.class);
//        oppgave.opprettet = timestampTilTid(rs.getTimestamp("opprettet"));
//        oppgave.sistKjort = timestampTilTid(rs.getTimestamp("sistkjort"));
//        oppgave.nesteForsok = timestampTilTid(rs.getTimestamp("nesteforsok"));
//        oppgave.retries = rs.getInt("retries");
//        return oppgave;
//    };
//
//    @Inject
//    public void setDS(DataSource ds) {
//        super.setDataSource(ds);
//    }
//
//    @Override
//    public void opprett(Oppgave oppgave) {
//        oppgave.id = getJdbcTemplate().queryForObject(selectNextSequenceValue("OPPGAVE_ID_SEQ"), Long.class);
//        getJdbcTemplate().update("INSERT INTO oppgave (id, behandlingsid, type, status, steg, oppgavedata, oppgaveresultat, " +
//                        "opprettet, sistkjort, nesteforsok, retries) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
//                oppgave.id,
//                oppgave.behandlingsId,
//                oppgave.type,
//                oppgave.status.name(),
//                oppgave.steg,
//                Oppgave.JAXB.marshal(oppgave.oppgaveData),
//                Oppgave.JAXB.marshal(oppgave.oppgaveResultat),
//                tidTilTimestamp(oppgave.opprettet),
//                tidTilTimestamp(oppgave.sistKjort),
//                tidTilTimestamp(oppgave.nesteForsok),
//                oppgave.retries
//        );
//    }
//
//    @Override
//    public void oppdater(Oppgave oppgave) {
//        getJdbcTemplate().update("UPDATE oppgave SET status = ?, steg = ?, oppgavedata = ?, oppgaveresultat = ?, " +
//                        "nesteforsok = ?, retries = ? WHERE id = ?",
//                oppgave.status.name(),
//                oppgave.steg,
//                Oppgave.JAXB.marshal(oppgave.oppgaveData),
//                Oppgave.JAXB.marshal(oppgave.oppgaveResultat),
//                tidTilTimestamp(oppgave.nesteForsok),
//                oppgave.retries,
//                oppgave.id
//        );
//    }
//
//    @Override
//    public Optional<Oppgave> hentOppgave(String behandlingsId) {
//        return getJdbcTemplate().query("SELECT * FROM oppgave WHERE behandlingsid = ?",
//                oppgaveRowMapper, behandlingsId).stream().findFirst();
//    }
//
//    @Override
//    public Optional<Oppgave> hentNeste() {
//        String select = "SELECT * FROM oppgave WHERE status = ? and (nesteforsok is null OR nesteforsok < ?) " + limit(1);
//
//        while (true) {
//            Optional<Oppgave> resultat = getJdbcTemplate().query(select, oppgaveRowMapper, KLAR.name(), tidTilTimestamp(LocalDateTime.now()))
//                    .stream().findFirst();
//
//            if (!resultat.isPresent()) {
//                return Optional.empty();
//            }
//
//            String update = "UPDATE oppgave SET status = ?, sistkjort = ? WHERE status = ? AND id = ?";
//            int rowsAffected = getJdbcTemplate().update(update, UNDER_ARBEID.name(), tidTilTimestamp(LocalDateTime.now()), KLAR.name(), resultat.get().id);
//            if (rowsAffected == 1) {
//                return resultat;
//            }
//        }
//    }
//
//    @Override
//    public int retryOppgaveStuckUnderArbeid() {
//        final String updateSql = "UPDATE oppgave SET status = ? WHERE status = ? AND sistkjort < ?";
//        return getJdbcTemplate().update(updateSql, KLAR.name(), UNDER_ARBEID.name(), tidTilTimestamp(LocalDateTime.now().minusHours(1)));
//    }
//
//    @Override
//    public Map<String, Integer> hentStatus() {
//        Map<String, Integer> statuser = new HashMap<>();
//
//        statuser.put("feilede", getJdbcTemplate().queryForObject("SELECT count(*) FROM oppgave WHERE status = ?", Integer.class, FEILET.name()));
//        statuser.put("lengearbeid", getJdbcTemplate().queryForObject("SELECT count(*) FROM oppgave WHERE status = ? AND sistkjort < ?",
//                Integer.class, UNDER_ARBEID.name(), tidTilTimestamp(LocalDateTime.now().minusMinutes(10))));
//
//        return statuser;
//    }
//
//    @Override
//    public void slettOppgave(String behandlingsId) {
//        getJdbcTemplate().update("DELETE FROM oppgave WHERE behandlingsid = ?", behandlingsId);
//    }
//}
