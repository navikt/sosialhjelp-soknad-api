package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import no.nav.sosialhjelp.soknad.domain.SendtSoknad
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

object SendtSoknadRowMapper {

    val sendtSoknadRowMapper = RowMapper { rs: ResultSet, _: Int ->
        SendtSoknad()
            .withSendtSoknadId(rs.getLong("sendt_soknad_id"))
            .withBehandlingsId(rs.getString("behandlingsid"))
            .withTilknyttetBehandlingsId(rs.getString("tilknyttetbehandlingsid"))
            .withEier(rs.getString("eier"))
            .withFiksforsendelseId(rs.getString("fiksforsendelseid"))
            .withOrgnummer(rs.getString("orgnr"))
            .withNavEnhetsnavn(rs.getString("navenhetsnavn"))
            .withBrukerOpprettetDato(rs.getTimestamp("brukeropprettetdato")?.toLocalDateTime())
            .withBrukerFerdigDato(rs.getTimestamp("brukerferdigdato")?.toLocalDateTime())
            .withSendtDato(rs.getTimestamp("sendtdato")?.toLocalDateTime())
    }
}
