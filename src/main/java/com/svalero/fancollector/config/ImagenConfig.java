package com.svalero.fancollector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class ImagenConfig implements WebMvcConfigurer {

    private static final String RUTA_UPLOADS =
            "C:\\Users\\Nerea\\Desktop\\Proyectos\\FanCollector-Proyecto\\Uploads";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ruta = Paths.get(RUTA_UPLOADS).toAbsolutePath().toString().replace("\\", "/");
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("file:" + ruta);
    }
}