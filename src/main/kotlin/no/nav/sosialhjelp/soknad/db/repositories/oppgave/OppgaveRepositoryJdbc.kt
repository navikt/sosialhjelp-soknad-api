package no.nav.sosialhjelp.soknad.db.repositories.oppgave

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.SQLUtils.nullableTimestampTilTid
import no.nav.sosialhjelp.soknad.db.SQLUtils.selectNextSequenceValue
import no.nav.sosialhjelp.soknad.db.SQLUtils.tidTilTimestamp
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
@Transactional
open class OppgaveRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate
) : OppgaveRepository {

    private val oppgaveRowMapper = RowMapper { rs: ResultSet, _: Int ->
        Oppgave(
            id = rs.getLong("id"),
            behandlingsId = rs.getString("behandlingsid"),
            type = rs.getString("type"),
            status = Status.valueOf(rs.getString("status")),
            steg = rs.getInt("steg"),
            oppgaveData = rs.getString("oppgavedata")?.let { JAXB.unmarshal(it, FiksData::class.java) },
            oppgaveResultat = rs.getString("oppgaveresultat")?.let { JAXB.unmarshal(it, FiksResultat::class.java) },
            opprettet = nullableTimestampTilTid(rs.getTimestamp("opprettet")),
            sistKjort = nullableTimestampTilTid(rs.getTimestamp("sistkjort")),
            nesteForsok = nullableTimestampTilTid(rs.getTimestamp("nesteforsok")),
            retries = rs.getInt("retries")
        )
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
            oppgave.oppgaveData?.let { JAXB.marshal(it) },
            oppgave.oppgaveResultat?.let { JAXB.marshal(it) },
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
            oppgave.oppgaveData?.let { JAXB.marshal(it) },
            oppgave.oppgaveResultat?.let { JAXB.marshal(it) },
            tidTilTimestamp(oppgave.nesteForsok),
            oppgave.retries,
            oppgave.id
        )
    }

    override fun hentOppgave(behandlingsId: String): Oppgave? {
        return jdbcTemplate.query(
            "SELECT * FROM oppgave WHERE behandlingsid = ?",
            oppgaveRowMapper,
            behandlingsId
        ).firstOrNull()
    }

    override fun hentNeste(): Oppgave? {
        val select =
            "SELECT * FROM oppgave WHERE status = ? and (nesteforsok is null OR nesteforsok < ?) " + SQLUtils.limit(1)
        while (true) {
            val resultat = jdbcTemplate.query(
                select,
                oppgaveRowMapper,
                Status.KLAR.name,
                tidTilTimestamp(LocalDateTime.now())
            ).firstOrNull() ?: return null

            val rowsAffected = jdbcTemplate.update(
                "UPDATE oppgave SET status = ?, sistkjort = ? WHERE status = ? AND id = ?",
                Status.UNDER_ARBEID.name,
                tidTilTimestamp(LocalDateTime.now()),
                Status.KLAR.name,
                resultat.id
            )
            if (rowsAffected == 1) {
                return resultat
            }
        }
    }

    override fun retryOppgaveStuckUnderArbeid(): Int {
        return jdbcTemplate.update(
            "UPDATE oppgave SET status = ? WHERE status = ? AND sistkjort < ?",
            Status.KLAR.name,
            Status.UNDER_ARBEID.name,
            tidTilTimestamp(LocalDateTime.now().minusHours(1))
        )
    }

    override fun hentAntallFeilede(): Int {
        return jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave WHERE status = ?",
            Int::class.java,
            Status.FEILET.name
        )
    }

    override fun hentAntallStuckUnderArbeid(): Int {
        return jdbcTemplate.queryForObject(
            "SELECT count(*) FROM oppgave WHERE status = ? AND sistkjort < ?",
            Int::class.java,
            Status.UNDER_ARBEID.name,
            tidTilTimestamp(LocalDateTime.now().minusMinutes(10))
        )
    }

    override fun slettOppgave(behandlingsId: String) {
        jdbcTemplate.update("DELETE FROM oppgave WHERE behandlingsid = ?", behandlingsId)
    }

    override fun count(): Int {
        return jdbcTemplate.queryForObject(
            "select count(*) from oppgave",
            Int::class.java
        ) ?: 0
    }
}
