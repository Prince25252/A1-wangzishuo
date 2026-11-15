import java.time.DayOfWeek;
import java.util.Set;

/**
 * A dietitian who provides nutrition assessment and dietary planning.
 *
 * <p>Adds the dietitian's clinical focus area and the length of a standard
 * consultation, because dietary reviews need longer slots than a GP visit.</p>
 *
 * @author Student
 * @version 1.0
 */
public class Dietitian extends MedicalProfessional {

    /** Clinical focus area, for example "Diabetes management". */
    private final String focusArea;

    /** Length of a standard dietitian consultation, in minutes. */
    private final int consultationMinutes;

    /**
     * Creates a dietitian with an explicit working-day set.
     *
     * @param staffId            numeric-only identifier
     * @param name               full name
     * @param workingDays        days the dietitian works
     * @param focusArea          clinical focus area
     * @param consultationMinutes consultation length in minutes, must be positive
     */
    public Dietitian(String staffId, String name, Set<DayOfWeek> workingDays,
                     String focusArea, int consultationMinutes) {
        super(staffId, name, workingDays);
        if (focusArea == null || focusArea.isBlank()) {
            throw new IllegalArgumentException("Focus area must not be empty.");
        }
        if (consultationMinutes <= 0) {
            throw new IllegalArgumentException("Consultation length must be positive.");
        }
        this.focusArea = focusArea.trim();
        this.consultationMinutes = consultationMinutes;
    }

    /**
     * Convenience constructor using the clinic's default Monday-to-Friday roster.
     *
     * @param staffId            numeric-only identifier
     * @param name               full name
     * @param focusArea          clinical focus area
     * @param consultationMinutes consultation length in minutes
     */
    public Dietitian(String staffId, String name, String focusArea, int consultationMinutes) {
        this(staffId, name, defaultWorkingDays(), focusArea, consultationMinutes);
    }

    /**
     * Reads the clinical focus area.
     *
     * @return the focus area label
     */
    public String getFocusArea() {
        return focusArea;
    }

    /**
     * Reads the standard consultation length.
     *
     * @return the length in minutes
     */
    public int getConsultationMinutes() {
        return consultationMinutes;
    }

    /**
     * Dietitian-specific behaviour: recommends how many follow-up reviews a
     * patient should be booked for, based on the focus area.
     *
     * @param initialAssessmentScore a simple 0-10 assessment score
     * @return the recommended number of follow-up reviews
     */
    public int recommendFollowUpReviews(int initialAssessmentScore) {
        if (initialAssessmentScore < 0 || initialAssessmentScore > 10) {
            throw new IllegalArgumentException("Assessment score must be between 0 and 10.");
        }
        if (initialAssessmentScore <= 3) {
            return 1;
        }
        if (initialAssessmentScore <= 7) {
            return 2;
        }
        return 4;
    }

    /**
     * {@inheritDoc}
     *
     * @return "Dietitian"
     */
    @Override
    public String getRole() {
        return "Dietitian";
    }

    /**
     * {@inheritDoc}
     *
     * @return the focus area and consultation length
     */
    @Override
    public String getRoleDetails() {
        return String.format("Focus area: %s, Consultation: %d minutes", focusArea, consultationMinutes);
    }

    /**
     * Appends dietitian detail to the shared summary.
     *
     * @return the shared summary followed by the dietitian fields
     */
    @Override
    public String toString() {
        return super.toString() + " | " + getRoleDetails();
    }
}
