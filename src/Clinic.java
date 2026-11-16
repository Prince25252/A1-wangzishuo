import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Holds the clinic's whole state: the staff, the patient register and the
 * appointment manager.
 *
 * <p>The class exists so that the interactive console, the statistics report and
 * the scripted demonstration all read the same objects instead of building their
 * own copies. It also keeps the domain logic - who is on staff, which slots are
 * still free, how busy the clinic is - out of the menu class, which only formats
 * what it is given.</p>
 *
 * @author Student
 * @version 1.1
 */
public class Clinic {

    /** Every professional employed by the clinic. */
    private final List<MedicalProfessional> staff;

    /** The booking engine shared by every screen of the console. */
    private final AppointmentManager manager;

    /** Registered patients, searched by phone number. */
    private final List<Patient> patients;

    /**
     * Builds the clinic with its staff, an empty appointment book and an empty
     * patient register.
     */
    public Clinic() {
        this.staff = createStaff();
        this.manager = new AppointmentManager();
        this.patients = new ArrayList<>();
    }

    /**
     * Creates the clinic's staff roster.
     *
     * @return every professional employed by the clinic
     */
    private static List<MedicalProfessional> createStaff() {
        Set<DayOfWeek> weekdays = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        Set<DayOfWeek> extended = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);

        List<MedicalProfessional> staff = new ArrayList<>();

        staff.add(new GeneralPractitioner("1001", "Dr Alice Chan", weekdays, "Room 1", 24));
        staff.add(new GeneralPractitioner("1002", "Dr Ben Okafor", extended, "Room 2", 20));
        staff.add(new SpecialistDoctor("2001", "Dr Priya Raman", weekdays, "Cardiology", 180.00));
        staff.add(new SpecialistDoctor("2002", "Dr Luis Moreno", weekdays, "Dermatology", 150.00));
        staff.add(new Nurse("3001", "Nurse Grace Tan", weekdays, "Ward A", 11));
        staff.add(new Nurse("3002", "Nurse Sam Whitfield", extended, "Ward B", 2));
        staff.add(new Dietitian("4001", "Ms Hazel Brooks", weekdays, "Diabetes management", 45));
        staff.add(new Physiotherapist("5001", "Mr Theo Nguyen", weekdays, "Sports rehabilitation", 3));

