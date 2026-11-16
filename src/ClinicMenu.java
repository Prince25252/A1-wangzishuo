import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Interactive console front end for the clinic.
 *
 * <p>The assessment brief only requires the object model to be simulated from
 * {@code main}, so this console is an extension rather than a requirement. It
 * exists because the model is easier to demonstrate - and easier to mark -
 * when a marker can book, filter, cancel and inspect the day without editing
 * source code and recompiling.</p>
 *
 * <p>The class is presentation only: it validates and formats what the user
 * types, then delegates every business rule to {@link AppointmentManager} and
 * {@link Clinic}. No booking rule is re-implemented here.</p>
 *
 * @author Student
 * @version 1.1
 */
public class ClinicMenu {

    /** Width of the rules used to separate screens. */
    private static final int WIDTH = 74;

    /** Format used for times on screen. */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /** Clinic state shared by every screen. */
    private final Clinic clinic;

    /** Console reader that survives bad input. */
    private final InputReader input;

    /**
     * Creates the console over a prepared clinic.
     *
     * @param clinic the clinic whose state the screens will read and change
     */
    public ClinicMenu(Clinic clinic) {
        this.clinic = clinic;
        this.input = new InputReader();
    }

    /**
     * Runs the console until the user chooses to exit or input ends.
     */
    public void run() {
        seedSampleData();
        System.out.println();
        System.out.println("Greenfield Family Clinic - appointment console");
        System.out.println("Sample patients and bookings were loaded so every screen has content.");
        System.out.println("Appointments are same-day only; the clinic books today, "
                + LocalDate.now() + ".");

        while (true) {
            printMainMenu();
            OptionalInt choice = input.readIntInRange("Select an option: ", 0, 12);
            if (choice.isEmpty()) {
                System.out.println();
                System.out.println("Input closed. Goodbye.");
                return;
            }
            int selected = choice.getAsInt();
            if (selected == 0) {
                System.out.println();
                System.out.println("Goodbye.");
                return;
            }
            System.out.println();
            switch (selected) {
                case 1 -> showAllStaff();
                case 2 -> searchStaff();
                case 3 -> showFreeSlots();
                case 4 -> bookAppointment();
                case 5 -> showAllAppointments();
                case 6 -> filterByProfessional();
                case 7 -> filterByPatient();
                case 8 -> cancelByProfessionalAndTime();
                case 9 -> cancelByPatient();
                case 10 -> registerPatient();
                case 11 -> showStatistics();
                case 12 -> showTimetable();
                default -> System.out.println("Unknown option.");
            }
            if (!input.pause()) {
                System.out.println();
                System.out.println("Input closed. Goodbye.");
                return;
            }
        }
    }

    /**
     * Adds a few patients and bookings so the console demonstrates itself
     * immediately instead of starting from an empty day.
     */
    private void seedSampleData() {
        LocalDate today = LocalDate.now();
        AppointmentManager manager = clinic.getManager();

        Patient mei = clinic.registerPatient("Mei Lin", "0412345678");
        Patient jordan = clinic.registerPatient("Jordan Pike", "0423456789");
        Patient ana = clinic.registerPatient("Ana Duarte", "0434567890");

        clinic.registerPatient("Rob Ellery", "0445678901");

        clinic.findProfessionalById("1001").ifPresent(gp ->
                manager.addAppointment(mei, gp, today, LocalTime.of(9, 0)));
        clinic.findProfessionalById("1001").ifPresent(gp ->
                manager.addAppointment(jordan, gp, today, LocalTime.of(9, 30)));
        clinic.findProfessionalById("2001").ifPresent(specialist ->
                manager.addAppointment(ana, specialist, today, LocalTime.of(10, 0)));

        System.out.println("Loaded " + clinic.getPatients().size() + " patients and "
                + manager.getAppointmentCount() + " sample bookings.");
    }

