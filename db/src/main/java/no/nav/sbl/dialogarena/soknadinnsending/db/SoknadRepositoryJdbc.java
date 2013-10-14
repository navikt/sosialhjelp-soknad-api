package no.nav.sbl.dialogarena.soknadinnsending.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@Named
public class SoknadRepositoryJdbc implements SoknadRepository{
	
	private static final Logger LOG = LoggerFactory.getLogger(SoknadRepositoryJdbc.class);

	
	@Inject
	private JdbcTemplate db;
	
	public SoknadRepositoryJdbc() {}
	
	public SoknadRepositoryJdbc(DataSource ds) {
		this.db = new JdbcTemplate(ds);
	}


    @Override
    public String opprettBehandling() {
		Long databasenokkel = db.queryForObject(SQLUtils.selectNextSequenceValue("BRUKERBEH_ID_SEQ"), Long.class);
		System.out.println("db nokkel " + databasenokkel);
		String behandlingsId = IdGenerator.lagBehandlingsId(databasenokkel);
		db.update("insert into henvendelse (henvendelse_id, behandlingsid, type, opprettetdato) values (?, ?, ?, sysdate)", databasenokkel, behandlingsId,"SOKNADINNSENDING");
		return behandlingsId;
	}
    
 	@Override
    public Long opprettSoknad(WebSoknad soknad) {
        Long databasenokkel = db.queryForObject(SQLUtils.selectNextSequenceValue("SOKNAD_ID_SEQ"), Long.class);
        System.out.println("db nokkel " + databasenokkel);
        db.update("insert into soknad (soknad_id, brukerbehandlingid, navsoknadid, aktorid, opprettetdato, status) values (?,?,?,?,?,?)", 
        		databasenokkel, soknad.getBrukerbehandlingId(), soknad.getNavSoknadId(), soknad.getAktoerId(), 
        		soknad.getOpprettetDato().toDate(), SoknadInnsendingStatus.UNDER_ARBEID.name());
        return databasenokkel;
    }

    @Override
    public WebSoknad hentSoknad(Long id) {
        String sql = "select * from SOKNAD where soknad_id = ?";
        return db.queryForObject(sql, new SoknadRowMapper(), id);
    }

    @Override
    public List<WebSoknad> hentListe(String aktorId) {
        String sql = "select * from soknad where aktorid = ? order by opprettetdato desc";
        return db.query(sql, new String[]{aktorId}, new SoknadMapper());
    }

    @Override
    public WebSoknad hentSoknadMedData(Long id) {
        return hentSoknad(id).medBrukerData(hentAlleBrukerData(id));
    }

    @Override
	public WebSoknad hentMedBehandlingsId(String behandlingsId) {
    	String sql = "select * from SOKNAD where brukerbehandlingid = ?";
        return db.queryForObject(sql, new SoknadRowMapper(),behandlingsId);
	}

    private int oppdaterBrukerData(long soknadId, Faktum faktum) {
    	return db.update("update soknadbrukerdata set value=? where key = ? and soknad_id = ?", faktum.getValue(), faktum.getKey(), soknadId);
    }
    @Override
    public void lagreFaktum(long soknadId, Faktum faktum) {
        Long dbNokkel = db.queryForObject(SQLUtils.selectNextSequenceValue("SOKNAD_BRUKER_DATA_ID_SEQ"), Long.class);
        if (oppdaterBrukerData(soknadId, faktum) == 0) {
        	db.update("insert into soknadbrukerdata (soknadbrukerdata_id, soknad_id, key, value, type, sistendret) values (?, ?, ?, ?, ?, sysdate)",
        		dbNokkel, soknadId, faktum.getKey(), faktum.getValue(), faktum.getType());
        }
    }
    

    @Override
    public List<Faktum> hentAlleBrukerData(Long soknadId) {
        return select("select * from SOKNADBRUKERDATA where soknad_id = ?", soknadId);
    }


    @Override
    public void avslutt(WebSoknad soknad) {
    	LOG.debug("Setter status til søknad med id {} til ferdig", soknad.getSoknadId());
    	String status = SoknadInnsendingStatus.FERDIG.name();
        db.update("update soknad set status = ? where soknad_id = ?",status, soknad.getSoknadId()); 
    }

    
    @Override
    public void avbryt(WebSoknad soknad) {
        LOG.debug("Setter status til søknad med id {} til avbrutt", soknad.getSoknadId());
        String status = SoknadInnsendingStatus.AVBRUTT_AV_BRUKER.name();
        db.update("update soknad set status = ? where soknad_id = ?",status, soknad.getSoknadId());
        db.update("delete from soknadbrukerdata where soknad_id = ?",soknad.getSoknadId());
    }

    private List<Faktum> select(String sql, Object... args) {
        return db.query(sql, args, rowMapper);
    }
	
	private static class SoknadMapper implements RowMapper<WebSoknad> {
		public WebSoknad mapRow(ResultSet rs, int row) throws SQLException  {
			return WebSoknad.startSoknad()
					.medId(rs.getLong("soknad_id"))
					.medBehandlingId(rs.getString("brukerbehandlingid"))
					.medGosysId(rs.getString("navsoknadid"))
					.medAktorId(rs.getString("aktorid"))
					.opprettetDato(new DateTime(rs.getTimestamp("opprettetdato").getTime()))
					.medStatus(SoknadInnsendingStatus.valueOf(rs.getString("status")));
		}
	}

    private final RowMapper<Faktum> rowMapper = new RowMapper<Faktum>() {
        public Faktum mapRow(ResultSet rs, int rowNum) throws SQLException {

            return new Faktum(rs.getLong("soknadbrukerdata_id"), rs.getLong("soknad_id"), rs.getString("key"), 
            		rs.getString("value"), rs.getString("type"),
                    new DateTime(rs.getTimestamp("sistendret").getTime()));
        }
    };
}
