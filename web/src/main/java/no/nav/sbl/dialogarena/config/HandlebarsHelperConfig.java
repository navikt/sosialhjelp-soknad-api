package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.service.HandleBarKjoerer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = HandleBarKjoerer.class)
public class HandlebarsHelperConfig {

}