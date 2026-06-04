package com.api.sindigo.core.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

@Slf4j
@Component
public class SecretsValidator {

	@Value("${app.jwt.secret:}")
	private String jwtSecret;

	@Value("${app.admin.secret-key:}")
	private String adminSecretKey;

	private final Environment environment;

	public SecretsValidator(Environment environment) {
		this.environment = environment;
	}

	@PostConstruct
	public void validateSecrets() {
		validateJwtSecret();
		validateAdminSecret();
	}

	private void validateJwtSecret() {
		if (jwtSecret == null || jwtSecret.isEmpty()) {
			log.error("❌ ERRO CRÍTICO: APP_JWT_SECRET não está configurado!");
			throw new IllegalStateException("APP_JWT_SECRET environment variable is required");
		}

		if (jwtSecret.contains("your-super-secret-key") || jwtSecret.length() < 32) {
			log.warn("⚠️ AVISO DE SEGURANÇA: JWT_SECRET fraco detectado!");
			log.warn("   Configure APP_JWT_SECRET com valor forte (mínimo 32 caracteres)");
			if (isProduction()) {
				throw new IllegalStateException("JWT_SECRET é muito fraco para produção");
			}
		}
	}

	private void validateAdminSecret() {
		if (adminSecretKey == null || adminSecretKey.isEmpty()) {
			log.error("❌ ERRO CRÍTICO: APP_ADMIN_SECRET_KEY não está configurado!");
			throw new IllegalStateException("APP_ADMIN_SECRET_KEY environment variable is required");
		}

		if (adminSecretKey.contains("sindigo-hash-teste") || adminSecretKey.length() < 16) {
			log.warn("⚠️ AVISO DE SEGURANÇA: ADMIN_SECRET_KEY fraco detectado!");
			log.warn("   Configure APP_ADMIN_SECRET_KEY com valor forte (mínimo 16 caracteres)");
			if (isProduction()) {
				throw new IllegalStateException("ADMIN_SECRET_KEY é muito fraco para produção");
			}
		}
	}

	private boolean isProduction() {
		String[] profiles = environment.getActiveProfiles();
		return Arrays.asList(profiles).contains("prod") || Arrays.asList(profiles).contains("production");
	}
}


