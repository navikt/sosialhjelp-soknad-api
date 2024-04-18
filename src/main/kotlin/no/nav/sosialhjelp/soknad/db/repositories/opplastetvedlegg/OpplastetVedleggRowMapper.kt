package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

object OpplastetVedleggRowMapper {
    val opplastetVedleggRowMapper =
        RowMapper { rs: ResultSet, _: Int ->
            OpplastetVedlegg(
                uuid = rs.getString("uuid"),
                eier = rs.getString("eier"),
                vedleggType =
                    OpplastetVedleggType(
                        sammensattType = rs.getString("type"),
                    ),
                data = rs.getBytes("data"),
                soknadId = rs.getLong("soknad_under_arbeid_id"),
                filnavn = rs.getString("filnavn"),
                sha512 = rs.getString("sha512"),
            )
        }
}
