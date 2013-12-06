package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.IdGenerator.lagBehandlingsId;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils.selectNextSequenceValue;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.UTFYLLING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.AVBRUTT_AV_BRUKER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad.startSoknad;

@Named("soknadInnsendingRepository")
// marker alle metoder som transactional. Alle operasjoner vil skje i en
// transactional write context. Read metoder kan overstyre dette om det trengs.
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, readOnly = false)
public class SoknadRepositoryJdbc extends JdbcDaoSupport implements
		SoknadRepository {

	private static final Logger LOG = LoggerFactory
			.getLogger(SoknadRepositoryJdbc.class);

	public SoknadRepositoryJdbc() {
	}

	@Inject
	public void setDS(DataSource ds) {
		super.setDataSource(ds);
	}

	@Override
	public String opprettBehandling() {
		Long databasenokkel = getJdbcTemplate().queryForObject(
				selectNextSequenceValue("BRUKERBEH_ID_SEQ"), Long.class);
		String behandlingsId = lagBehandlingsId(databasenokkel);
		getJdbcTemplate()
				.update("insert into henvendelse (henvendelse_id, behandlingsid, type, opprettetdato) values (?, ?, ?, sysdate)",
						databasenokkel, behandlingsId, "SOKNADINNSENDING");
		return behandlingsId;
	}

	@Override
	public Long opprettSoknad(WebSoknad soknad) {
		Long databasenokkel = getJdbcTemplate().queryForObject(
				selectNextSequenceValue("SOKNAD_ID_SEQ"), Long.class);
		getJdbcTemplate()
				.update("insert into soknad (soknad_id, brukerbehandlingid, navsoknadid, aktorid, opprettetdato, status, delstegstatus) values (?,?,?,?,?,?,?)",
						databasenokkel, soknad.getBrukerBehandlingId(),
						soknad.getGosysId(), soknad.getAktoerId(),
						new Date(soknad.getOpprettetDato()),
						UNDER_ARBEID.name(), OPPRETTET.name());
		return databasenokkel;
	}

	@Override
	public WebSoknad hentSoknad(Long id) {
		String sql = "select * from SOKNAD where soknad_id = ?";
		return getJdbcTemplate().queryForObject(sql, new SoknadRowMapper(), id);
	}

	@Override
	public List<WebSoknad> hentListe(String aktorId) {
		String sql = "select * from soknad where aktorid = ? order by opprettetdato desc";
		return getJdbcTemplate().query(sql, new String[] { aktorId },
				new SoknadMapper());
	}

	@Override
	public WebSoknad hentSoknadMedData(Long id) {
		return hentSoknad(id).medBrukerData(hentAlleBrukerData(id));
	}

	@Override
	public WebSoknad hentMedBehandlingsId(String behandlingsId) {
		String sql = "select * from SOKNAD where brukerbehandlingid = ?";
		return getJdbcTemplate().queryForObject(sql, new SoknadRowMapper(),
				behandlingsId);
	}

	private int oppdaterBrukerData(long soknadId, Faktum faktum) {
		return getJdbcTemplate()
				.update("update soknadbrukerdata set value=? where key = ? and soknad_id = ? and soknadbrukerdata_id = ?",
						faktum.getValue(), faktum.getKey(), soknadId, faktum.getFaktumId());
	}

	@Override
	public Faktum hentFaktum(Long soknadId, Long faktumId) {
		String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and soknadbrukerdata_id = ?";
		return getJdbcTemplate().queryForObject(sql, soknadDataRowMapper, soknadId, faktumId);
	}
	
	/**
	 * Brukes for å se om systemfaktumet er lagret tidligere. 
	 * Returnerer faktumet dersom det eksisterer, Dersom ikke returneres et tomt faktum.
	 */
	@Override
	public Faktum hentSystemFaktum(Long soknadId, String key, String type) {
		String sql = "select * from SOKNADBRUKERDATA where soknad_id = ? and key = ? and type= ?";
		List<Faktum> faktum = getJdbcTemplate().query(sql, soknadDataRowMapper, soknadId, key, type);
		
		if(faktum.size() > 0)  {
			return faktum.get(0);
		} else {
			return new Faktum();
		}
	}

	@Override
	public Long lagreFaktum(long soknadId, Faktum faktum) {
		if (faktum.getFaktumId() == null) {
			Long dbNokkel = getJdbcTemplate().queryForObject(
					selectNextSequenceValue("SOKNAD_BRUKER_DATA_ID_SEQ"),
					Long.class);
			getJdbcTemplate()
					.update("insert into soknadbrukerdata (soknadbrukerdata_id, soknad_id, key, value, type, sistendret) values (?, ?, ?, ?, ?, sysdate)",
							dbNokkel, soknadId, faktum.getKey(),
							faktum.getValue(), faktum.getType());
			
			utfyllingStartet(soknadId);
			return dbNokkel;
		}else {
			oppdaterBrukerData(soknadId, faktum);
			return faktum.getFaktumId();
		}
		
	}

	private int utfyllingStartet(long soknadId) {
		return getJdbcTemplate().update(
				"update soknad set DELSTEGSTATUS = ? where soknad_id = ?",
				UTFYLLING.name(), soknadId);
	}

	@Override
	public List<Faktum> hentAlleBrukerData(Long soknadId) {
		return select("select * from SOKNADBRUKERDATA where soknad_id = ?",
				soknadId);
	}

	@Override
	public void avslutt(WebSoknad soknad) {
		LOG.debug("Setter status til søknad med id {} til ferdig",
				soknad.getSoknadId());
		String status = FERDIG.name();
		getJdbcTemplate().update(
				"update soknad set status = ? where soknad_id = ?", status,
				soknad.getSoknadId());
	}

	@Override
	public void avbryt(Long soknad) {
		LOG.debug("Setter status til søknad med id {} til avbrutt", soknad);
		String status = AVBRUTT_AV_BRUKER.name();
		getJdbcTemplate().update(
				"update soknad set status = ? where soknad_id = ?", status,
				soknad);
		getJdbcTemplate().update("delete from vedlegg where soknad_id = ?",
				soknad);
		getJdbcTemplate().update(
				"delete from soknadbrukerdata where soknad_id = ?", soknad);
	}

	private List<Faktum> select(String sql, Object... args) {
		return getJdbcTemplate().query(sql, args, soknadDataRowMapper);
	}

	private static class SoknadMapper implements RowMapper<WebSoknad> {
		public WebSoknad mapRow(ResultSet rs, int row) throws SQLException {
			return startSoknad()
					.medId(rs.getLong("soknad_id"))
					.medBehandlingId(rs.getString("brukerbehandlingid"))
					.medGosysId(rs.getString("navsoknadid"))
					.medAktorId(rs.getString("aktorid"))
					.opprettetDato(
							new DateTime(rs.getTimestamp("opprettetdato")
									.getTime()))
					.medStatus(
							SoknadInnsendingStatus.valueOf(rs
									.getString("status")));
		}
	}

	private final RowMapper<Faktum> soknadDataRowMapper = new RowMapper<Faktum>() {
		public Faktum mapRow(ResultSet rs, int rowNum) throws SQLException {

			Faktum faktum = new Faktum(rs.getLong("soknad_id"),
					rs.getLong("soknadbrukerdata_id"),
					rs.getString("key"), rs.getString("value"),
					rs.getString("type"));
			faktum.setVedleggId(rs.getLong("vedlegg_id"));
			return faktum;
		}
	};

}