    /**
     * Draws the main menu.
     */
    private void printMainMenu() {
        System.out.println();
        System.out.println(repeat('=', WIDTH));
        System.out.println(" MAIN MENU   " + LocalDate.now() + "  " + LocalDate.now().getDayOfWeek());
        System.out.println(repeat('=', WIDTH));
        System.out.println("  1) List all medical professionals");
        System.out.println("  2) Search professionals by name or role");
        System.out.println("  3) Show a professional's free slots today");
        System.out.println("  4) Book an appointment");
        System.out.println("  5) Show all appointments (sorted by time)");
        System.out.println("  6) Filter appointments by professional");
        System.out.println("  7) Filter appointments by patient phone");
        System.out.println("  8) Cancel an appointment (professional + time)");
        System.out.println("  9) Cancel all bookings for a patient");
        System.out.println(" 10) Register a new patient");
        System.out.println(" 11) Today's statistics");
        System.out.println(" 12) Show clinic timetable");
        System.out.println("  0) Exit");
    }

    /**
     * Screen 1: prints the whole roster, numbering each professional.
     */
    private void showAllStaff() {
        printHeading("STAFF ROSTER");
        List<MedicalProfessional> staff = clinic.getStaff();
        int index = 1;
        for (MedicalProfessional professional : staff) {
            System.out.printf("  %2d. %s%n", index++, professional);
        }
        System.out.println();
        System.out.printf("  %d professionals across %d roles.%n",
                staff.size(), countRoles(staff));
    }

    /**
     * Counts the distinct role titles on the roster.
     *
     * @param staff the roster
     * @return the number of distinct roles
     */
    private int countRoles(List<MedicalProfessional> staff) {
        java.util.Set<String> roles = new java.util.LinkedHashSet<>();
        for (MedicalProfessional professional : staff) {
            roles.add(professional.getRole());
        }
        return roles.size();
    }

    /**
     * Screen 2: searches the roster by a fragment of a name or role.
     */
    private void searchStaff() {
        printHeading("SEARCH STAFF");
        Optional<String> keyword = input.readLine("Name or role fragment (blank for all): ");
        if (keyword.isEmpty()) {
            return;
        }
        List<MedicalProfessional> matches = clinic.searchStaff(keyword.get());
        if (matches.isEmpty()) {
            System.out.println("  No professional matches \"" + keyword.get() + "\".");
            return;
        }
        int index = 1;
        for (MedicalProfessional professional : matches) {
            System.out.printf("  %2d. %s%n", index++, professional);
        }
    }

    /**
     * Screen 3: shows which slots a professional still has free today, and
     * which are already taken.
     */
    private void showFreeSlots() {
        printHeading("FREE SLOTS TODAY");
        Optional<MedicalProfessional> professional = promptForProfessional();
        if (professional.isEmpty()) {
            return;
        }
        MedicalProfessional selected = professional.get();
        LocalDate today = LocalDate.now();

        List<LocalTime> free = clinic.getFreeSlots(selected, today);
        int booked = clinic.countBookingsFor(selected, today);

        System.out.println();
        System.out.println("  " + selected.getRole() + " " + selected.getName()
                + " (ID " + selected.getStaffId() + ")");
        System.out.println("  Rostered today: "
                + (selected.worksOn(today.getDayOfWeek()) ? "yes" : "no"));
        System.out.println("  Booked slots: " + booked + ", free slots: " + free.size());
        System.out.println();

        StringBuilder freeLine = new StringBuilder("  Free: ");
        if (free.isEmpty()) {
            freeLine.append("(none left today)");
        } else {
            for (LocalTime slot : free) {
                freeLine.append(slot.format(TIME_FORMAT)).append("  ");
            }
        }
        System.out.println(freeLine.toString().trim());
    }

