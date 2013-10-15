package no.nav.sbl.dialogarena.soknadinnsending.db.config;

import javax.sql.DataSource;

import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadRepositoryJdbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jndi.JndiObjectFactoryBean;


@Configuration
@ComponentScan(basePackageClasses=SoknadRepositoryJdbc.class)
public class SoknadInnsendingDBConfig {

	@Bean 
	public SoknadRepository soknadInnsendingRepository() {
		return new SoknadRepositoryJdbc();
	}
	
	@Bean
    public JndiObjectFactoryBean dataSourceJndiLookup() {
		JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
		bean.setJndiName("java:jboss/datasources/SoknadInnsendingDS");
		return bean;
	}

}
