package com.applications.bobatea.config;

import com.applications.bobatea.controllers.FilesController;
import com.applications.bobatea.customExceptions.MinioClientException;
import com.applications.bobatea.services.MinIoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class StartupConfig {

    private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);
    private MinIoService minIoService;

    @Autowired
    public StartupConfig(MinIoService minIoService) {
        this.minIoService = minIoService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(){
        try {
            minIoService.CreateBucket();
        } catch (Exception e) {
            logger.error("Error creating bucket: {}", e.getMessage(), e);
        }
    }
}
