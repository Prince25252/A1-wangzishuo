import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages every appointment held by the clinic.
 *
 * <p>The manager owns five responsibilities:</p>
 * <ol>
 *   <li>validating and storing new appointments;</li>
 *   <li>refusing a second booking for the same professional at the same time;</li>
 *   <li>filtering appointments by professional identifier or patient phone number;</li>
 *   <li>sorting appointments chronologically;</li>
 *   <li>cancelling appointments by professional-and-time or by patient phone number.</li>
 * </ol>
 *
 * <p>The clinic timetable is stored as configuration (opening time, closing
 * time, slot length) instead of being hard-coded, so the clinic can change its
 * hours without editing the booking logic. Slots are derived from that
 * configuration on demand.</p>
 *
 * <p>Appointments are held in an {@link ArrayList}. The collection is small
 * (one day of bookings for a single clinic), insertion order is not part of the
 * business rules, and sorting is done explicitly when needed, which makes a
 * list-based collection the natural choice taught in the unit.</p>
 *
 * @author Student
 * @version 1.0
 */
public class AppointmentManager {

    /** Time-of-day format used in messages, for example "08:00". */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /** All appointments currently held by the clinic. */
    private final List<Appointment> appointments;

    /** Time the clinic opens. */
    private LocalTime openingTime;

    /** Time the clinic closes; the last slot must finish by this time. */
    private LocalTime closingTime;

    /** Length of one consultation slot in minutes. */
    private int slotMinutes;

    /**
     * Creates a manager using the clinic's standard timetable of 08:00 to 16:00
     * in thirty-minute slots.
     */
    public AppointmentManager() {
        this(LocalTime.of(8, 0), LocalTime.of(16, 0), 30);
    }

    /**
     * Creates a manager with a custom timetable.
     *
     * @param openingTime time the clinic opens
     * @param closingTime time the clinic closes
     * @param slotMinutes length of one slot in minutes
     * @throws IllegalArgumentException if the timetable itself is not usable
     */
    public AppointmentManager(LocalTime openingTime, LocalTime closingTime, int slotMinutes) {
        this.appointments = new ArrayList<>();
        configureTimetable(openingTime, closingTime, slotMinutes);
    }

    /**
     * Reconfigures the clinic timetable. Existing bookings are left untouched,
     * because a clinic that shortens its day still has to honour appointments
     * already made; the new configuration governs future bookings only.
     *
     * @param openingTime time the clinic opens
     * @param closingTime time the clinic closes
     * @param slotMinutes length of one slot in minutes
     * @throws IllegalArgumentException if the timetable does not describe a
     *                                  usable sequence of slots
     */
    public void configureTimetable(LocalTime openingTime, LocalTime closingTime, int slotMinutes) {
        if (openingTime == null || closingTime == null) {
            throw new IllegalArgumentException("Opening and closing times are both required.");
        }
        if (slotMinutes <= 0) {
            throw new IllegalArgumentException("Slot length must be positive.");
        }
        if (!openingTime.isBefore(closingTime)) {
            throw new IllegalArgumentException("Opening time must be before closing time.");
        }
        if (openingTime.plusMinutes(slotMinutes).isAfter(closingTime)) {
            throw new IllegalArgumentException("Closing time must allow at least one full slot.");
        }
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.slotMinutes = slotMinutes;
    }

    /**
     * Reads the opening time.
     *
     * @return the opening time
     */
    public LocalTime getOpeningTime() {
        return openingTime;
    }

    /**
     * Reads the closing time.
     *
     * @return the closing time
     */
    public LocalTime getClosingTime() {
        return closingTime;
    }

    /**
     * Reads the slot length.
     *
     * @return the slot length in minutes
     */
    public int getSlotMinutes() {
        return slotMinutes;
    }

