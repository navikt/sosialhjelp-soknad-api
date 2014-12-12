package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SikkerhetsAspect;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.Tilgangskontroll;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.nio.charset.Charset;
import java.util.List;

import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.IMAGE_JPEG;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@Configuration
@EnableWebMvc
@EnableAsync
@EnableAspectJAutoProxy
@Import({
        ConfigController.class,
        LandController.class,
        ExceptionController.class,
        FaktaController.class,
        FortsettSenereController.class,
        MessageController.class,
        SoknadBekreftelseController.class,
        SoknadDataController.class,
        SoknadTpsDataController.class,
        UtslagskriterierController.class,
        ServiceConfig.class,
        VedleggController.class
})
public class ServletConfig extends WebMvcConfigurerAdapter {
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocalOverride(true);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public SikkerhetsAspect sikkerhet() {
        return new SikkerhetsAspect();
    }

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }

    @Bean
    public TaskExecutor thumbnailExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        return threadPoolTaskExecutor;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        converters.addAll(asList(
                opprettJsontyper(),
                opprettImagetyper(),
                opprettHttptyper()
        ));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor cache = new WebContentInterceptor();
        cache.setCacheSeconds(9999999);
        cache.setUseCacheControlHeader(false);
        cache.setUseCacheControlNoStore(false);
        registry.addInterceptor(cache).addPathPatterns("/**/thumbnail*");
    }

    private StringHttpMessageConverter opprettHttptyper() {
        StringHttpMessageConverter http = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        http.setSupportedMediaTypes(asList(TEXT_HTML));
        return http;
    }

    private ByteArrayHttpMessageConverter opprettImagetyper() {
        ByteArrayHttpMessageConverter imageConverter = new ByteArrayHttpMessageConverter();
        imageConverter.setSupportedMediaTypes(asList(IMAGE_PNG, IMAGE_JPEG, APPLICATION_OCTET_STREAM));
        return imageConverter;
    }

    private MappingJackson2HttpMessageConverter opprettJsontyper() {
        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setSupportedMediaTypes(asList(APPLICATION_JSON, TEXT_PLAIN));
        return json;
    }

}
