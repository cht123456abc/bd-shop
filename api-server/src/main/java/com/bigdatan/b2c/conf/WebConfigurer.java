package com.bigdatan.b2c.conf;

import com.bigdatan.b2c.interceptor.QqInterceptor;
import com.bigdatan.b2c.interceptor.QqWebInterceptor;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class WebConfigurer extends WebMvcConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new QqInterceptor())
                .addPathPatterns("/admin/*/**");
        registry.addInterceptor(new QqWebInterceptor())
                .addPathPatterns("/front/*/**").
                excludePathPatterns("/front/user/user/checkCode").
                excludePathPatterns("/front/user/user/sendCode");

        super.addInterceptors(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")//映射地址，/** 代表所有映射
                .allowedOrigins("*")//允许的源域，*不能和Credentials=true一起？
//                .allowedOrigins("http://127.0.0.1:4865")
                .allowCredentials(true)//允不允许cookie跨域
                .maxAge(3600)//预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
                .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE");
    }

    @Bean
   public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        ThreadPoolTaskExecutor  threadPoolTaskExecutor=new ThreadPoolTaskExecutor();
        //核心线程数
        threadPoolTaskExecutor.setCorePoolSize(10);
        //最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(15);
        //队列最大长度
        threadPoolTaskExecutor.setQueueCapacity(20);
        //线程池维护线程所允许的空闲时间
        threadPoolTaskExecutor.setKeepAliveSeconds(300);
        //主线程直接执行该任务，执行完之后尝试添加下一个任务到线程池中，可以有效降低向线程池内添加任务的速度
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return  threadPoolTaskExecutor;
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {

        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {

            @Override
            protected void postProcessContext(Context context) {

                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
        return tomcat;
    }

    private Connector initiateHttpConnector() {

        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}
