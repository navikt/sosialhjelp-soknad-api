package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.web.oidc.OidcTokenValidatorFilter;
import no.nav.sosialhjelp.soknad.web.saml.FakeLoginFilter;
import no.nav.sosialhjelp.soknad.web.saml.OpenAmLoginFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
public class FilterConfig {

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        return new CharacterEncodingFilter("UTF-8", true);
    }

    @Bean
    @Profile("!(mock-alt | test)")
    public FilterRegistrationBean<OpenAmLoginFilter> openAmLoginFilterRegistration() {
        FilterRegistrationBean<OpenAmLoginFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(openAmLoginFilter());
        registration.addUrlPatterns("/metadata/*");
        registration.setName("openAmLoginFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean(name = "openAmLoginFilter")
    @Profile("!(mock-alt | test)")
    public OpenAmLoginFilter openAmLoginFilter() {
        return new OpenAmLoginFilter();
    }

    @Bean
    @Profile("(mock-alt | test)")
    public FilterRegistrationBean<FakeLoginFilter> fakeLoginFilterRegistration() {
        FilterRegistrationBean<FakeLoginFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(fakeLoginFilter());
        registration.addUrlPatterns("/metadata/*");
        registration.addInitParameter("defaultFnr", "11111111111");
        registration.setName("openAmLoginFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean(name = "openAmLoginFilter")
    @Profile("(mock-alt | test)")
    public FakeLoginFilter fakeLoginFilter() {
        return new FakeLoginFilter();
    }

    @Bean
    @Profile("!(mock-alt | test)")
    public OidcTokenValidatorFilter oidcTokenValidatorFilter() {
        return new OidcTokenValidatorFilter();
    }
}
