import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * Reads console input defensively so a mistyped value never terminates the
 * program.
 *
 * <p>Every read method re-prompts until it gets acceptable input, and every
 * method reports end-of-input by returning an empty {@link Optional} instead of
 * throwing, which lets the console shut down cleanly when input is piped in or
 * the user closes the stream.</p>
 *
 * @author Student
 * @version 1.1
 */
public final class InputReader {

    /** Format expected for times typed by the user. */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /** Underlying console scanner. */
    private final Scanner scanner;

    /** Set once the input stream is exhausted or closed. */
    private boolean closed;

    /**
     * Creates a reader over standard input.
     */
    public InputReader() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Reports whether the input stream can still supply data.
     *
     * @return {@code true} when no further input is available
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Reads one trimmed line.
     *
     * @param prompt the text shown before reading
     * @return the trimmed line, or empty when input has ended
     */
    public Optional<String> readLine(String prompt) {
        if (closed) {
            return Optional.empty();
        }
        System.out.print(prompt);
        System.out.flush();
        if (!scanner.hasNextLine()) {
            closed = true;
            System.out.println();
            return Optional.empty();
        }
        return Optional.of(scanner.nextLine().trim());
    }

    /**
     * Reads a non-empty line.
     *
     * @param prompt the text shown before reading
     * @return the trimmed line, or empty when input has ended
     */
    public Optional<String> readRequiredText(String prompt) {
        while (true) {
            Optional<String> line = readLine(prompt);
            if (line.isEmpty()) {
                return Optional.empty();
            }
            if (!line.get().isEmpty()) {
                return line;
            }
            System.out.println("  That field cannot be blank. Please try again.");
        }
    }

    /**
     * Reads an integer inside an inclusive range.
     *
     * @param prompt the text shown before reading
     * @param min    smallest acceptable value
     * @param max    largest acceptable value
     * @return the number, or empty when input has ended
     */
    public OptionalInt readIntInRange(String prompt, int min, int max) {
        while (true) {
            Optional<String> line = readLine(prompt);
            if (line.isEmpty()) {
                return OptionalInt.empty();
            }
            String text = line.get();
            if (text.isEmpty()) {
                System.out.println("  Please type a number between " + min + " and " + max + ".");
                continue;
            }
            try {
                int value = Integer.parseInt(text);
                if (value < min || value > max) {
                    System.out.println("  " + value + " is out of range. Expected " + min + " to " + max + ".");
                    continue;
                }
                return OptionalInt.of(value);
            } catch (NumberFormatException exception) {
                System.out.println("  \"" + text + "\" is not a number. Please try again.");
            }
        }
    }

    /**
     * Reads a ten-digit phone number.
     *
     * @param prompt the text shown before reading
     * @return the normalised number, or empty when input has ended
     */
    public Optional<String> readPhoneNumber(String prompt) {
        while (true) {
            Optional<String> line = readLine(prompt);
            if (line.isEmpty()) {
                return Optional.empty();
            }
            String normalised = line.get().replace(" ", "");
            if (normalised.matches("\\d{10}")) {
                return Optional.of(normalised);
            }
            System.out.println("  A phone number must be exactly 10 digits, for example 0412345678.");
        }
    }

    /**
     * Reads a time of day in the form HH:mm.
     *
     * @param prompt the text shown before reading
     * @return the parsed time, or empty when input has ended
     */
    public Optional<LocalTime> readTime(String prompt) {
        while (true) {
            Optional<String> line = readLine(prompt);
            if (line.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of(LocalTime.parse(line.get(), TIME_FORMAT));
            } catch (DateTimeParseException exception) {
                System.out.println("  Use the 24-hour form HH:mm, for example 09:30.");
            }
        }
    }

    /**
     * Asks a yes/no question.
     *
     * @param prompt the text shown before reading
     * @return {@code true} for yes, {@code false} for no, empty when input has ended
     */
    public Optional<Boolean> readYesNo(String prompt) {
        while (true) {
            Optional<String> line = readLine(prompt);
            if (line.isEmpty()) {
                return Optional.empty();
            }
            String answer = line.get().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return Optional.of(Boolean.TRUE);
            }
            if (answer.equals("n") || answer.equals("no")) {
                return Optional.of(Boolean.FALSE);
            }
            System.out.println("  Please answer y or n.");
        }
    }

    /**
     * Waits for the user to press Enter before the next screen is drawn.
     *
     * @return {@code false} when input has ended
     */
    public boolean pause() {
        return readLine("Press Enter to continue...").isPresent();
    }

    /**
     * Releases the underlying scanner.
     */
    public void close() {
        closed = true;
        scanner.close();
    }
}
