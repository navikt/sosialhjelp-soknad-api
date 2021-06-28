package no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
import org.slf4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public class SoknadUnderArbeidRowMapper implements RowMapper<SoknadUnderArbeid>  {

    private static final Logger log = getLogger(SoknadUnderArbeidRowMapper.class);

    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    {
        mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    public SoknadUnderArbeid mapRow(ResultSet rs, int rowNum) throws SQLException {
        SoknadUnderArbeidStatus status = null;
        try {
            final String statusFraDb = rs.getString("status");
            if (isNotEmpty(statusFraDb)) {
                status = SoknadUnderArbeidStatus.valueOf(statusFraDb);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Ukjent innsendingsstatus fra database", e);
        }
        return new SoknadUnderArbeid()
                .withSoknadId(rs.getLong("soknad_under_arbeid_id"))
                .withVersjon(rs.getLong("versjon"))
                .withBehandlingsId(rs.getString("behandlingsid"))
                .withTilknyttetBehandlingsId(rs.getString("tilknyttetbehandlingsid"))
                .withEier(rs.getString("eier"))
                .withJsonInternalSoknad(mapDataToJsonInternalSoknad(rs.getBytes("data")))
                .withStatus(status)
                .withOpprettetDato(rs.getTimestamp("opprettetdato") != null ?
                        rs.getTimestamp("opprettetdato").toLocalDateTime() : null)
                .withSistEndretDato(rs.getTimestamp("sistendretdato") != null ?
                        rs.getTimestamp("sistendretdato").toLocalDateTime() : null);
    }

    private JsonInternalSoknad mapDataToJsonInternalSoknad(byte[] data){
        if (data == null){
            return null;
        }
        try {
            return mapper.readValue(data, JsonInternalSoknad.class);
        } catch (IOException e) {
            log.error("Kunne ikke finne søknad", e);
            throw new RuntimeException(e);
        }
    }
}
