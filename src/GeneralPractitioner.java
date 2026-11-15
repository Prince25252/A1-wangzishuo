import java.time.DayOfWeek;
import java.util.Set;

/**
 * A general practitioner (GP): the clinic's first point of contact for
 * patients who do not yet know which service they need.
 *
 * <p>Extends {@link MedicalProfessional} and adds GP-specific data
 * (consultation room and daily patient cap) plus GP-specific behaviour
 * (whether further bookings can still be taken today).</p>
 *
 * @author Student
 * @version 1.0
 */
public class GeneralPractitioner extends MedicalProfessional {

    /** Room the GP consults from, for example "Room 3". */
    private final String consultationRoom;

    /** Maximum number of patients the GP can see in one day. */
    private final int dailyPatientLimit;

    /**
     * Creates a GP with an explicit working-day set.
     *
     * @param staffId          numeric-only identifier
     * @param name             full name
     * @param workingDays      days the GP works
     * @param consultationRoom room the GP consults from
     * @param dailyPatientLimit maximum patients per day, must be positive
     */
    public GeneralPractitioner(String staffId, String name, Set<DayOfWeek> workingDays,
                               String consultationRoom, int dailyPatientLimit) {
        super(staffId, name, workingDays);
        if (consultationRoom == null || consultationRoom.isBlank()) {
            throw new IllegalArgumentException("Consultation room must not be empty.");
        }
        if (dailyPatientLimit <= 0) {
            throw new IllegalArgumentException("Daily patient limit must be positive.");
        }
        this.consultationRoom = consultationRoom.trim();
        this.dailyPatientLimit = dailyPatientLimit;
    }

    /**
     * Convenience constructor using the clinic's default Monday-to-Friday roster.
     *
     * @param staffId          numeric-only identifier
     * @param name             full name
     * @param consultationRoom room the GP consults from
     * @param dailyPatientLimit maximum patients per day
     */
    public GeneralPractitioner(String staffId, String name, String consultationRoom,
                               int dailyPatientLimit) {
        this(staffId, name, defaultWorkingDays(), consultationRoom, dailyPatientLimit);
    }

    /**
     * Reads the consultation room.
     *
     * @return the room label
     */
    public String getConsultationRoom() {
        return consultationRoom;
    }

    /**
     * Reads the maximum number of patients this GP sees per day.
     *
     * @return the daily cap
     */
    public int getDailyPatientLimit() {
        return dailyPatientLimit;
    }

    /**
     * GP-specific behaviour: decides whether another booking can still be
     * accepted today, given how many patients have already been booked.
     *
     * @param bookedToday number of appointments already taken for this GP today
     * @return {@code true} while the daily cap has not been reached
     */
    public boolean canAcceptMorePatients(int bookedToday) {
        return bookedToday < dailyPatientLimit;
    }

    /**
     * {@inheritDoc}
     *
     * @return "General Practitioner"
     */
    @Override
    public String getRole() {
        return "General Practitioner";
    }

    /**
     * {@inheritDoc}
     *
     * @return the GP's room and daily cap
     */
    @Override
    public String getRoleDetails() {
        return String.format("Room: %s, Daily patient limit: %d", consultationRoom, dailyPatientLimit);
    }

    /**
     * Appends GP-specific detail to the shared summary.
     *
     * @return the shared summary followed by the GP-specific fields
     */
    @Override
    public String toString() {
        return super.toString() + " | " + getRoleDetails();
    }
}
