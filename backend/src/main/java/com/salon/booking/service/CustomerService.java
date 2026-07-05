package com.salon.booking.service;

import com.salon.booking.domain.Customer;
import com.salon.booking.exception.BadRequestException;
import com.salon.booking.exception.NotFoundException;
import com.salon.booking.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    public Customer getById(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }

    /** Finds an existing customer by Google subject or email, or creates one. */
    @Transactional
    public Customer findOrCreateFromGoogle(String googleSub, String email, String name) {
        return customers.findByGoogleSub(googleSub)
                .or(() -> customers.findByEmail(email).map(existing -> {
                    // Link an existing (e.g. guest) account to this Google identity.
                    existing.setGoogleSub(googleSub);
                    return existing;
                }))
                .map(c -> {
                    if (name != null && !name.isBlank()) {
                        c.setName(name);
                    }
                    return customers.save(c);
                })
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setGoogleSub(googleSub);
                    c.setEmail(email);
                    c.setName(name != null && !name.isBlank() ? name : email);
                    return customers.save(c);
                });
    }

    /** Finds a customer by email or creates a lightweight guest record. */
    @Transactional
    public Customer findOrCreateGuest(String email, String name, String phone) {
        if (email == null || email.isBlank() || name == null || name.isBlank()) {
            throw new BadRequestException("Guest bookings require a name and email.");
        }
        String normalized = email.trim().toLowerCase();
        return customers.findByEmail(normalized)
                .map(c -> {
                    c.setName(name);
                    if (phone != null && !phone.isBlank()) {
                        c.setPhone(phone);
                    }
                    return customers.save(c);
                })
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setEmail(normalized);
                    c.setName(name);
                    c.setPhone(phone);
                    return customers.save(c);
                });
    }
}
