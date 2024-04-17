package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.db.SQLUtils
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

object SoknadMetadataRowMapper {

    private val mapper = jacksonObjectMapper()

    val soknadMetadataRowMapper = RowMapper { rs: ResultSet, _: Int ->
        SoknadMetadata(
            id = rs.getLong("id"),
            behandlingsId = rs.getString("behandlingsid"),
            fnr = rs.getString("fnr"),
            skjema = rs.getString("skjema"),
            orgnr = rs.getString("orgnr"),
            navEnhet = rs.getString("navenhet"),
            fiksForsendelseId = rs.getString("fiksforsendelseid"),
            vedlegg = rs.getString("vedlegg")?.let { mapper.readValue(it, VedleggMetadataListe::class.java) },
            type = SoknadMetadataType.valueOf(rs.getString("soknadtype")),
            status = SoknadMetadataInnsendingStatus.valueOf(rs.getString("innsendingstatus")),
            opprettetDato = SQLUtils.timestampTilTid(rs.getTimestamp("opprettetdato")),
            sistEndretDato = SQLUtils.timestampTilTid(rs.getTimestamp("sistendretdato")),
            innsendtDato = SQLUtils.nullableTimestampTilTid(rs.getTimestamp("innsendtdato")),
            lest = rs.getBoolean("lest_ditt_nav")
        )
    }
}
