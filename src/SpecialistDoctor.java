import java.time.DayOfWeek;
import java.util.Set;

/**
 * A specialist doctor who sees patients referred by a GP, for example a
 * cardiologist or a dermatologist.
 *
 * <p>Adds specialty data and a fee-calculation behaviour that the base class
 * does not have.</p>
 *
 * @author Student
 * @version 1.0
 */
public class SpecialistDoctor extends MedicalProfessional {

    /** Clinical specialty, for example "Cardiology". */
    private final String specialty;

    /** Standard consultation fee in Australian dollars. */
    private final double standardFee;

    /**
     * Creates a specialist with an explicit working-day set.
     *
     * @param staffId    numeric-only identifier
     * @param name       full name
     * @param workingDays days the specialist works
     * @param specialty  clinical specialty
     * @param standardFee standard consultation fee, must not be negative
     */
    public SpecialistDoctor(String staffId, String name, Set<DayOfWeek> workingDays,
                            String specialty, double standardFee) {
        super(staffId, name, workingDays);
        if (specialty == null || specialty.isBlank()) {
            throw new IllegalArgumentException("Specialty must not be empty.");
        }
        if (standardFee < 0) {
            throw new IllegalArgumentException("Standard fee must not be negative.");
        }
        this.specialty = specialty.trim();
        this.standardFee = standardFee;
    }

    /**
     * Convenience constructor using the clinic's default Monday-to-Friday roster.
     *
     * @param staffId    numeric-only identifier
     * @param name       full name
     * @param specialty  clinical specialty
     * @param standardFee standard consultation fee
     */
    public SpecialistDoctor(String staffId, String name, String specialty, double standardFee) {
        this(staffId, name, defaultWorkingDays(), specialty, standardFee);
    }

    /**
     * Reads the clinical specialty.
     *
     * @return the specialty label
     */
    public String getSpecialty() {
        return specialty;
    }

    /**
     * Reads the standard consultation fee.
     *
     * @return the fee in Australian dollars
     */
    public double getStandardFee() {
        return standardFee;
    }

    /**
     * Specialist-specific behaviour: a review visit is charged at a reduced
     * rate because no new diagnostic work-up is required.
     *
     * @param followUpVisit {@code true} for a review of an existing patient
     * @return the fee payable for the visit
     */
    public double calculateFee(boolean followUpVisit) {
        return followUpVisit ? standardFee * 0.6 : standardFee;
    }

    /**
     * {@inheritDoc}
     *
     * @return "Specialist Doctor"
     */
    @Override
    public String getRole() {
        return "Specialist Doctor";
    }

    /**
     * {@inheritDoc}
     *
     * @return the specialty and standard fee
     */
    @Override
    public String getRoleDetails() {
        return String.format("Specialty: %s, Standard fee: $%.2f", specialty, standardFee);
    }

    /**
     * Appends specialist detail to the shared summary.
     *
     * @return the shared summary followed by the specialist fields
     */
    @Override
    public String toString() {
        return super.toString() + " | " + getRoleDetails();
    }
}