    /**
     * Screen 4: books an appointment through a guided series of prompts.
     */
    private void bookAppointment() {
        printHeading("BOOK AN APPOINTMENT");
        Optional<MedicalProfessional> professional = promptForProfessional();
        if (professional.isEmpty()) {
            return;
        }
        MedicalProfessional selected = professional.get();
        LocalDate today = LocalDate.now();

        if (!selected.worksOn(today.getDayOfWeek())) {
            System.out.println("  Warning: " + selected.getName() + " is not rostered today ("
                    + today.getDayOfWeek() + ").");
            Optional<Boolean> proceed = input.readYesNo("  Continue anyway? (y/n): ");
            if (proceed.isEmpty() || !proceed.get()) {
                System.out.println("  Booking abandoned.");
                return;
            }
        }

        List<LocalTime> free = clinic.getFreeSlots(selected, today);
        if (free.isEmpty()) {
            System.out.println("  There are no free slots left for " + selected.getName() + " today.");
            return;
        }

        System.out.println();
        System.out.println("  Available slots:");
        for (int i = 0; i < free.size(); i++) {
            System.out.printf("   %2d) %s%n", i + 1, free.get(i).format(TIME_FORMAT));
        }

        OptionalInt choice = input.readIntInRange("  Choose a slot number: ", 1, free.size());
        if (choice.isEmpty()) {
            return;
        }
        LocalTime slot = free.get(choice.getAsInt() - 1);

        Optional<String> phone = input.readPhoneNumber("  Patient phone number: ");
        if (phone.isEmpty()) {
            return;
        }
        Optional<Patient> existing = clinic.findPatientByPhone(phone.get());
        Patient patient;
        if (existing.isPresent()) {
            patient = existing.get();
            System.out.println("  Known patient: " + patient.getName());
        } else {
            Optional<String> name = input.readRequiredText("  New patient name: ");
            if (name.isEmpty()) {
                return;
            }
            patient = clinic.registerPatient(name.get(), phone.get());
            System.out.println("  Registered " + patient.getName() + ".");
        }

        clinic.getManager().addAppointment(patient, selected, today, slot);
    }

    /**
     * Screen 5: prints every appointment in time order.
     */
    private void showAllAppointments() {
        printHeading("ALL APPOINTMENTS");
        clinic.getManager().printAppointments("BOOKINGS FOR " + LocalDate.now(),
                clinic.getManager().getAllAppointmentsSorted());
    }

    /**
     * Screen 6: filters by professional identifier.
     */
    private void filterByProfessional() {
        printHeading("FILTER BY PROFESSIONAL");
        Optional<MedicalProfessional> professional = promptForProfessional();
        if (professional.isEmpty()) {
            return;
        }
        List<Appointment> results = clinic.getManager()
                .findAppointmentsByProfessional(professional.get().getStaffId());
        clinic.getManager().printAppointments(
                "STAFF ID " + professional.get().getStaffId() + " - " + professional.get().getName(),
                clinic.getManager().sortAppointments(results));
    }

    /**
     * Screen 7: filters by patient phone number.
     */
    private void filterByPatient() {
        printHeading("FILTER BY PATIENT");
        Optional<String> phone = input.readPhoneNumber("Phone number: ");
        if (phone.isEmpty()) {
            return;
        }
        List<Appointment> results = clinic.getManager().findAppointmentsByPatient(phone.get());
        Optional<Patient> patient = clinic.findPatientByPhone(phone.get());
        String heading = patient.map(value -> value.getName() + " (" + phone.get() + ")")
                .orElse("phone " + phone.get());
        clinic.getManager().printAppointments("PATIENT - " + heading,
                clinic.getManager().sortAppointments(results));
    }

    /**
     * Screen 8: cancels one booking by professional and start time.
     */
    private void cancelByProfessionalAndTime() {
        printHeading("CANCEL BY PROFESSIONAL AND TIME");
        Optional<MedicalProfessional> professional = promptForProfessional();
        if (professional.isEmpty()) {
            return;
        }
        Optional<LocalTime> time = input.readTime("Start time (HH:mm): ");
        if (time.isEmpty()) {
            return;
        }
        clinic.getManager().cancelAppointment(professional.get().getStaffId(),
                LocalDate.now(), time.get());
    }

    /**
     * Screen 9: cancels every booking belonging to one patient.
     */
    private void cancelByPatient() {
        printHeading("CANCEL PATIENT BOOKINGS");
        Optional<String> phone = input.readPhoneNumber("Phone number: ");
        if (phone.isEmpty()) {
            return;
        }
        int cancelled = clinic.getManager().cancelAppointmentsByPatient(phone.get());
        if (cancelled > 0) {
            System.out.println("  Patient record kept in the register; only bookings were removed.");
        }
    }

