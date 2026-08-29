package com.liquidacionremates.app.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

@Component
public class BrowserLauncher {

    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() {
        // Desactiva el modo "headless" que Spring Boot trae por defecto,
        // permitiendo que Java interactúe con las ventanas de Windows.
        System.setProperty("java.awt.headless", "false");

        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI("http://localhost:8080/clients"));
            }
        } catch (Exception e) {
            System.out.println("Ocurrió un error al intentar abrir el navegador automáticamente.");
        }
    }
}