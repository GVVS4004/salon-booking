package com.salon.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A service the salon offers (e.g. Haircut, Colour), with a fixed duration and price.
 */
@Entity
@Table(name = "services")
@Getter
@Setter
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    /** Price in minor currency units (e.g. cents/paise) to avoid floating point. */
    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(nullable = false)
    private boolean active = true;
}
