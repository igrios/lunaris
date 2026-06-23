package com.lunaris.ansenuza.infrastructure.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${storage.local-dir}")
    private String localDir;

    @Value("${storage.invoices-dir}")
    private String invoicesDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea la URL web a la carpeta real de tu Linux
        registry.addResourceHandler("/comprobantes/**")
                .addResourceLocations("file:" + localDir);

        // 🧾 PDFs de facturas subidos por la operadora
        registry.addResourceHandler("/facturas/**")
                .addResourceLocations("file:" + invoicesDir);
    }
}