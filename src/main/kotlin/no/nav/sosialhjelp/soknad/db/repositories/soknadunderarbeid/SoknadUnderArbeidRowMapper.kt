package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import java.io.IOException
import java.sql.ResultSet

class SoknadUnderArbeidRowMapper : RowMapper<SoknadUnderArbeid> {

    private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        .configure(ORDER_MAP_ENTRIES_BY_KEYS, true)
        .configure(SORT_PROPERTIES_ALPHABETICALLY, true)

    override fun mapRow(rs: ResultSet, rowNum: Int): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            soknadId = rs.getLong("soknad_under_arbeid_id"),
            versjon = rs.getLong("versjon"),
            behandlingsId = rs.getString("behandlingsid"),
            tilknyttetBehandlingsId = rs.getString("tilknyttetbehandlingsid"),
            eier = rs.getString("eier"),
            jsonInternalSoknad = mapDataToJsonInternalSoknad(rs.getBytes("data")),
            status = SoknadUnderArbeidStatus.valueOf(rs.getString("status")),
            opprettetDato = rs.getTimestamp("opprettetdato").toLocalDateTime(),
            sistEndretDato = rs.getTimestamp("sistendretdato").toLocalDateTime(),
        )
    }

    private fun mapDataToJsonInternalSoknad(data: ByteArray?): JsonInternalSoknad? {
        return data?.let {
            try {
                mapper.readValue(data, JsonInternalSoknad::class.java)
            } catch (e: IOException) {
                log.error("Kunne ikke finne søknad", e)
                throw RuntimeException(e)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoknadUnderArbeidRowMapper::class.java)
    }
}
