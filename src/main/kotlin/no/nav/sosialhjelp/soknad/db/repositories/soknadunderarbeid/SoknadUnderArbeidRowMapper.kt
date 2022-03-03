package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import java.io.IOException
import java.sql.ResultSet

class SoknadUnderArbeidRowMapper : RowMapper<SoknadUnderArbeid> {

    private val mapper: ObjectMapper = ObjectMapper().addMixIn(JsonAdresse::class.java, AdresseMixIn::class.java)
    private val writer: ObjectWriter = mapper.writerWithDefaultPrettyPrinter()

    override fun mapRow(rs: ResultSet, rowNum: Int): SoknadUnderArbeid {
        var status: SoknadUnderArbeidStatus? = null
        try {
            val statusFraDb = rs.getString("status")
            if (StringUtils.isNotEmpty(statusFraDb)) {
                status = SoknadUnderArbeidStatus.valueOf(statusFraDb)
            }
        } catch (e: IllegalArgumentException) {
            throw RuntimeException("Ukjent innsendingsstatus fra database", e)
        }
        return SoknadUnderArbeid()
            .withSoknadId(rs.getLong("soknad_under_arbeid_id"))
            .withVersjon(rs.getLong("versjon"))
            .withBehandlingsId(rs.getString("behandlingsid"))
            .withTilknyttetBehandlingsId(rs.getString("tilknyttetbehandlingsid"))
            .withEier(rs.getString("eier"))
            .withJsonInternalSoknad(mapDataToJsonInternalSoknad(rs.getBytes("data")))
            .withStatus(status)
            .withOpprettetDato(rs.getTimestamp("opprettetdato")?.toLocalDateTime())
            .withSistEndretDato(rs.getTimestamp("sistendretdato")?.toLocalDateTime())
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
