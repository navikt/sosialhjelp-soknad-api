package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataType
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

object SoknadMetadataRowMapper {

    val soknadMetadataRowMapper = RowMapper { rs: ResultSet, _: Int ->
        val metadata = SoknadMetadata()
        metadata.id = rs.getLong("id")
        metadata.behandlingsId = rs.getString("behandlingsid")
        metadata.tilknyttetBehandlingsId = rs.getString("tilknyttetBehandlingsId")
        metadata.skjema = rs.getString("skjema")
        metadata.fnr = rs.getString("fnr")
        metadata.vedlegg = SoknadMetadata.JAXB.unmarshal(rs.getString("vedlegg"), SoknadMetadata.VedleggMetadataListe::class.java)
        metadata.orgnr = rs.getString("orgnr")
        metadata.navEnhet = rs.getString("navenhet")
        metadata.fiksForsendelseId = rs.getString("fiksforsendelseid")
        metadata.type = SoknadMetadataType.valueOf(rs.getString("soknadtype"))
        metadata.status = SoknadMetadataInnsendingStatus.valueOf(rs.getString("innsendingstatus"))
        metadata.opprettetDato = SQLUtils.timestampTilTid(rs.getTimestamp("opprettetdato"))
        metadata.sistEndretDato = SQLUtils.timestampTilTid(rs.getTimestamp("sistendretdato"))
        metadata.innsendtDato = SQLUtils.timestampTilTid(rs.getTimestamp("innsendtdato"))
        metadata.lestDittNav = rs.getBoolean("lest_ditt_nav")
        metadata
    }
}
