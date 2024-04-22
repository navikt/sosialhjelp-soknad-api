package no.nav.sosialhjelp.soknad.v2.okonomi

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class OkonomiForventningRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : OkonomiForventningRepository {
    override fun findOpplysningTypesBySoknadId(soknadId: UUID): List<String> =
        jdbcTemplate.queryForList(
            """
            SELECT opplysning_type
            FROM opplysning_expectation
            WHERE soknad_id = :soknadId
            """.trimIndent(),
            MapSqlParameterSource()
                .addValue("soknadId", soknadId),
            String::class.java,
        )

    override fun setExpectationForOpplysningType(
        soknadId: UUID,
        opplysningType: String,
        isExpected: Boolean,
    ) {
        if (isExpected) {
            jdbcTemplate.update(
                """
                INSERT INTO opplysning_expectation (soknad_id, opplysning_type)
                VALUES (:soknadId, :opplysningType)
                ON CONFLICT (soknad_id, opplysning_type) DO NOTHING
                """.trimIndent(),
                MapSqlParameterSource()
                    .addValue("soknadId", soknadId)
                    .addValue("opplysningType", opplysningType),
            )
        } else {
            jdbcTemplate.update(
                """
                DELETE FROM opplysning_expectation
                WHERE
                    soknad_id = :soknadId 
                AND 
                    opplysning_type = :opplysningType
                """.trimIndent(),
                MapSqlParameterSource()
                    .addValue("soknadId", soknadId)
                    .addValue("opplysningType", opplysningType),
            )
        }
    }
}