        return staff;
    }

    /**
     * Reads the staff roster.
     *
     * @return the staff list; modify it only through the clinic's own methods
     */
    public List<MedicalProfessional> getStaff() {
        return staff;
    }

    /**
     * Reads the booking engine.
     *
     * @return the shared appointment manager
     */
    public AppointmentManager getManager() {
        return manager;
    }

    /**
     * Reads the patient register.
     *
     * @return every registered patient
     */
    public List<Patient> getPatients() {
        return patients;
    }

    /**
     * Finds a professional by identifier.
     *
     * @param staffId the numeric identifier typed by the user
     * @return the matching professional, or an empty result when nobody matches
     */
    public Optional<MedicalProfessional> findProfessionalById(String staffId) {
        if (staffId == null || staffId.isBlank()) {
            return Optional.empty();
        }
        String target = staffId.trim();
        for (MedicalProfessional professional : staff) {
            if (professional.getStaffId().equals(target)) {
                return Optional.of(professional);
            }
        }
        return Optional.empty();
    }

    /**
     * Searches the staff by a fragment of a name or of a role title, which
     * makes the roster easy to browse without scrolling the whole list.
     *
     * @param keyword the fragment to look for, case-insensitive
     * @return every professional whose name or role contains the fragment
     */
    public List<MedicalProfessional> searchStaff(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>(staff);
        }
        String needle = keyword.trim().toLowerCase();
        List<MedicalProfessional> matches = new ArrayList<>();
        for (MedicalProfessional professional : staff) {
            if (professional.getName().toLowerCase().contains(needle)
                    || professional.getRole().toLowerCase().contains(needle)) {
                matches.add(professional);
            }
        }
        return matches;
    }

    /**
     * Finds a registered patient by phone number.
     *
     * @param phoneNumber the ten-digit number
     * @return the matching patient, or an empty result when nobody matches
     */
    public Optional<Patient> findPatientByPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return Optional.empty();
        }
        String target = phoneNumber.replace(" ", "").trim();
        for (Patient patient : patients) {
            if (patient.getPhoneNumber().equals(target)) {
                return Optional.of(patient);
            }
        }
        return Optional.empty();
    }

    /**
     * Adds a patient to the register, reusing an existing record when the phone
     * number is already known so one person cannot end up with two entries.
     *
     * @param name        the patient's name
     * @param phoneNumber the ten-digit phone number
     * @return the registered patient
     */
    public Patient registerPatient(String name, String phoneNumber) {
        Optional<Patient> existing = findPatientByPhone(phoneNumber);
        if (existing.isPresent()) {
            return existing.get();
        }
        Patient patient = new Patient(name, phoneNumber);
        patients.add(patient);
        return patient;
    }

    /**
     * Lists the slots on a given date that a professional still has free.
     *
     * @param professional the professional to check
     * @param date         the date to check
     * @return the free start times, in chronological order
     */
    public List<LocalTime> getFreeSlots(MedicalProfessional professional, LocalDate date) {
        List<LocalTime> free = new ArrayList<>(manager.getAvailableSlots());
        for (Appointment appointment : manager.findAppointmentsByProfessional(professional.getStaffId())) {
            if (appointment.getDate().equals(date)) {
                free.remove(appointment.getStartTime());
            }
        }
        return free;
    }

    /**
     * Counts how many bookings a professional holds on a date.
     *
     * @param professional the professional
     * @param date         the date
     * @return the number of bookings
     */
    public int countBookingsFor(MedicalProfessional professional, LocalDate date) {
        int count = 0;
        for (Appointment appointment : manager.findAppointmentsByProfessional(professional.getStaffId())) {
            if (appointment.getDate().equals(date)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts today's bookings per role, in the order the roles appear on the
     * roster, so the report reads like the clinic's own list.
     *
     * @param date the date to count
     * @return role title to booking count
     */
    public Map<String, Integer> countBookingsByRole(LocalDate date) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (MedicalProfessional professional : staff) {
            counts.putIfAbsent(professional.getRole(), 0);
        }
        for (Appointment appointment : manager.getAllAppointmentsSorted()) {
            if (appointment.getDate().equals(date)) {
                String role = appointment.getProfessional().getRole();
                counts.put(role, counts.getOrDefault(role, 0) + 1);
            }
        }
        return counts;
    }

    /**
     * Finds the time of day that currently carries the most bookings, which
     * tells the receptionist where the pressure is.
     *
     * @param date the date to inspect
     * @return the busiest start time and its booking count, or empty when the
     *         day has no bookings
     */
    public Optional<Map.Entry<LocalTime, Integer>> findBusiestSlot(LocalDate date) {
        Map<LocalTime, Integer> counts = new LinkedHashMap<>();
        for (Appointment appointment : manager.getAllAppointmentsSorted()) {
            if (appointment.getDate().equals(date)) {
                LocalTime slot = appointment.getStartTime();
                counts.put(slot, counts.getOrDefault(slot, 0) + 1);
            }
        }
        Map.Entry<LocalTime, Integer> busiest = null;
        for (Map.Entry<LocalTime, Integer> entry : counts.entrySet()) {
            if (busiest == null || entry.getValue() > busiest.getValue()) {
                busiest = entry;
            }
        }
        return Optional.ofNullable(busiest);
    }

    /**
     * Reports the share of the day's capacity that is already booked, expressed
     * as a percentage.
     *
     * @param date the date to measure
     * @return a value between 0 and 100
     */
    public double calculateUtilisation(LocalDate date) {
        int slotsPerProfessional = manager.getAvailableSlots().size();
        int capacity = slotsPerProfessional * staff.size();
        if (capacity == 0) {
            return 0;
        }
        int booked = 0;
        for (Appointment appointment : manager.getAllAppointmentsSorted()) {
            if (appointment.getDate().equals(date)) {
                booked++;
            }
        }
        return booked * 100.0 / capacity;
    }

    /**
     * Lists every professional who is on the roster for a given day, so the
     * console can warn when a booking is attempted on someone's day off.
     *
     * @param day the day to test
     * @return the professionals rostered that day
     */
    public List<MedicalProfessional> getRosteredStaff(DayOfWeek day) {
        List<MedicalProfessional> rostered = new ArrayList<>();
        for (MedicalProfessional professional : staff) {
            if (professional.worksOn(day)) {
                rostered.add(professional);
            }
        }
        return rostered;
    }

    /**
     * Returns an unmodifiable view of the roster, for callers that only need to
     * read it.
     *
     * @return the staff list, read-only
     */
    public List<MedicalProfessional> getStaffReadOnly() {
        return Collections.unmodifiableList(staff);
    }
}
