package cloud.on.vaadin.flow

import com.vaadin.flow.spring.SpringServlet
import grails.plugins.Plugin
import grails.util.Environment
import grails.util.Holders
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VaadinFlowGrailsPlugin extends Plugin {

    @Bean
    ServletRegistrationBean springServletBean() {
        final ApplicationContext context = getApplicationContext()
        Map vaadinConfig = ((ConfigObject) getGrailsApplication().getConfig().vaadin).flatten()
        String urlMapping
        urlMapping = (vaadinConfig['urlMapping'] ?: "").toString()
        if (!urlMapping) urlMapping = '/*'
        ServletRegistrationBean bean
        if (urlMapping == '/*') {
            bean = new ServletRegistrationBean(new SpringServlet(context, true), urlMapping)
        } else {
            String[] mappings = [urlMapping, '/frontend/*', '/VAADIN/*']
            bean = new ServletRegistrationBean(new SpringServlet(context, false), mappings)
        }
        bean.setAsyncSupported((boolean) (vaadinConfig['asyncSupported'] ?: false))
        return bean
    }

    Closure doWithSpring() {
        { ->
            loadSecurityConfig()
        }
    }


    @Override
    void onChange(Map<String, Object> event) {
        loadSecurityConfig()
    }

    static ConfigObject getVaadinConfig() {
        return (ConfigObject) Holders.grailsApplication.config.vaadin
    }

    private static void mergeConfig(ConfigObject currentConfig, String className) {
        ConfigObject secondary = new ConfigSlurper(Environment.current.name).parse(
                new GroovyClassLoader(this.classLoader).loadClass(className))
        mergeConfig(currentConfig, secondary.vaadin as ConfigObject)
    }

    private static void mergeConfig(ConfigObject currentConfig, ConfigObject secondary) {
        (secondary ?: new ConfigObject()).merge(currentConfig ?: new ConfigObject()) as ConfigObject
    }

    private static void loadSecurityConfig() {
        mergeConfig(vaadinConfig, 'DefaultVaadinFlowConfig')
    }

}