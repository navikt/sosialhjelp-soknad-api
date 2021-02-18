package no.nav.sosialhjelp.soknad.web.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Invokable;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class IntegrationConfig {

    @Bean
    public BeanFactoryPostProcessor mockMissingBeans(){
        return beanFactory -> {
            MOCKS.clear();
            try {
                ImmutableSet<ClassPath.ClassInfo> tjenester = ClassPath
                        .from(IntegrationConfig.class.getClassLoader())
                        .getTopLevelClasses("no.nav.sosialhjelp.soknad.consumer.wsconfig");
                System.out.println(tjenester);
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


            NavMessageSource mock = Mockito.mock(NavMessageSource.class);
            String name = "navMessageSource";
            MOCKS.put(name, mock);
            Properties properties = Mockito.mock(Properties.class);
            Mockito.when(properties.getProperty(Mockito.anyString())).thenReturn("mock");
            Mockito.when(mock.getBundleFor(Mockito.anyString(), Mockito.any())).thenReturn(properties);
            beanFactory.registerSingleton(name, mock);
        };
    }

    private static Map<String, Object> MOCKS = new HashMap<>();

    private Object mockClass(Class<?> type) throws Exception {
        Object mock = Mockito.mock(type);
        System.out.println("mocking " + type);
        return mock;
    }
    public static void resetMocks(){
        for (Object mock : MOCKS.values()) {
            Mockito.reset(mock);
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T getMocked(String name){
        return (T) MOCKS.get(name);
    }
}