    /**
     * Screen 10: adds a patient to the register.
     */
    private void registerPatient() {
        printHeading("REGISTER A NEW PATIENT");
        Optional<String> name = input.readRequiredText("Patient name: ");
        if (name.isEmpty()) {
            return;
        }
        Optional<String> phone = input.readPhoneNumber("Phone number: ");
        if (phone.isEmpty()) {
            return;
        }
        Patient patient = clinic.registerPatient(name.get(), phone.get());
        System.out.println("  Registered: " + patient);
        System.out.println("  Register now holds " + clinic.getPatients().size() + " patient(s).");
    }

    /**
     * Screen 11: prints the day's operational summary.
     */
    private void showStatistics() {
        printHeading("TODAY'S STATISTICS");
        LocalDate today = LocalDate.now();
        AppointmentManager manager = clinic.getManager();

        System.out.println("  Date: " + today + " (" + today.getDayOfWeek() + ")");
        System.out.println("  Total bookings: " + manager.getAppointmentCount());
        System.out.printf("  Capacity utilisation: %.1f%%%n", clinic.calculateUtilisation(today));
        System.out.println("  Registered patients: " + clinic.getPatients().size());

        Optional<Map.Entry<LocalTime, Integer>> busiest = clinic.findBusiestSlot(today);
        if (busiest.isEmpty()) {
            System.out.println("  Busiest slot: none booked yet");
        } else {
            System.out.println("  Busiest slot: " + busiest.get().getKey().format(TIME_FORMAT)
                    + " with " + busiest.get().getValue() + " booking(s)");
        }

        System.out.println();
        System.out.println("  Bookings by role:");
        for (Map.Entry<String, Integer> entry : clinic.countBookingsByRole(today).entrySet()) {
            System.out.printf("    %-22s %d%n", entry.getKey(), entry.getValue());
        }

        System.out.println();
        System.out.println("  Load per professional:");
        for (MedicalProfessional professional : clinic.getStaff()) {
            int booked = clinic.countBookingsFor(professional, today);
            int free = clinic.getFreeSlots(professional, today).size();
            System.out.printf("    %-5s %-20s booked %2d, free %2d%n",
                    professional.getStaffId(), professional.getName(), booked, free);
        }
    }

    /**
     * Screen 12: prints the configured timetable and the roster for today.
     */
    private void showTimetable() {
        printHeading("CLINIC TIMETABLE");
        clinic.getManager().printTimetable();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        System.out.println();
        System.out.println("  Rostered today (" + today + "):");
        List<MedicalProfessional> rostered = clinic.getRosteredStaff(today);
        if (rostered.isEmpty()) {
            System.out.println("    nobody - the clinic is closed today");
        } else {
            for (MedicalProfessional professional : rostered) {
                System.out.println("    " + professional.getRole() + " - " + professional.getName());
            }
        }
    }

    /**
     * Prompts for a professional and resolves it, offering the roster when the
     * identifier is unknown.
     *
     * @return the chosen professional, or empty when the user backs out
     */
    private Optional<MedicalProfessional> promptForProfessional() {
        while (true) {
            Optional<String> id = input.readLine("Staff ID (blank to list staff): ");
            if (id.isEmpty()) {
                return Optional.empty();
            }
            if (id.get().isEmpty()) {
                int index = 1;
                for (MedicalProfessional professional : clinic.getStaff()) {
                    System.out.printf("   %2d) %-5s %-20s %s%n", index++,
                            professional.getStaffId(), professional.getName(), professional.getRole());
                }
                continue;
            }
            Optional<MedicalProfessional> match = clinic.findProfessionalById(id.get());
            if (match.isPresent()) {
                return match;
            }
            System.out.println("  No professional has ID " + id.get() + ". Try again or leave blank to list.");
        }
    }

    /**
     * Prints a ruled screen heading.
     *
     * @param title the heading text
     */
    private void printHeading(String title) {
        System.out.println(repeat('-', WIDTH));
        System.out.println(title);
        System.out.println(repeat('-', WIDTH));
    }

    /**
     * Builds a repeated character string without external libraries.
     *
     * @param character the character to repeat
     * @param count     how many times to repeat it
     * @return the repeated string
     */
    private static String repeat(char character, int count) {
        char[] symbols = new char[count];
        java.util.Arrays.fill(symbols, character);
        return new String(symbols);
    }
}
