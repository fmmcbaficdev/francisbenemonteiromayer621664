package br.gov.mt.seplag.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Resposta para GET na raiz (/) — evita 403 ao acessar http://localhost:8080 no navegador.
 */
@RestController
public class WelcomeController {

    @Value("${server.port:8080}")
    private String serverPort;

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        String base = "http://localhost:" + serverPort;
        return ResponseEntity.ok(Map.of(
                "message", "SEPLAG Artistas e Álbuns API",
                "docs", base + "/swagger-ui.html",
                "health", base + "/actuator/health"
        ));
    }
}
