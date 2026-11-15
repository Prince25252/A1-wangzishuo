import java.time.DayOfWeek;
import java.util.Set;

/**
 * A nurse working on a specific ward, who can also be booked for wound care,
 * immunisations and health checks.
 *
 * <p>Adds ward data and the ability to declare whether a ward is covered
 * during a shift.</p>
 *
 * @author Student
 * @version 1.0
 */
public class Nurse extends MedicalProfessional {

    /** Ward the nurse is rostered to, for example "Ward B". */
    private final String ward;

    /** Years of clinical experience, used to determine the duty level. */
    private final int yearsExperience;

    /**
     * Creates a nurse with an explicit working-day set.
     *
     * @param staffId        numeric-only identifier
     * @param name           full name
     * @param workingDays    days the nurse works
     * @param ward           ward assignment
     * @param yearsExperience years of experience, must not be negative
     */
    public Nurse(String staffId, String name, Set<DayOfWeek> workingDays,
                 String ward, int yearsExperience) {
        super(staffId, name, workingDays);
        if (ward == null || ward.isBlank()) {
            throw new IllegalArgumentException("Ward must not be empty.");
        }
        if (yearsExperience < 0) {
            throw new IllegalArgumentException("Years of experience must not be negative.");
        }
        this.ward = ward.trim();
        this.yearsExperience = yearsExperience;
    }

    /**
     * Convenience constructor using the clinic's default Monday-to-Friday roster.
     *
     * @param staffId        numeric-only identifier
     * @param name           full name
     * @param ward           ward assignment
     * @param yearsExperience years of experience
     */
    public Nurse(String staffId, String name, String ward, int yearsExperience) {
        this(staffId, name, defaultWorkingDays(), ward, yearsExperience);
    }

    /**
     * Reads the ward assignment.
     *
     * @return the ward label
     */
    public String getWard() {
        return ward;
    }

    /**
     * Reads the years of clinical experience.
     *
     * @return the experience in years
     */
    public int getYearsExperience() {
        return yearsExperience;
    }

    /**
     * Nurse-specific behaviour: derives a duty level from experience, which
     * decides whether the nurse may run a clinic without supervision.
     *
     * @return a duty level label
     */
    public String getDutyLevel() {
        if (yearsExperience >= 8) {
            return "Senior (unsupervised)";
        }
        if (yearsExperience >= 3) {
            return "Registered (supervised for complex care)";
        }
        return "Graduate (supervised)";
    }

    /**
     * Nurse-specific behaviour: reports whether this nurse can cover a requested
     * ward, which the clinic checks when a booking is made against a ward.
     *
     * @param requestedWard the ward a booking refers to
     * @return {@code true} when the ward matches and the nurse is not a graduate
     */
    public boolean canCoverWard(String requestedWard) {
        if (requestedWard == null) {
            return false;
        }
        return ward.equalsIgnoreCase(requestedWard.trim())
                && !getDutyLevel().startsWith("Graduate");
    }

    /**
     * {@inheritDoc}
     *
     * @return "Nurse"
     */
    @Override
    public String getRole() {
        return "Nurse";
    }

    /**
     * {@inheritDoc}
     *
     * @return the ward, experience and duty level
     */
    @Override
    public String getRoleDetails() {
        return String.format("Ward: %s, Experience: %d year(s), Duty level: %s",
                ward, yearsExperience, getDutyLevel());
    }

    /**
     * Appends nursing detail to the shared summary.
     *
     * @return the shared summary followed by the nursing fields
     */
    @Override
    public String toString() {
        return super.toString() + " | " + getRoleDetails();
    }
}
