package com.x.customer.config;

import com.x.customer.entity.Customer;
import com.x.customer.entity.PhoneNumbers;
import com.x.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/** Fills the lookup form of phone numbers saved before customers could be found by phone. */
@Component
@RequiredArgsConstructor
public class PhoneNumberBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PhoneNumberBackfill.class);
    private static final int BATCH_SIZE = 500;
    private final CustomerRepository customerRepository;

    @Override
    public void run(ApplicationArguments args) {
        long lastId = -1;
        int updated = 0;
        List<Customer> batch;
        do {
            batch = customerRepository.findByIdGreaterThanAndPhoneIsNotNullAndPhoneNormalizedIsNullOrderByIdAsc(
                    lastId, PageRequest.of(0, BATCH_SIZE));
            for (Customer customer : batch) {
                lastId = customer.getId();
                customer.setPhoneNormalized(PhoneNumbers.normalize(customer.getPhone()));
            }
            customerRepository.saveAll(batch);
            updated += batch.size();
        } while (batch.size() == BATCH_SIZE);
        if (updated > 0) {
            log.info("Prepared {} customer phone numbers for lookup", updated);
        }
    }
}