    /**
     * Builds the list of valid starting times for one day, derived from the
     * configured timetable. Because the slot list is generated rather than
     * written out by hand, changing the clinic hours automatically changes
     * which times are bookable.
     *
     * @return every valid slot start time, in order
     */
    public List<LocalTime> getAvailableSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime candidate = openingTime;
        while (!candidate.plusMinutes(slotMinutes).isAfter(closingTime)) {
            slots.add(candidate);
            candidate = candidate.plusMinutes(slotMinutes);
        }
        return slots;
    }

    /**
     * Books an appointment after checking that the request is complete, falls
     * inside a real slot, and does not clash with an existing booking for the
     * same professional.
     *
     * @param patient      the patient, must not be null
     * @param professional the professional, must not be null
     * @param date         the appointment date, must not be null
     * @param startTime    the requested start time, must not be null
     * @return {@code true} when the appointment was booked, {@code false} when it was rejected
     */
    public boolean addAppointment(Patient patient, MedicalProfessional professional,
                                  LocalDate date, LocalTime startTime) {
        if (patient == null || professional == null || date == null || startTime == null) {
            warn("Appointment rejected: patient, professional, date and time are all required.");
            return false;
        }

        // The clinic only supports same-day appointments, so any other date is refused.
        if (!date.equals(LocalDate.now())) {
            warn(String.format(
                    "Appointment rejected: the clinic books same-day appointments only. "
                            + "Requested %s, today is %s.",
                    date, LocalDate.now()));
            return false;
        }

        if (!getAvailableSlots().contains(startTime.withSecond(0).withNano(0))) {
            warn(String.format(
                    "Appointment rejected: %s is not a bookable slot. Slots run from %s to %s every %d minutes.",
                    startTime.format(TIME_FORMAT),
                    openingTime.format(TIME_FORMAT),
                    closingTime.format(TIME_FORMAT),
                    slotMinutes));
            return false;
        }

        Appointment candidate = new Appointment(patient, professional, date, startTime, slotMinutes);

        if (isProfessionalBooked(professional, date, candidate.getStartTime())) {
            warn(String.format(
                    "Appointment rejected: %s already has a booking at %s on %s. Double booking is not allowed.",
                    professional.getName(),
                    candidate.getStartTime().format(TIME_FORMAT),
                    date));
            return false;
        }

        appointments.add(candidate);
        System.out.printf("Booked: %s with %s at %s on %s.%n",
                patient.getName(), professional.getName(),
                candidate.getStartTime().format(TIME_FORMAT), date);
        return true;
    }

    /**
     * Checks whether a professional already holds a booking at a given moment.
     * The comparison relies on the professional's {@code equals} rule and on
     * {@link Appointment#equals(Object)}.
     *
     * @param professional the professional to check
     * @param date         the date to check
     * @param startTime    the start time to check
     * @return {@code true} when a booking already exists for that professional at that time
     */
    public boolean isProfessionalBooked(MedicalProfessional professional, LocalDate date,
                                        LocalTime startTime) {
        if (professional == null || date == null || startTime == null) {
            return false;
        }
        Appointment probe = new Appointment(new Patient("Probe Patient", "0400000000"),
                professional, date, startTime);
        for (Appointment existing : appointments) {
            if (existing.equals(probe)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns every appointment for a given professional.
     *
     * @param staffId the professional's numeric identifier
     * @return a new list holding the matching appointments, possibly empty
     */
    public List<Appointment> findAppointmentsByProfessional(String staffId) {
        List<Appointment> result = new ArrayList<>();
        if (staffId == null || staffId.isBlank()) {
            warn("Search by professional skipped: no staff ID supplied.");
            return result;
        }
        String target = staffId.trim();
        for (Appointment appointment : appointments) {
            if (appointment.getProfessional().getStaffId().equals(target)) {
                result.add(appointment);
            }
        }
        return result;
    }

    /**
     * Returns every appointment for a given patient.
     *
     * @param phoneNumber the patient's ten-digit phone number
     * @return a new list holding the matching appointments, possibly empty
     */
    public List<Appointment> findAppointmentsByPatient(String phoneNumber) {
        List<Appointment> result = new ArrayList<>();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            warn("Search by patient skipped: no phone number supplied.");
            return result;
        }
        String target = phoneNumber.replace(" ", "").trim();
        for (Appointment appointment : appointments) {
            if (appointment.getPatient().getPhoneNumber().equals(target)) {
                result.add(appointment);
            }
        }
        return result;
    }

    /**
     * Returns a chronologically sorted copy of every appointment, leaving the
     * internal order untouched.
     *
     * @return a new sorted list
     */
    public List<Appointment> getAllAppointmentsSorted() {
        return sortAppointments(appointments);
    }

    /**
     * Returns a chronologically sorted copy of any appointment list, so the
     * same sorting rule can be applied to filtered results.
     *
     * @param source the appointments to sort, must not be null
     * @return a new sorted list; the source list is not modified
     */
    public List<Appointment> sortAppointments(List<Appointment> source) {
        List<Appointment> copy = new ArrayList<>(source);
        Collections.sort(copy);
        return copy;
    }

    /**
     * Cancels the booking held by a specific professional at a specific time.
     *
     * @param staffId   the professional's numeric identifier
     * @param date      the appointment date
     * @param startTime the appointment start time
     * @return {@code true} when a booking was found and removed
     */
    public boolean cancelAppointment(String staffId, LocalDate date, LocalTime startTime) {
        if (staffId == null || date == null || startTime == null) {
            warn("Cancellation failed: staff ID, date and time are all required.");
            return false;
        }
        String target = staffId.trim();
        LocalTime normalised = startTime.withSecond(0).withNano(0);
        for (int index = 0; index < appointments.size(); index++) {
            Appointment appointment = appointments.get(index);
            if (appointment.getProfessional().getStaffId().equals(target)
                    && appointment.getDate().equals(date)
                    && appointment.getStartTime().equals(normalised)) {
                appointments.remove(index);
                System.out.printf("Cancelled: booking with staff ID %s at %s on %s.%n",
                        target, normalised.format(TIME_FORMAT), date);
                return true;
            }
        }
        warn(String.format("Cancellation failed: no booking for staff ID %s at %s on %s.",
                target, normalised.format(TIME_FORMAT), date));
        return false;
    }

    /**
     * Cancels every booking belonging to one patient, identified by phone
     * number.
     *
     * @param phoneNumber the patient's ten-digit phone number
     * @return the number of bookings cancelled, zero when none existed
     */
    public int cancelAppointmentsByPatient(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            warn("Cancellation failed: a phone number is required.");
            return 0;
        }
        String target = phoneNumber.replace(" ", "").trim();
        int cancelled = 0;
        for (int index = appointments.size() - 1; index >= 0; index--) {
            if (appointments.get(index).getPatient().getPhoneNumber().equals(target)) {
                appointments.remove(index);
                cancelled++;
            }
        }
        if (cancelled == 0) {
            warn("Cancellation failed: no bookings found for phone number " + target + ".");
        } else {
            System.out.printf("Cancelled %d booking(s) for phone number %s.%n", cancelled, target);
        }
        return cancelled;
    }

    /**
     * Reports how many appointments are currently held.
     *
     * @return the number of bookings
     */
    public int getAppointmentCount() {
        return appointments.size();
    }

    /**
     * Prints a titled list of appointments in a readable block, numbering each
     * entry so a booking can be referred to by line.
     *
     * @param heading the block heading, for example "ALL APPOINTMENTS (sorted)"
     * @param list    the appointments to print, already in the order to show
     */
    public void printAppointments(String heading, List<Appointment> list) {
        System.out.println();
        System.out.println("--- " + heading + " (" + list.size() + ") ---");
        if (list.isEmpty()) {
            System.out.println("    (none)");
            return;
        }
        int line = 1;
        for (Appointment appointment : list) {
            System.out.printf("    %2d. %s%n", line++, appointment);
        }
    }

    /**
     * Prints the configured timetable so the demonstration can show that the
     * clinic hours are configuration rather than hard-coded behaviour.
     */
    public void printTimetable() {
        System.out.printf("Timetable: %s-%s, %d-minute slots, %d bookable slot(s):%n",
                openingTime.format(TIME_FORMAT), closingTime.format(TIME_FORMAT),
                slotMinutes, getAvailableSlots().size());
        StringBuilder builder = new StringBuilder("    ");
        for (LocalTime slot : getAvailableSlots()) {
            builder.append(slot.format(TIME_FORMAT)).append("  ");
        }
        System.out.println(builder.toString().trim());
    }

    /**
     * Emits a single, consistently formatted warning. All rejection paths go
     * through this method so the console output stays tidy.
     *
     * @param message the warning text
     */
    private void warn(String message) {
        System.out.println("WARNING: " + message);
    }
}
