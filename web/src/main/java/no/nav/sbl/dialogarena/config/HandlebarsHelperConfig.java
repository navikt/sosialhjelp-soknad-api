package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = RegistryAwareHelper.class)
public class HandlebarsHelperConfig {

}

