package no.nav.sbl.dialogarena.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class NAVHickaryDatasource {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        config.setJdbcUrl(System.getProperty("db.url"));
        config.setUsername(System.getProperty("db.username"));
        config.setPassword(System.getProperty("db.password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }
;

    public NAVHickaryDatasource() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }


    public static javax.sql.DataSource getDataSource() {

        return ds;
    }
}