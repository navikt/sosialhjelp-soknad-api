package no.nav.sosialhjelp.soknad.db.repositories.oppgave

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.SQLUtils.selectNextSequenceValue
import no.nav.sosialhjelp.soknad.db.SQLUtils.tidTilTimestamp
import no.nav.sosialhjelp.soknad.db.SQLUtils.timestampTilTid
import no.nav.sosialhjelp.soknad.domain.FiksData
import no.nav.sosialhjelp.soknad.domain.FiksResultat
import no.nav.sosialhjelp.soknad.domain.Oppgave
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.Optional
import javax.inject.Inject
import javax.sql.DataSource

@Component
@Transactional
open class OppgaveRepositoryJdbc : NamedParameterJdbcDaoSupport(), OppgaveRepository {

    private val oppgaveRowMapper = RowMapper { rs: ResultSet, _: Int ->
        val oppgave = Oppgave()
        oppgave.id = rs.getLong("id")
        oppgave.behandlingsId = rs.getString("behandlingsid")
        oppgave.type = rs.getString("type")
        oppgave.status = Oppgave.Status.valueOf(rs.getString("status"))
        oppgave.steg = rs.getInt("steg")
        oppgave.oppgaveData = Oppgave.JAXB.unmarshal(rs.getString("oppgavedata"), FiksData::class.java)
        oppgave.oppgaveResultat = Oppgave.JAXB.unmarshal(rs.getString("oppgaveresultat"), FiksResultat::class.java)
        oppgave.opprettet = timestampTilTid(rs.getTimestamp("opprettet"))
        oppgave.sistKjort = timestampTilTid(rs.getTimestamp("sistkjort"))
        oppgave.nesteForsok = timestampTilTid(rs.getTimestamp("nesteforsok"))
        oppgave.retries = rs.getInt("retries")
        oppgave
    }

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    override fun opprett(oppgave: Oppgave) {
        oppgave.id = jdbcTemplate.queryForObject(selectNextSequenceValue("OPPGAVE_ID_SEQ"), Long::class.java)
        jdbcTemplate.update(
            "INSERT INTO oppgave (id, behandlingsid, type, status, steg, oppgavedata, oppgaveresultat, opprettet, sistkjort, nesteforsok, retries) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            oppgave.id,
            oppgave.behandlingsId,
            oppgave.type,
            oppgave.status.name,
            oppgave.steg,
            Oppgave.JAXB.marshal(oppgave.oppgaveData),
            Oppgave.JAXB.marshal(oppgave.oppgaveResultat),
            tidTilTimestamp(oppgave.opprettet),
            tidTilTimestamp(oppgave.sistKjort),
            tidTilTimestamp(oppgave.nesteForsok),
            oppgave.retries
        )
    }

    override fun oppdater(oppgave: Oppgave) {
        jdbcTemplate.update(
            "UPDATE oppgave SET status = ?, steg = ?, oppgavedata = ?, oppgaveresultat = ?, nesteforsok = ?, retries = ? WHERE id = ?",
            oppgave.status.name,
            oppgave.steg,
            Oppgave.JAXB.marshal(oppgave.oppgaveData),
            Oppgave.JAXB.marshal(oppgave.oppgaveResultat),
            tidTilTimestamp(oppgave.nesteForsok),
            oppgave.retries,
            oppgave.id
        )
    }

    override fun hentOppgave(behandlingsId: String): Optional<Oppgave> {
        return jdbcTemplate.query(
            "SELECT * FROM oppgave WHERE behandlingsid = ?",
            oppgaveRowMapper,
            behandlingsId
        ).stream().findFirst()
    }

    override fun hentNeste(): Optional<Oppgave> {
        val select =
            "SELECT * FROM oppgave WHERE status = ? and (nesteforsok is null OR nesteforsok < ?) " + SQLUtils.limit(1)
        while (true) {
            val resultat = jdbcTemplate.query(
                select,
                oppgaveRowMapper,
                Oppgave.Status.KLAR.name,
                tidTilTimestamp(LocalDateTime.now())
            ).stream().findFirst()
            if (!resultat.isPresent) {
                return Optional.empty()
            }
            val update = "UPDATE oppgave SET status = ?, sistkjort = ? WHERE status = ? AND id = ?"
            val rowsAffected = jdbcTemplate.update(
                update,
                Oppgave.Status.UNDER_ARBEID.name,
                tidTilTimestamp(LocalDateTime.now()),
                Oppgave.Status.KLAR.name,
                resultat.get().id
            )
            if (rowsAffected == 1) {
                return resultat
            }
        }
    }

    override fun retryOppgaveStuckUnderArbeid(): Int {
        val updateSql = "UPDATE oppgave SET status = ? WHERE status = ? AND sistkjort < ?"
        return jdbcTemplate.update(
            updateSql,
            Oppgave.Status.KLAR.name,
            Oppgave.Status.UNDER_ARBEID.name,
            tidTilTimestamp(LocalDateTime.now().minusHours(1))
        )
    }

    override fun hentStatus(): Map<String, Int> {
        val statuser: MutableMap<String, Int> = HashMap()
        statuser["feilede"] = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave WHERE status = ?",
            Int::class.java,
            Oppgave.Status.FEILET.name
        )
        statuser["lengearbeid"] = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave WHERE status = ? AND sistkjort < ?",
            Int::class.java,
            Oppgave.Status.UNDER_ARBEID.name,
            tidTilTimestamp(LocalDateTime.now().minusMinutes(10))
        )
        return statuser
    }

    override fun slettOppgave(behandlingsId: String) {
        jdbcTemplate.update("DELETE FROM oppgave WHERE behandlingsid = ?", behandlingsId)
    }
}
