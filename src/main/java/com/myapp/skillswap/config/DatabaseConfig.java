package com.myapp.skillswap.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        // If H2 in-memory (e.g. during integration tests), configure directly
        if (rawUrl != null && rawUrl.startsWith("jdbc:h2:")) {
            HikariConfig h2Config = new HikariConfig();
            h2Config.setJdbcUrl(rawUrl);
            h2Config.setUsername(username != null ? username : "sa");
            h2Config.setPassword(password != null ? password : "");
            h2Config.setDriverClassName("org.h2.Driver");
            return new HikariDataSource(h2Config);
        }

        HikariConfig config = new HikariConfig();
        String jdbcUrl = rawUrl;
        String finalUsername = username;
        String finalPassword = password;

        try {
            String cleanUrl = rawUrl != null ? rawUrl.trim() : "";
            if (cleanUrl.startsWith("jdbc:")) {
                cleanUrl = cleanUrl.substring(5);
            }

            if (cleanUrl.startsWith("postgres://") || cleanUrl.startsWith("postgresql://")) {
                URI uri = new URI(cleanUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] parts = userInfo.split(":", 2);
                    if (finalUsername == null || finalUsername.isBlank() || "postgres".equalsIgnoreCase(finalUsername)) {
                        finalUsername = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    }
                    if (parts.length > 1 && (finalPassword == null || finalPassword.isBlank())) {
                        finalPassword = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    }
                }

                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String query = uri.getQuery();

                StringBuilder sb = new StringBuilder("jdbc:postgresql://").append(host);
                if (port != 5432 || uri.getPort() > 0) {
                    sb.append(":").append(port);
                }
                if (path != null && !path.isBlank()) {
                    sb.append(path);
                } else {
                    sb.append("/skillswap");
                }

                // Ensure sslmode=require for Neon / remote cloud databases
                if (query == null || query.isBlank()) {
                    sb.append("?sslmode=require");
                } else if (!query.contains("sslmode=")) {
                    sb.append("?").append(query).append("&sslmode=require");
                } else {
                    sb.append("?").append(query);
                }

                jdbcUrl = sb.toString();
            } else if (jdbcUrl != null && !jdbcUrl.startsWith("jdbc:")) {
                jdbcUrl = "jdbc:" + jdbcUrl;
            }
        } catch (Exception e) {
            log.warn("Could not parse datasource URL as URI, using original: {}", e.getMessage());
        }

        if (finalUsername == null || finalUsername.isBlank()) {
            finalUsername = "postgres";
        }
        if (finalPassword == null) {
            finalPassword = "";
        }

        log.info("Configuring PostgreSQL DataSource with URL: {}", sanitizeUrl(jdbcUrl));

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(finalUsername);
        config.setPassword(finalPassword);
        config.setDriverClassName("org.postgresql.Driver");

        // Resilience settings for cloud databases (especially Neon serverless wake-up)
        config.setConnectionTimeout(30000); // 30s timeout for cold start wake-up
        config.setMaxLifetime(600000);      // 10m connection max lifetime
        config.setIdleTimeout(300000);      // 5m idle timeout
        config.setMaximumPoolSize(5);       // Safe connection pool size for Neon
        config.setMinimumIdle(1);

        return new HikariDataSource(config);
    }

    private String sanitizeUrl(String url) {
        if (url == null) return null;
        return url.replaceAll(":[^/@:]+@", ":***@");
    }
}
