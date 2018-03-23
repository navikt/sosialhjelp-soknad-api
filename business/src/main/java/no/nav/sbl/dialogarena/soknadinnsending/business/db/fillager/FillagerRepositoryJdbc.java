package no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.List;

@Component
@Transactional
public class FillagerRepositoryJdbc extends NamedParameterJdbcDaoSupport implements FillagerRepository {

    private static RowMapper<Fil> rowMapper = (rs, rowNum) -> new Fil(
            rs.getString("behandlingsid"),
            rs.getString("uuid"),
            rs.getBytes("data"),
            rs.getString("eier")
    );

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public void lagreFil(Fil fil) {
        Fil eksisterende = hentFil(fil.uuid);

        if (eksisterende != null) {
            if (!eksisterende.eier.equals(fil.eier)) {
                throw new RuntimeException("Ny fil tilhører ikke eksisterende eier");
            }
            getJdbcTemplate().update("UPDATE fillager SET data = ? WHERE uuid = ?",
                    ps -> {
                        ps.setBytes(1, fil.data);
                        ps.setString(2, fil.uuid);
                    });
        } else {
            getJdbcTemplate().update("INSERT INTO fillager (behandlingsid, uuid, data, eier) VALUES (?,?,?,?)",
                    ps -> {
                        ps.setString(1, fil.behandlingsId);
                        ps.setString(2, fil.uuid);
                        ps.setBytes(3, fil.data);
                        ps.setString(4, fil.eier);
                    });
        }

    }

    @Override
    public Fil hentFil(String uuid) {
        List<Fil> resultat = getJdbcTemplate().query("SELECT behandlingsid, uuid, data, eier FROM fillager WHERE uuid = ?",
                rowMapper, uuid);
        if (!resultat.isEmpty()) {
            return resultat.get(0);
        }
        return null;
    }

    @Override
    public void slettFil(String uuid) {
        getJdbcTemplate().update("DELETE from fillager WHERE uuid = ?", uuid);
    }

    @Override
    public List<Fil> hentFiler(String behandlingsId) {
        return getJdbcTemplate().query("SELECT behandlingsid, uuid, data, eier FROM fillager WHERE behandlingsid = ?",
                rowMapper, behandlingsId);
    }

    @Override
    public void slettAlle(String behandlingsId) {
        getJdbcTemplate().update("DELETE from fillager WHERE behandlingsid = ?", behandlingsId);
    }
}
