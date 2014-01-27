package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SikkerhetsAspect;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.Tilgangskontroll;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
@EnableWebMvc
@EnableAsync
@ComponentScan(excludeFilters = @Filter(Configuration.class))
@EnableAspectJAutoProxy
public class ServletContext extends WebMvcConfigurerAdapter {
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
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter();
        json.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        ByteArrayHttpMessageConverter imageConverter = new ByteArrayHttpMessageConverter();
        imageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG));
        StringHttpMessageConverter http = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        http.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_HTML));
        converters.add(json);
        converters.add(imageConverter);
        converters.add(http);

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor cache = new WebContentInterceptor();
        cache.setCacheSeconds(9999999);
        cache.setUseCacheControlHeader(false);
        cache.setUseCacheControlNoStore(false);
        registry.addInterceptor(cache).addPathPatterns("/**/thumbnail*");
    }
}
