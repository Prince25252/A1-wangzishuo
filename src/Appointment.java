import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A single booking in the clinic: one patient, one medical professional,
 * one date and one start time.
 *
 * <p>The class implements {@link Comparable} so that a collection of
 * appointments can be sorted by time without the manager needing its own
 * comparator logic. All fields are final and validated in the constructor,
 * so an appointment can never exist in an incomplete or half-built state.</p>
 *
 * @author Student
 * @version 1.0
 */
public class Appointment implements Comparable<Appointment> {

    /** Time-of-day format used in printed output, for example "09:30". */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /** Format used when printing the date, for example "2026-11-16". */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** The patient attending the appointment. */
    private final Patient patient;

    /** The professional the patient is booked with. */
    private final MedicalProfessional professional;

    /** Date of the appointment; the clinic currently books same-day only. */
    private final LocalDate date;

    /** Start time of the appointment. */
    private final LocalTime startTime;

    /** Length of the appointment in minutes. */
    private final int durationMinutes;

    /**
     * Creates an appointment.
     *
     * @param patient         the patient, must not be null
     * @param professional    the professional, must not be null
     * @param date            the appointment date, must not be null
     * @param startTime       the start time, must not be null
     * @param durationMinutes the duration, must be positive
     * @throws IllegalArgumentException if any required value is missing or invalid
     */
    public Appointment(Patient patient, MedicalProfessional professional, LocalDate date,
                       LocalTime startTime, int durationMinutes) {
        if (patient == null) {
            throw new IllegalArgumentException("Appointment requires a patient.");
        }
        if (professional == null) {
            throw new IllegalArgumentException("Appointment requires a medical professional.");
        }
        if (date == null || startTime == null) {
            throw new IllegalArgumentException("Appointment requires both a date and a start time.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Appointment duration must be positive.");
        }
        this.patient = patient;
        this.professional = professional;
        this.date = date;
        this.startTime = startTime.withSecond(0).withNano(0);
        this.durationMinutes = durationMinutes;
    }

    /**
     * Creates a standard thirty-minute appointment, which is the clinic's default
     * slot length.
     *
     * @param patient      the patient
     * @param professional the professional
     * @param date         the appointment date
     * @param startTime    the start time
     */
    public Appointment(Patient patient, MedicalProfessional professional,
                       LocalDate date, LocalTime startTime) {
        this(patient, professional, date, startTime, 30);
    }

    /**
     * Reads the patient.
     *
     * @return the patient
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * Reads the professional.
     *
     * @return the professional
     */
    public MedicalProfessional getProfessional() {
        return professional;
    }

    /**
     * Reads the appointment date.
     *
     * @return the date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Reads the start time.
     *
     * @return the start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Reads the appointment length.
     *
     * @return the duration in minutes
     */
    public int getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Reads the end time, derived from the start time plus the duration.
     *
     * @return the finish time
     */
    public LocalTime getEndTime() {
        return startTime.plusMinutes(durationMinutes);
    }

    /**
     * Orders appointments chronologically: first by date, then by start time.
     * Appointments at the same moment for different professionals are then
     * ordered by staff identifier so the output remains stable between runs.
     *
     * @param other the appointment to compare with
     * @return a negative, zero, or positive value when this appointment is
     *         earlier, equal, or later than the other
     */
    @Override
    public int compareTo(Appointment other) {
        int byDate = date.compareTo(other.date);
        if (byDate != 0) {
            return byDate;
        }
        int byTime = startTime.compareTo(other.startTime);
        if (byTime != 0) {
            return byTime;
        }
        return professional.getStaffId().compareTo(other.professional.getStaffId());
    }

    /**
     * An appointment is defined by who is booked with whom and when. This rule
     * is what the manager uses to detect double bookings.
     *
     * @param other the object to compare with
     * @return {@code true} when professional, date and start time all match
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Appointment)) {
            return false;
        }
        Appointment that = (Appointment) other;
        return professional.equals(that.professional)
                && date.equals(that.date)
                && startTime.equals(that.startTime);
    }

    /**
     * Keeps hashing consistent with {@link #equals(Object)}.
     *
     * @return a hash value derived from professional, date and start time
     */
    @Override
    public int hashCode() {
        return Objects.hash(professional, date, startTime);
    }

    /**
     * Produces a readable summary of the appointment, including the full detail
     * of the professional involved.
     *
     * @return a formatted multi-field summary
     */
    @Override
    public String toString() {
        return String.format("%s %s-%s | Patient: %-24s | %s | %s",
                date.format(DATE_FORMAT),
                startTime.format(TIME_FORMAT),
                getEndTime().format(TIME_FORMAT),
                patient.toString(),
                professional.getRole(),
                professional.getName() + " (ID " + professional.getStaffId() + ")");
    }
}
