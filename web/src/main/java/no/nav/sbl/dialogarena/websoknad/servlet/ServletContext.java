package no.nav.sbl.dialogarena.websoknad.servlet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.util.Arrays;
import java.util.List;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
@EnableWebMvc
@EnableAsync
@ComponentScan(excludeFilters = @Filter(Configuration.class))
public class ServletContext extends WebMvcConfigurerAdapter {
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
        json.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
        ByteArrayHttpMessageConverter imageConverter = new ByteArrayHttpMessageConverter();
        imageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG));
        converters.add(json);
        converters.add(imageConverter);

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
