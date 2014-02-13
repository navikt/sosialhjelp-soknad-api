package no.nav.sbl.dialogarena.soknadinnsending.web;

import no.nav.modig.test.util.FilesAndDirs;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;

@Configuration
@EnableWebMvc
public class ResourceServlet extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File libs = new File(FilesAndDirs.PROJECT_BASEDIR, "../frontend/");
        String frontendPath = libs.toURI().toString();
        String javascriptPath = frontendPath + "js/";
        registry.addResourceHandler("/js/**").addResourceLocations(javascriptPath);

    }
}
