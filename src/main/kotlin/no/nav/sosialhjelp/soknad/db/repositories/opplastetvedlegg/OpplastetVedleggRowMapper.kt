package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.OpplastetVedleggType
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

object OpplastetVedleggRowMapper {

    val opplastetVedleggRowMapper = RowMapper { rs: ResultSet, _: Int ->
        OpplastetVedlegg()
            .withUuid(rs.getString("uuid"))
            .withEier(rs.getString("eier"))
            .withVedleggType(OpplastetVedleggType(rs.getString("type")))
            .withData(rs.getBytes("data"))
            .withSoknadId(rs.getLong("soknad_under_arbeid_id"))
            .withFilnavn(rs.getString("filnavn"))
            .withSha512(rs.getString("sha512"))
    }
}
