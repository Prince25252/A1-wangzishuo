import java.util.Objects;

/**
 * A patient who books appointments with the clinic.
 *
 * <p>Patients are modelled as their own class rather than being flattened into
 * the {@link Appointment} as two loose strings. The appointment manager needs
 * to search by phone number, and keeping the phone number inside a validated,
 * immutable value object guarantees that every appointment always carries a
 * usable contact number.</p>
 *
 * @author Student
 * @version 1.0
 */
public class Patient {

    /** Full name of the patient. */
    private final String name;

    /** Ten-digit Australian mobile number, for example "0412345678". */
    private final String phoneNumber;

    /**
     * Creates a patient.
     *
     * @param name        full name, must not be null or blank
     * @param phoneNumber ten-digit mobile number, digits only
     * @throws IllegalArgumentException if the name is missing or the phone
     *                                  number is not exactly ten digits
     */
    public Patient(String name, String phoneNumber) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Patient name must not be empty.");
        }
        this.name = name.trim();
        this.phoneNumber = requirePhoneNumber(phoneNumber);
    }

    /**
     * Validates the phone number so that no appointment can ever hold an
     * unusable contact number.
     *
     * @param phoneNumber number supplied by the caller
     * @return the normalised ten-digit number
     * @throws IllegalArgumentException if the number is not ten digits
     */
    private static String requirePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number must not be empty.");
        }
        String normalised = phoneNumber.replace(" ", "").trim();
        if (!normalised.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Phone number must be exactly 10 digits, but was: " + phoneNumber);
        }
        return normalised;
    }

    /**
     * Reads the patient's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Reads the patient's phone number.
     *
     * @return the ten-digit number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Two patients are the same patient when their phone numbers match, which
     * is what makes phone-based searching in the manager reliable.
     *
     * @param other the object to compare with
     * @return {@code true} when both objects are patients with the same number
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Patient)) {
            return false;
        }
        Patient that = (Patient) other;
        return phoneNumber.equals(that.phoneNumber);
    }

    /**
     * Keeps hashing consistent with {@link #equals(Object)}.
     *
     * @return a hash value derived from the phone number
     */
    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }

    /**
     * Produces a readable summary of the patient.
     *
     * @return name and phone number
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", name, phoneNumber);
    }
}
