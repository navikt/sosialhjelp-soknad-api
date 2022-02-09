package no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.common.exceptions.SamtidigOppdateringException;
import no.nav.sosialhjelp.soknad.common.exceptions.SoknadLaastException;
import no.nav.sosialhjelp.soknad.common.exceptions.SoknadUnderArbeidIkkeFunnetException;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.Date.from;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static no.nav.sosialhjelp.soknad.business.db.SQLUtils.selectNextSequenceValue;
import static no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus.UNDER_ARBEID;

@Named("SoknadUnderArbeidRepository")
@Component
public class SoknadUnderArbeidRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadUnderArbeidRepository {

    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    {
        mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        writer = mapper.writerWithDefaultPrettyPrinter();
    }

    @Inject
    private TransactionTemplate transactionTemplate;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public Long opprettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier) {
        sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid, eier);
        Long soknadUnderArbeidId = getJdbcTemplate().queryForObject(selectNextSequenceValue("SOKNAD_UNDER_ARBEID_ID_SEQ"), Long.class);
        getJdbcTemplate()
                .update("insert into SOKNAD_UNDER_ARBEID (SOKNAD_UNDER_ARBEID_ID, VERSJON, BEHANDLINGSID, TILKNYTTETBEHANDLINGSID, EIER, DATA, STATUS, OPPRETTETDATO, SISTENDRETDATO)" +
                                " values (?,?,?,?,?,?,?,?,?)",
                        soknadUnderArbeidId,
                        soknadUnderArbeid.getVersjon(),
                        soknadUnderArbeid.getBehandlingsId(),
                        soknadUnderArbeid.getTilknyttetBehandlingsId(),
                        soknadUnderArbeid.getEier(),
                        mapJsonSoknadInternalTilFil(soknadUnderArbeid.getJsonInternalSoknad()),
                        soknadUnderArbeid.getStatus().toString(),
                        from(soknadUnderArbeid.getOpprettetDato().atZone(ZoneId.systemDefault()).toInstant()),
                        from(soknadUnderArbeid.getSistEndretDato().atZone(ZoneId.systemDefault()).toInstant()));
        return soknadUnderArbeidId;
    }

    @Override
    public Optional<SoknadUnderArbeid> hentSoknad(Long soknadId, String eier) {
        return getJdbcTemplate().query("select * from SOKNAD_UNDER_ARBEID where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
                new SoknadUnderArbeidRowMapper(), eier, soknadId).stream().findFirst();
    }

    @Override
    public SoknadUnderArbeid hentSoknad(String behandlingsId, String eier) {
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = getJdbcTemplate().query("select * from SOKNAD_UNDER_ARBEID where EIER = ? and BEHANDLINGSID = ?",
                new SoknadUnderArbeidRowMapper(), eier, behandlingsId).stream().findFirst();
        if (soknadUnderArbeidOptional.isPresent()) {
            return soknadUnderArbeidOptional.get();
        } else {
            throw new SoknadUnderArbeidIkkeFunnetException("Ingen SoknadUnderArbeid funnet på behandlingsId: " + behandlingsId);
        }
    }

    @Override
    public Optional<SoknadUnderArbeid> hentSoknadOptional(String behandlingsId, String eier) {
        return getJdbcTemplate().query("select * from SOKNAD_UNDER_ARBEID where EIER = ? and BEHANDLINGSID = ?",
                new SoknadUnderArbeidRowMapper(), eier, behandlingsId).stream().findFirst();
    }

    @Override
    public Optional<SoknadUnderArbeid> hentEttersendingMedTilknyttetBehandlingsId(String tilknyttetBehandlingsId, String eier) {
        return getJdbcTemplate().query("select * from SOKNAD_UNDER_ARBEID where EIER = ? and TILKNYTTETBEHANDLINGSID = ? and STATUS = ?",
                new SoknadUnderArbeidRowMapper(), eier, tilknyttetBehandlingsId, UNDER_ARBEID.toString()).stream().findFirst();
    }

    @Override
    public void oppdaterSoknadsdata(SoknadUnderArbeid soknadUnderArbeid, String eier) throws SamtidigOppdateringException {
        sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid, eier);
        sjekkOmSoknadErLaast(soknadUnderArbeid);
        final Long opprinneligVersjon = soknadUnderArbeid.getVersjon();
        final Long oppdatertVersjon = opprinneligVersjon + 1;
        final LocalDateTime sistEndretDato = now();
        byte[] data = mapJsonSoknadInternalTilFil(soknadUnderArbeid.getJsonInternalSoknad());

        final int antallOppdaterteRader = getJdbcTemplate()
                .update("update SOKNAD_UNDER_ARBEID set VERSJON = ?, DATA = ?, SISTENDRETDATO = ? where SOKNAD_UNDER_ARBEID_ID = ? and EIER = ? and VERSJON = ? and STATUS = ?",
                        oppdatertVersjon,
                        data,
                        from(sistEndretDato.atZone(ZoneId.systemDefault()).toInstant()),
                        soknadUnderArbeid.getSoknadId(),
                        eier,
                        opprinneligVersjon,
                        UNDER_ARBEID.toString());
        if (antallOppdaterteRader == 0) {
            SoknadUnderArbeid soknadIDb = hentSoknad(soknadUnderArbeid.getSoknadId(), soknadUnderArbeid.getEier()).orElseThrow(() -> new IllegalStateException(
                            String.format("Ingen soknadUnderArbeid funnet for %s, med status %s ",
                                    soknadUnderArbeid.getBehandlingsId(),
                                    soknadUnderArbeid.getStatus())));
            if (Arrays.equals(mapJsonSoknadInternalTilFil(soknadIDb.getJsonInternalSoknad()), data)) {
                return;
            }
            throw new SamtidigOppdateringException("Mulig versjonskonflikt ved oppdatering av søknad under arbeid " +
                    "med behandlingsId " + soknadUnderArbeid.getBehandlingsId() + " fra versjon " + opprinneligVersjon +
                    " til versjon " + oppdatertVersjon);
        }
        soknadUnderArbeid.setVersjon(oppdatertVersjon);
        soknadUnderArbeid.setSistEndretDato(sistEndretDato);
    }

    @Override
    public void oppdaterInnsendingStatus(SoknadUnderArbeid soknadUnderArbeid, String eier) {
        sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid, eier);
        final LocalDateTime sistEndretDato = now();
        final int antallOppdaterteRader = getJdbcTemplate()
                .update("update SOKNAD_UNDER_ARBEID set STATUS = ?, SISTENDRETDATO = ? where SOKNAD_UNDER_ARBEID_ID = ? and EIER = ?",
                        soknadUnderArbeid.getStatus().toString(),
                        from(sistEndretDato.atZone(ZoneId.systemDefault()).toInstant()),
                        soknadUnderArbeid.getSoknadId(),
                        eier);
        if (antallOppdaterteRader != 0) {
            soknadUnderArbeid.setSistEndretDato(sistEndretDato);
        }
    }

    @Override
    public void slettSoknad(SoknadUnderArbeid soknadUnderArbeid, String eier) {
        sjekkOmBrukerEierSoknadUnderArbeid(soknadUnderArbeid, eier);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                final Long soknadUnderArbeidId = soknadUnderArbeid.getSoknadId();
                if (soknadUnderArbeidId == null) {
                    throw new RuntimeException("Kan ikke slette sendt søknad uten søknadsid");
                }
                opplastetVedleggRepository.slettAlleVedleggForSoknad(soknadUnderArbeidId, eier);
                getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?", eier, soknadUnderArbeidId);
            }
        });
    }

    private void sjekkOmBrukerEierSoknadUnderArbeid(SoknadUnderArbeid soknadUnderArbeid, String eier) {
        if (eier == null || !eier.equalsIgnoreCase(soknadUnderArbeid.getEier())) {
            throw new RuntimeException("Eier stemmer ikke med søknadens eier");
        }
    }

    private void sjekkOmSoknadErLaast(SoknadUnderArbeid soknadUnderArbeid) {
        if (SoknadUnderArbeidStatus.LAAST.equals(soknadUnderArbeid.getStatus())) {
            throw new SoknadLaastException("Kan ikke oppdatere søknad med behandlingsid " + soknadUnderArbeid.getBehandlingsId() +
                    " fordi den er sendt fra bruker");
        }
    }

    private byte[] mapJsonSoknadInternalTilFil(JsonInternalSoknad jsonInternalSoknad) {
        try {
            final String internalSoknad = writer.writeValueAsString(jsonInternalSoknad);
            ensureValidInternalSoknad(internalSoknad);
            return internalSoknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere søknadsobjekt til tekststreng", e);
            throw new RuntimeException(e);
        }
    }


}
