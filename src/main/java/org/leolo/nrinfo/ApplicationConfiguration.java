package org.leolo.nrinfo;

import org.leolo.nrinfo.interceptor.AuthInterceptor;
import org.leolo.nrinfo.interceptor.WebAuthInterceptor;
import org.leolo.nrinfo.service.AuthenticationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class ApplicationConfiguration implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired private WebAuthInterceptor webAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(webAuthInterceptor).addPathPatterns("/**").excludePathPatterns("/api/**");
    }
}
