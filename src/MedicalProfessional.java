import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract base class representing any medical professional who can be
 * booked through the clinic's appointment system.
 *
 * <p>This class is deliberately abstract because a "generic medical
 * professional" does not correspond to a real, bookable role in the clinic.
 * Only concrete subclasses (General Practitioner, Specialist Doctor, Nurse,
 * Dietitian, Physiotherapist) can be instantiated.</p>
 *
 * <p>Encapsulation is enforced in three ways:</p>
 * <ul>
 *   <li>all fields are private;</li>
 *   <li>the staff identifier is final and validated at construction, so it can
 *       never be changed nor replaced by a non-numeric value;</li>
 *   <li>the working-day set is copied on the way in and exposed as an
 *       unmodifiable view on the way out, so no external code can mutate the
 *       internal state of a professional.</li>
 * </ul>
 *
 * @author Student
 * @version 1.0
 */
public abstract class MedicalProfessional {

    /** Identifier made of digits only, for example "1024". */
    private final String staffId;

    /** Full name of the professional, for example "Dr Alice Chan". */
    private String name;

    /** Days of the week this professional normally works in the clinic. */
    private final Set<DayOfWeek> workingDays;

    /**
     * Creates a professional with an explicit set of working days.
     *
     * @param staffId numeric-only identifier, must not be null or blank
     * @param name    full name of the professional, must not be null or blank
     * @param workingDays the days this professional works; a defensive copy is
     *                    stored, so the caller keeps ownership of its own set
     * @throws IllegalArgumentException if the identifier is not numeric, or if
     *                                  the name or working-day set is missing
     */
    protected MedicalProfessional(String staffId, String name, Set<DayOfWeek> workingDays) {
        this.staffId = requireNumericId(staffId);
        setName(name);
        this.workingDays = copyWorkingDays(workingDays);
    }

    /**
     * Overloaded constructor that applies the clinic's default working pattern
     * (Monday to Friday) so callers do not have to repeat it every time.
     *
     * @param staffId numeric-only identifier
     * @param name    full name of the professional
     */
    protected MedicalProfessional(String staffId, String name) {
        this(staffId, name, defaultWorkingDays());
    }

    /**
     * Supplies the clinic's default working pattern.
     *
     * @return a mutable set containing Monday to Friday, ready to be copied
     */
    protected static Set<DayOfWeek> defaultWorkingDays() {
        return EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
    }

    /**
     * Validates that the supplied identifier contains digits only.
     *
     * @param staffId identifier supplied by the caller
     * @return the same identifier, trimmed
     * @throws IllegalArgumentException if the identifier is missing or not numeric
     */
    private static String requireNumericId(String staffId) {
        if (staffId == null || staffId.isBlank()) {
            throw new IllegalArgumentException("Staff ID must not be empty.");
        }
        String trimmed = staffId.trim();
        if (!trimmed.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Staff ID must contain digits only, but was: " + trimmed);
        }
        return trimmed;
    }

    /**
     * Protects the internal working-day set from outside modification.
     *
     * @param days days supplied by the caller
     * @return an immutable copy, never null
     */
    private static Set<DayOfWeek> copyWorkingDays(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("At least one working day is required.");
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(days));
    }

    /**
     * Reads the staff identifier.
     *
     * @return the numeric-only identifier
     */
    public final String getStaffId() {
        return staffId;
    }

    /**
     * Reads the professional's name.
     *
     * @return the current name
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the name while keeping the object in a valid state.
     *
     * @param name new name, must not be null, blank, or longer than 60 characters
     * @throws IllegalArgumentException if the name is not acceptable
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 60) {
            throw new IllegalArgumentException("Name must be 60 characters or fewer.");
        }
        this.name = trimmed;
    }

    /**
     * Reads the working days as an unmodifiable view.
     *
     * @return the working days; attempts to modify it raise
     *         {@link UnsupportedOperationException}
     */
    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    /**
     * Tells whether this professional is rostered on a given day.
     *
     * @param day the day to test
     * @return {@code true} when the professional works that day
     */
    public boolean worksOn(DayOfWeek day) {
        return workingDays.contains(day);
    }

    /**
     * Each subclass must state its own role so that the generic printing code
     * can label every professional correctly.
     *
     * @return the role title, for example "General Practitioner"
     */
    public abstract String getRole();

    /**
     * Each subclass must describe the data and behaviour unique to it, so the
     * shared {@link #toString()} can print role-specific detail.
     *
     * @return a short description of the subclass-specific attributes
     */
    public abstract String getRoleDetails();

    /**
     * Produces a readable summary of the professional's shared attributes.
     * Subclasses extend this by appending their own detail.
     *
     * @return a formatted one-line summary
     */
    @Override
    public String toString() {
        return String.format("%-22s | ID: %-5s | Name: %-20s | Works: %s",
                getRole(), staffId, name, formatWorkingDays());
    }

    /**
     * Renders the working-day set as a compact abbreviation list.
     *
     * @return abbreviated days such as "MON, TUE, WED, THU, FRI"
     */
    private String formatWorkingDays() {
        StringBuilder builder = new StringBuilder();
        for (DayOfWeek day : workingDays) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(day.name(), 0, 3);
        }
        return builder.toString();
    }

    /**
     * Two professionals are the same professional when their staff identifiers
     * match. This equality rule is what the appointment manager uses to detect
     * double bookings.
     *
     * @param other the object to compare with
     * @return {@code true} when both objects are professionals with the same ID
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MedicalProfessional)) {
            return false;
        }
        MedicalProfessional that = (MedicalProfessional) other;
        return staffId.equals(that.staffId);
    }

    /**
     * Keeps hashing consistent with {@link #equals(Object)}.
     *
     * @return a hash value derived from the staff identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(staffId);
    }
}
