package com.salon.booking.config;

import com.salon.booking.domain.ServiceOffering;
import com.salon.booking.domain.Staff;
import com.salon.booking.domain.StaffAvailability;
import com.salon.booking.repository.ServiceOfferingRepository;
import com.salon.booking.repository.StaffAvailabilityRepository;
import com.salon.booking.repository.StaffRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds a demo catalogue (services, stylists, working hours) on first run. */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ServiceOfferingRepository serviceRepo;
    private final StaffRepository staffRepo;
    private final StaffAvailabilityRepository availabilityRepo;

    public DataSeeder(ServiceOfferingRepository serviceRepo,
                      StaffRepository staffRepo,
                      StaffAvailabilityRepository availabilityRepo) {
        this.serviceRepo = serviceRepo;
        this.staffRepo = staffRepo;
        this.availabilityRepo = availabilityRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (serviceRepo.count() > 0) {
            return; // already seeded
        }
        log.info("Seeding demo salon data...");

        ServiceOffering haircut = service("Haircut", "Wash, cut and style", 30, 3000);
        ServiceOffering colour = service("Hair Colour", "Full colour treatment", 90, 8000);
        ServiceOffering beard = service("Beard Trim", "Shape and tidy", 15, 1500);
        ServiceOffering blowDry = service("Blow Dry", "Wash and blow dry", 45, 4000);
        serviceRepo.saveAll(List.of(haircut, colour, beard, blowDry));

        Staff alice = staff("Alice Johnson", "alice@salon.local", Set.of(haircut, colour, beard, blowDry));
        Staff bob = staff("Bob Smith", "bob@salon.local", Set.of(haircut, beard));
        Staff carol = staff("Carol Diaz", "carol@salon.local", Set.of(colour, blowDry, haircut));
        staffRepo.saveAll(List.of(alice, bob, carol));

        // Weekday hours for everyone; Alice also works Saturday mornings.
        for (Staff s : List.of(alice, bob, carol)) {
            weekdays(s, LocalTime.of(9, 0), LocalTime.of(17, 0));
        }
        availabilityRepo.save(window(alice, DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(14, 0)));

        log.info("Seed complete: {} services, {} stylists", serviceRepo.count(), staffRepo.count());
    }

    private ServiceOffering service(String name, String desc, int minutes, int priceCents) {
        ServiceOffering s = new ServiceOffering();
        s.setName(name);
        s.setDescription(desc);
        s.setDurationMinutes(minutes);
        s.setPriceCents(priceCents);
        s.setActive(true);
        return s;
    }

    private Staff staff(String name, String email, Set<ServiceOffering> services) {
        Staff s = new Staff();
        s.setName(name);
        s.setEmail(email);
        s.setActive(true);
        s.setServices(services);
        return s;
    }

    private void weekdays(Staff staff, LocalTime start, LocalTime end) {
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            availabilityRepo.save(window(staff, d, start, end));
        }
    }

    private StaffAvailability window(Staff staff, DayOfWeek day, LocalTime start, LocalTime end) {
        StaffAvailability a = new StaffAvailability();
        a.setStaff(staff);
        a.setDayOfWeek(day);
        a.setStartTime(start);
        a.setEndTime(end);
        return a;
    }
}
