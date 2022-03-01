//package no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg;
//
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
//import org.springframework.stereotype.Component;
//
//import javax.inject.Inject;
//import javax.inject.Named;
//import javax.sql.DataSource;
//
///**
// * Repository for OpplastetVedlegg.
// * Operasjoner som kun er tiltenkt batch/schedulerte jobber.
// */
//@Named("BatchOpplastetVedleggRepository")
//@Component
//public class BatchOpplastetVedleggRepositoryJdbc extends NamedParameterJdbcDaoSupport implements BatchOpplastetVedleggRepository {
//
//    @Inject
//    public void setDS(DataSource ds) {
//        super.setDataSource(ds);
//    }
//
//    @Override
//    public void slettAlleVedleggForSoknad(Long soknadId) {
//        getJdbcTemplate()
//                .update("delete from OPPLASTET_VEDLEGG where SOKNAD_UNDER_ARBEID_ID = ?",
//                        soknadId);
//    }
//
//}
