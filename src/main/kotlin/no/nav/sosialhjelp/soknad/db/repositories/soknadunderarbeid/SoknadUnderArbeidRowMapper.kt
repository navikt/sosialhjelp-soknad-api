package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import java.io.IOException
import java.sql.ResultSet

class SoknadUnderArbeidRowMapper : RowMapper<SoknadUnderArbeid> {

    private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()

    override fun mapRow(rs: ResultSet, rowNum: Int): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            soknadId = rs.getLong("soknad_under_arbeid_id"),
            versjon = rs.getLong("versjon"),
            behandlingsId = rs.getString("behandlingsid"),
            eier = rs.getString("eier"),
            jsonInternalSoknad = mapDataToJsonInternalSoknad(rs.getString("data")),
            status = SoknadUnderArbeidStatus.valueOf(rs.getString("status")),
            opprettetDato = rs.getTimestamp("opprettetdato").toLocalDateTime(),
            sistEndretDato = rs.getTimestamp("sistendretdato").toLocalDateTime()
        )
    }

    private fun mapDataToJsonInternalSoknad(data: String?): JsonInternalSoknad? {
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
