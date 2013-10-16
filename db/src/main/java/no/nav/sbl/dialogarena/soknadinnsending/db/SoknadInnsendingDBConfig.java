package no.nav.sbl.dialogarena.soknadinnsending.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

@Configuration
@ComponentScan
public class SoknadInnsendingDBConfig {
	
	@Bean
    public JndiObjectFactoryBean dataSourceJndiLookup() {
		JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
		bean.setJndiName("java:jboss/datasources/SoknadInnsendingDS");
		return bean;
	}
}
