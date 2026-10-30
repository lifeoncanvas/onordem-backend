package com.honouryourmentor.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * HikariCP Connection Pool Diagnostics & Monitoring
 * 
 * Provides metrics on active, idle, total connections and waiting threads.
 * Helps monitor pool health when high volumes of media files, letters, and
 * tributes are being uploaded and retrieved.
 */
@Slf4j
@Configuration
public class DatabasePoolConfig {

    private final DataSource dataSource;

    @Autowired
    public DatabasePoolConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        if (dataSource instanceof HikariDataSource hikari) {
            log.info("==========================================================");
            log.info("HikariCP Connection Pool Initialized for Neon PostgreSQL:");
            log.info("  Pool Name:             {}", hikari.getPoolName());
            log.info("  Maximum Pool Size:     {}", hikari.getMaximumPoolSize());
            log.info("  Minimum Idle:          {}", hikari.getMinimumIdle());
            log.info("  Connection Timeout:    {} ms", hikari.getConnectionTimeout());
            log.info("  Idle Timeout:          {} ms", hikari.getIdleTimeout());
            log.info("  Max Lifetime:          {} ms", hikari.getMaxLifetime());
            log.info("  Keepalive Time:        {} ms", hikari.getKeepaliveTime());
            log.info("  Leak Detection:        {} ms", hikari.getLeakDetectionThreshold());
            log.info("==========================================================");
        }
    }

    /**
     * Retrieve live snapshot of the connection pool metrics.
     */
    public Map<String, Object> getPoolMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        if (dataSource instanceof HikariDataSource hikari) {
            HikariPoolMXBean poolMxBean = hikari.getHikariPoolMXBean();
            metrics.put("poolName", hikari.getPoolName());
            metrics.put("maximumPoolSize", hikari.getMaximumPoolSize());
            metrics.put("minimumIdle", hikari.getMinimumIdle());

            if (poolMxBean != null) {
                metrics.put("activeConnections", poolMxBean.getActiveConnections());
                metrics.put("idleConnections", poolMxBean.getIdleConnections());
                metrics.put("totalConnections", poolMxBean.getTotalConnections());
                metrics.put("threadsAwaitingConnection", poolMxBean.getThreadsAwaitingConnection());
            }
            metrics.put("status", "HEALTHY");
        } else {
            metrics.put("status", "NON_HIKARI_DATASOURCE");
        }
        return metrics;
    }
}

