package com.x.customer.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates the shared customer used for sales without a registered customer. */
@Component
@RequiredArgsConstructor
public class WalkInCustomerInitializer implements ApplicationRunner {

    private static final String WALK_IN_CODE = "WALK-IN";
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String originalSqlMode = (String) entityManager
                .createNativeQuery("SELECT @@SESSION.sql_mode")
                .getSingleResult();
        String sqlMode = originalSqlMode == null ? "" : originalSqlMode;
        if (!sqlMode.toUpperCase().contains("NO_AUTO_VALUE_ON_ZERO")) {
            sqlMode = sqlMode.isBlank() ? "NO_AUTO_VALUE_ON_ZERO" : sqlMode + ",NO_AUTO_VALUE_ON_ZERO";
        }

        try {
            entityManager.createNativeQuery("SET SESSION sql_mode = :sqlMode")
                    .setParameter("sqlMode", sqlMode)
                    .executeUpdate();
            entityManager.createNativeQuery("""
                    INSERT INTO customers
                        (id, user_id, business_id, store_id, customer_code, full_name, status, created_at, updated_at)
                    VALUES (0, NULL, 0, NULL, :code, :name, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    ON DUPLICATE KEY UPDATE
                        customer_code = VALUES(customer_code),
                        full_name = VALUES(full_name),
                        status = 1,
                        updated_at = CURRENT_TIMESTAMP
                    """)
                    .setParameter("code", WALK_IN_CODE)
                    .setParameter("name", "Walk-in customer")
                    .executeUpdate();
        } finally {
            entityManager.createNativeQuery("SET SESSION sql_mode = :sqlMode")
                    .setParameter("sqlMode", originalSqlMode == null ? "" : originalSqlMode)
                    .executeUpdate();
        }
    }
}
