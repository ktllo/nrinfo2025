package org.leolo.nrinfo;

import org.leolo.nrinfo.interceptor.AuthInterceptor;
import org.leolo.nrinfo.interceptor.WebAuthInterceptor;
import org.leolo.nrinfo.service.AuthenticationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

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
