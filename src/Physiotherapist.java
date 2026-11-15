import java.time.DayOfWeek;
import java.util.Set;

/**
 * A physiotherapist who treats movement and musculoskeletal problems.
 *
 * <p>Adds the treatment approach offered and a simple recovery estimate that
 * the clinic uses when advising patients on follow-up bookings.</p>
 *
 * @author Student
 * @version 1.0
 */
public class Physiotherapist extends MedicalProfessional {

    /** Treatment approach, for example "Sports rehabilitation". */
    private final String treatmentApproach;

    /** Number of treatment rooms the physiotherapist has access to. */
    private final int treatmentRooms;

    /**
     * Creates a physiotherapist with an explicit working-day set.
     *
     * @param staffId           numeric-only identifier
     * @param name              full name
     * @param workingDays       days the physiotherapist works
     * @param treatmentApproach treatment approach offered
     * @param treatmentRooms    number of treatment rooms, must be positive
     */
    public Physiotherapist(String staffId, String name, Set<DayOfWeek> workingDays,
                           String treatmentApproach, int treatmentRooms) {
        super(staffId, name, workingDays);
        if (treatmentApproach == null || treatmentApproach.isBlank()) {
            throw new IllegalArgumentException("Treatment approach must not be empty.");
        }
        if (treatmentRooms <= 0) {
            throw new IllegalArgumentException("Treatment rooms must be positive.");
        }
        this.treatmentApproach = treatmentApproach.trim();
        this.treatmentRooms = treatmentRooms;
    }

    /**
     * Convenience constructor using the clinic's default Monday-to-Friday roster.
     *
     * @param staffId           numeric-only identifier
     * @param name              full name
     * @param treatmentApproach treatment approach offered
     * @param treatmentRooms    number of treatment rooms
     */
    public Physiotherapist(String staffId, String name, String treatmentApproach, int treatmentRooms) {
        this(staffId, name, defaultWorkingDays(), treatmentApproach, treatmentRooms);
    }

    /**
     * Reads the treatment approach.
     *
     * @return the approach label
     */
    public String getTreatmentApproach() {
        return treatmentApproach;
    }

    /**
     * Reads the number of treatment rooms.
     *
     * @return the room count
     */
    public int getTreatmentRooms() {
        return treatmentRooms;
    }

    /**
     * Physiotherapist-specific behaviour: estimates how many sessions a
     * condition will need, which the clinic quotes to the patient.
     *
     * @param injurySeverity a simple 1-5 severity rating
     * @return the estimated number of sessions
     */
    public int estimateRecoverySessions(int injurySeverity) {
        if (injurySeverity < 1 || injurySeverity > 5) {
            throw new IllegalArgumentException("Injury severity must be between 1 and 5.");
        }
        return injurySeverity * 2;
    }

    /**
     * {@inheritDoc}
     *
     * @return "Physiotherapist"
     */
    @Override
    public String getRole() {
        return "Physiotherapist";
    }

    /**
     * {@inheritDoc}
     *
     * @return the treatment approach and room count
     */
    @Override
    public String getRoleDetails() {
        return String.format("Approach: %s, Treatment rooms: %d", treatmentApproach, treatmentRooms);
    }

    /**
     * Appends physiotherapy detail to the shared summary.
     *
     * @return the shared summary followed by the physiotherapy fields
     */
    @Override
    public String toString() {
        return super.toString() + " | " + getRoleDetails();
    }
}
