//package no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg;
//
//import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
//import no.nav.sosialhjelp.soknad.domain.VedleggType;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
//import org.springframework.stereotype.Component;
//
//import javax.inject.Inject;
//import javax.inject.Named;
//import javax.sql.DataSource;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Optional;
//
//@Named("OpplastetVedleggRepository")
//@Component
//public class OpplastetVedleggRepositoryJdbc extends NamedParameterJdbcDaoSupport implements OpplastetVedleggRepository {
//
//    @Inject
//    public void setDS(DataSource ds) {
//        super.setDataSource(ds);
//    }
//
//    @Override
//    public Optional<OpplastetVedlegg> hentVedlegg(String uuid, String eier) {
//        return getJdbcTemplate().query("select * from OPPLASTET_VEDLEGG where EIER = ? and UUID = ?",
//                new OpplastetVedleggRowMapper(), eier, uuid).stream().findFirst();
//    }
//
//    @Override
//    public List<OpplastetVedlegg> hentVedleggForSoknad(Long soknadId, String eier) {
//        return getJdbcTemplate().query("select * from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
//                new OpplastetVedleggRowMapper(), eier, soknadId);
//    }
//
//    @Override
//    public String opprettVedlegg(OpplastetVedlegg opplastetVedlegg, String eier) {
//        if (eier == null || !eier.equalsIgnoreCase(opplastetVedlegg.getEier())) {
//            throw new RuntimeException("Eier stemmer ikke med vedleggets eier");
//        }
//        getJdbcTemplate()
//                .update("insert into OPPLASTET_VEDLEGG (UUID, EIER, TYPE, DATA, SOKNAD_UNDER_ARBEID_ID, FILNAVN, SHA512)" +
//                                " values (?,?,?,?,?,?,?)",
//                        opplastetVedlegg.getUuid(),
//                        opplastetVedlegg.getEier(),
//                        opplastetVedlegg.getVedleggType().getSammensattType(),
//                        opplastetVedlegg.getData(),
//                        opplastetVedlegg.getSoknadId(),
//                        opplastetVedlegg.getFilnavn(),
//                        opplastetVedlegg.getSha512());
//        return opplastetVedlegg.getUuid();
//    }
//
//    @Override
//    public void slettVedlegg(String uuid, String eier) {
//        getJdbcTemplate()
//                .update("delete from OPPLASTET_VEDLEGG where EIER = ? and UUID = ?",
//                        eier,
//                        uuid);
//    }
//
//    @Override
//    public void slettAlleVedleggForSoknad(Long soknadId, String eier) {
//        getJdbcTemplate()
//                .update("delete from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
//                        eier,
//                        soknadId);
//    }
//
//    @Override
//    public Integer hentSamletVedleggStorrelse(Long soknadId, String eier) {
//        if (getJdbcTemplate()
//                .queryForObject("select count(*) from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?", Integer.class, eier, soknadId) > 0) {
//            String sql = "select sum(dbms_lob.getLength(DATA)) from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?";
//            Integer totalSize = getJdbcTemplate()
//                    .queryForObject(sql, Integer.class, eier, soknadId);
//            return totalSize;
//        }
//        return 0;
//    }
//
//    public class OpplastetVedleggRowMapper implements RowMapper<OpplastetVedlegg> {
//
//        public OpplastetVedlegg mapRow(ResultSet rs, int rowNum) throws SQLException {
//            return new OpplastetVedlegg()
//                    .withUuid(rs.getString("uuid"))
//                    .withEier(rs.getString("eier"))
//                    .withVedleggType(new VedleggType(rs.getString("type")))
//                    .withData(rs.getBytes("data"))
//                    .withSoknadId(rs.getLong("soknad_under_arbeid_id"))
//                    .withFilnavn(rs.getString("filnavn"))
//                    .withSha512(rs.getString("sha512"));
//        }
//    }
//}
