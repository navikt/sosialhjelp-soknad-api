package no.nav.sosialhjelp.soknad.web.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Invokable;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class IntegrationConfig {

    private static final Logger log = getLogger(IntegrationConfig.class);

    @Bean
    public BeanFactoryPostProcessor mockMissingBeans(){
        return beanFactory -> {
            MOCKS.clear();
            try {
                ImmutableSet<ClassPath.ClassInfo> tjenester = ClassPath
                        .from(IntegrationConfig.class.getClassLoader())
                        .getTopLevelClasses("no.nav.sosialhjelp.soknad.consumer.wsconfig");
                log.info(tjenester.toString());
                for (ClassPath.ClassInfo classInfo : tjenester) {
                    for (Method method: classInfo.load().getMethods()) {
                        Invokable<?, Object> from = Invokable.from(method);
                        if(from.isAnnotationPresent(Bean.class)){
                            Object mock = mockClass(from.getReturnType().getRawType());
                            beanFactory.registerSingleton(from.getName(), mock);
                            MOCKS.put(from.getName(), mock);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            NavMessageSource mock = mock(NavMessageSource.class);
            String name = "navMessageSource";
            MOCKS.put(name, mock);
            Properties properties = mock(Properties.class);
            when(properties.getProperty(anyString())).thenReturn("mock");
            when(mock.getBundleFor(anyString(), any())).thenReturn(properties);
            beanFactory.registerSingleton(name, mock);
        };
    }

    private static Map<String, Object> MOCKS = new HashMap<>();

    private Object mockClass(Class<?> type) {
        Object mock = mock(type);
        log.info("mocking {}", type);
        return mock;
    }
    public static void resetMocks(){
        for (Object mock : MOCKS.values()) {
            reset(mock);
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T getMocked(String name){
        return (T) MOCKS.get(name);
    }
}
