package com.salon.booking.service;

import com.salon.booking.domain.Appointment;
import com.salon.booking.domain.Customer;
import com.salon.booking.domain.ServiceOffering;
import com.salon.booking.domain.Staff;
import com.salon.booking.domain.StaffAvailability;
import com.salon.booking.dto.AppointmentDto;
import com.salon.booking.dto.AvailabilityDto;
import com.salon.booking.dto.CustomerDto;
import com.salon.booking.dto.ServiceDto;
import com.salon.booking.dto.StaffDto;
import java.util.List;

/** Pure entity -> DTO conversions. */
public final class Mapper {

    private Mapper() {
    }

    public static ServiceDto toDto(ServiceOffering s) {
        return new ServiceDto(s.getId(), s.getName(), s.getDescription(),
                s.getDurationMinutes(), s.getPriceCents(), s.isActive());
    }

    public static StaffDto toDto(Staff staff) {
        List<Long> serviceIds = staff.getServices().stream().map(ServiceOffering::getId).sorted().toList();
        return new StaffDto(staff.getId(), staff.getName(), staff.getEmail(), staff.isActive(), serviceIds);
    }

    public static AvailabilityDto toDto(StaffAvailability a) {
        return new AvailabilityDto(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime());
    }

    public static CustomerDto toDto(Customer c) {
        return new CustomerDto(c.getId(), c.getName(), c.getEmail(), c.getPhone());
    }

    public static AppointmentDto toDto(Appointment a) {
        ServiceOffering svc = a.getService();
        Staff staff = a.getStaff();
        Customer cust = a.getCustomer();
        return new AppointmentDto(
                a.getId(),
                svc.getId(), svc.getName(), svc.getDurationMinutes(), svc.getPriceCents(),
                staff.getId(), staff.getName(),
                cust.getId(), cust.getName(), cust.getEmail(), cust.getPhone(),
                a.getStartTime(), a.getEndTime(),
                a.getStatus().name(), a.getNotes());
    }
}
