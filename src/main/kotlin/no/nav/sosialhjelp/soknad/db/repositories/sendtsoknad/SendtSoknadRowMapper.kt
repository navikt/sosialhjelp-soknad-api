package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

object SendtSoknadRowMapper {

    val sendtSoknadRowMapper = RowMapper { rs: ResultSet, _: Int ->
        SendtSoknad(
            sendtSoknadId = rs.getLong("sendt_soknad_id"),
            behandlingsId = rs.getString("behandlingsid"),
            tilknyttetBehandlingsId = rs.getString("tilknyttetbehandlingsid"),
            eier = rs.getString("eier"),
            fiksforsendelseId = rs.getString("fiksforsendelseid"),
            orgnummer = rs.getString("orgnr"),
            navEnhetsnavn = rs.getString("navenhetsnavn"),
            brukerOpprettetDato = rs.getTimestamp("brukeropprettetdato").toLocalDateTime(),
            brukerFerdigDato = rs.getTimestamp("brukerferdigdato").toLocalDateTime(),
            sendtDato = rs.getTimestamp("sendtdato")?.toLocalDateTime(),
        )
    }
}
