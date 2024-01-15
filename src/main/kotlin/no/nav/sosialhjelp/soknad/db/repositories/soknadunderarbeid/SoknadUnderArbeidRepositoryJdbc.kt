package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SamtidigOppdateringException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadLaastException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadUnderArbeidIkkeFunnetException
import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
class SoknadUnderArbeidRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
) : SoknadUnderArbeidRepository {

    private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
    private val writer = mapper.writerWithDefaultPrettyPrinter()

    private val soknadUnderArbeidRowMapper = SoknadUnderArbeidRowMapper()

    override fun opprettSoknad(soknadUnderArbeid: SoknadUnderArbeid, eier: String): Long? {
        sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid, eier)
        val soknadUnderArbeidId = jdbcTemplate.queryForObject(
            SQLUtils.selectNextSequenceValue("SOKNAD_UNDER_ARBEID_ID_SEQ"),
            Long::class.java,
        )
        jdbcTemplate.update(
            "insert into SOKNAD_UNDER_ARBEID (SOKNAD_UNDER_ARBEID_ID, VERSJON, BEHANDLINGSID, TILKNYTTETBEHANDLINGSID, EIER, DATA, STATUS, OPPRETTETDATO, SISTENDRETDATO) values (?,?,?,?,?,?,?,?,?)",
            soknadUnderArbeidId,
            soknadUnderArbeid.versjon,
            soknadUnderArbeid.behandlingsId,
            soknadUnderArbeid.tilknyttetBehandlingsId,
            soknadUnderArbeid.eier,
            soknadUnderArbeid.jsonInternalSoknad?.let { mapJsonSoknadInternalTilFil(it) },
            soknadUnderArbeid.status.toString(),
            Date.from(soknadUnderArbeid.opprettetDato.atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(soknadUnderArbeid.sistEndretDato.atZone(ZoneId.systemDefault()).toInstant()),
        )
        return soknadUnderArbeidId
    }

    override fun hentSoknad(soknadId: Long, eier: String): SoknadUnderArbeid? {
        return jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
            soknadUnderArbeidRowMapper,
            eier,
            soknadId,
        ).firstOrNull()
    }

    override fun hentSoknad(behandlingsId: String?, eier: String): SoknadUnderArbeid {
        return jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where EIER = ? and BEHANDLINGSID = ?",
            soknadUnderArbeidRowMapper,
            eier,
            behandlingsId,
        ).firstOrNull() ?: throw SoknadUnderArbeidIkkeFunnetException("Ingen SoknadUnderArbeid funnet på behandlingsId: $behandlingsId")
    }

    override fun hentSoknadNullable(behandlingsId: String?, eier: String): SoknadUnderArbeid? {
        return jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where EIER = ? and BEHANDLINGSID = ?",
            soknadUnderArbeidRowMapper,
            eier,
            behandlingsId,
        ).firstOrNull()
    }

    override fun hentEttersendingMedTilknyttetBehandlingsId(
        tilknyttetBehandlingsId: String,
        eier: String,
    ): SoknadUnderArbeid? {
        return jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where EIER = ? and TILKNYTTETBEHANDLINGSID = ? and STATUS = ?",
            soknadUnderArbeidRowMapper,
            eier,
            tilknyttetBehandlingsId,
            SoknadUnderArbeidStatus.UNDER_ARBEID.toString(),
        ).firstOrNull()
    }

    override fun oppdaterSoknadsdata(soknadUnderArbeid: SoknadUnderArbeid, eier: String) {
        sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid, eier)
        sjekkOmSoknadErLaast(soknadUnderArbeid)
        val opprinneligVersjon = soknadUnderArbeid.versjon
        val oppdatertVersjon = opprinneligVersjon + 1
        val sistEndretDato = LocalDateTime.now()
        val data = soknadUnderArbeid.jsonInternalSoknad?.let { mapJsonSoknadInternalTilFil(it) }

        val antallOppdaterteRader = jdbcTemplate.update(
            "update SOKNAD_UNDER_ARBEID set VERSJON = ?, DATA = ?, SISTENDRETDATO = ? where SOKNAD_UNDER_ARBEID_ID = ? and EIER = ? and VERSJON = ? and STATUS = ?",
            oppdatertVersjon,
            data,
            Date.from(sistEndretDato.atZone(ZoneId.systemDefault()).toInstant()),
            soknadUnderArbeid.soknadId,
            eier,
            opprinneligVersjon,
            SoknadUnderArbeidStatus.UNDER_ARBEID.toString(),
        )
        if (antallOppdaterteRader == 0) {
            val soknadIDb: SoknadUnderArbeid = hentSoknad(soknadUnderArbeid.soknadId, soknadUnderArbeid.eier)
                ?: throw IllegalStateException("Ingen soknadUnderArbeid funnet for ${soknadUnderArbeid.behandlingsId}, med status ${soknadUnderArbeid.status}")

            if (soknadIDb.jsonInternalSoknad?.let { mapJsonSoknadInternalTilFil(it).contentEquals(data) } == true) {
                return
            }
            throw SamtidigOppdateringException("Mulig versjonskonflikt ved oppdatering av søknad under arbeid med behandlingsId ${soknadUnderArbeid.behandlingsId} fra versjon $opprinneligVersjon til versjon $oppdatertVersjon")
        }
        soknadUnderArbeid.versjon = oppdatertVersjon
        soknadUnderArbeid.sistEndretDato = sistEndretDato
    }

    override fun oppdaterInnsendingStatus(soknadUnderArbeid: SoknadUnderArbeid, eier: String) {
        sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid, eier)
        val sistEndretDato = LocalDateTime.now()
        val antallOppdaterteRader = jdbcTemplate.update(
            "update SOKNAD_UNDER_ARBEID set STATUS = ?, SISTENDRETDATO = ? where SOKNAD_UNDER_ARBEID_ID = ? and EIER = ?",
            soknadUnderArbeid.status.toString(),
            Date.from(sistEndretDato.atZone(ZoneId.systemDefault()).toInstant()),
            soknadUnderArbeid.soknadId,
            eier,
        )
        if (antallOppdaterteRader != 0) {
            soknadUnderArbeid.sistEndretDato = sistEndretDato
        }
    }

    override fun slettSoknad(soknadUnderArbeid: SoknadUnderArbeid, eier: String) {
        sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid, eier)
        transactionTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                val soknadUnderArbeidId = soknadUnderArbeid.soknadId
                opplastetVedleggRepository.slettAlleVedleggForSoknad(soknadUnderArbeidId, eier)
                jdbcTemplate.update(
                    "delete from SOKNAD_UNDER_ARBEID where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
                    eier,
                    soknadUnderArbeidId,
                )
            }
        })
    }

    private fun sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid: SoknadUnderArbeid, eier: String) {
        if (!eier.equals(soknadUnderArbeid.eier, ignoreCase = true)) {
            throw RuntimeException("Eier stemmer ikke med søknadens eier")
        }
    }

    private fun sjekkOmSoknadErLaast(soknadUnderArbeid: SoknadUnderArbeid) {
        if (SoknadUnderArbeidStatus.LAAST == soknadUnderArbeid.status) {
            throw SoknadLaastException("Kan ikke oppdatere søknad med behandlingsid ${soknadUnderArbeid.behandlingsId} fordi den er sendt fra bruker")
        }
    }

    private fun mapJsonSoknadInternalTilFil(jsonInternalSoknad: JsonInternalSoknad): ByteArray {
        return try {
            val internalSoknad = writer.writeValueAsString(jsonInternalSoknad)
            JsonSosialhjelpValidator.ensureValidInternalSoknad(internalSoknad)
            internalSoknad.toByteArray(StandardCharsets.UTF_8)
        } catch (e: JsonProcessingException) {
            log.error("Kunne ikke konvertere søknadsobjekt til tekststreng", e)
            throw RuntimeException(e)
        }
    }

    companion object {
        private val log by logger()
    }
}
