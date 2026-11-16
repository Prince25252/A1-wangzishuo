import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point of Assessment One.
 *
 * <p>With no arguments the class exercises the inheritance and polymorphism
 * design from Parts 1 to 3 and then runs an end-to-end demonstration of the
 * appointment system from Parts 4 to 6, including both successful operations
 * and rejected ones, so every validation rule and warning path can be observed
 * in the console output.</p>
 *
 * <p>With the single argument {@code menu} it starts the interactive console
 * instead. That console is an extension beyond the brief: the brief asks only
 * for the object model to be simulated from {@code main}.</p>
 *
 * <p>No graphical interface, login, or file storage is used.</p>
 *
 * @author Student
 * @version 1.1
 */
public class AssignmentOne {

    /** Width of the section rule printed between demonstration blocks. */
    private static final int RULE_WIDTH = 78;

    /**
     * Runs either the scripted demonstration or the interactive console.
     *
     * @param args pass {@code menu} to start the interactive console
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("menu")) {
            new ClinicMenu(new Clinic()).run();
            return;
        }

        Clinic clinic = new Clinic();
        List<MedicalProfessional> staff = clinic.getStaff();

        demonstrateStaffModel(staff);
        demonstrateRoleSpecificBehaviour(staff);
        demonstrateAppointmentSystem(staff);

        System.out.println();
        System.out.println("This run was the scripted demonstration required by Parts 3 and 6.");
        System.out.println("For the interactive console, run:");
        System.out.println("    java -cp bin AssignmentOne menu");
    }

    /**
     * Part 3: every professional is handled through one shared loop. The loop
     * only knows the abstract type, yet each object prints its own role and
     * detail, which is the polymorphic behaviour the brief requires.
     *
     * @param staff the clinic's staff
     */
    private static void demonstrateStaffModel(List<MedicalProfessional> staff) {
        printSection("PART 1 & 2 & 3 - STAFF MODEL AND POLYMORPHIC PRINTING");
        System.out.println("All staff are held in one List<MedicalProfessional>.");
        System.out.printf("Clinic employs %d professionals across %d concrete roles.%n",
                staff.size(), countDistinctRoles(staff));
        System.out.println();

        int index = 1;
        for (MedicalProfessional professional : staff) {
            System.out.printf("  %d. %s%n", index++, professional);
        }

        System.out.println();
        System.out.println("Note: the loop above never checks the concrete type.");
        System.out.println("Each object supplied its own toString() through the abstract class.");

        System.out.println();
        System.out.println("Encapsulation guard - the working-day set cannot be changed from outside:");
        MedicalProfessional first = staff.get(0);
        try {
            first.getWorkingDays().add(DayOfWeek.SUNDAY);
        } catch (UnsupportedOperationException exception) {
            System.out.println("  Attempt to modify working days was blocked, as designed.");
        }

        System.out.println();
        System.out.println("Encapsulation guard - an invalid staff ID is rejected at construction:");
        try {
            new Nurse("ABC", "Invalid Nurse", "Ward C", 1);
        } catch (IllegalArgumentException exception) {
            System.out.println("  Rejected: " + exception.getMessage());
        }

        System.out.println();
        System.out.println("Working-day check used by the roster:");
        System.out.printf("  %s works on Saturday: %s%n",
                staff.get(1).getName(), staff.get(1).worksOn(DayOfWeek.SATURDAY));
        System.out.printf("  %s works on Saturday: %s%n",
                staff.get(0).getName(), staff.get(0).worksOn(DayOfWeek.SATURDAY));
    }

    /**
     * Part 2: shows the behaviour that exists only on individual subclasses.
     * The base class has none of these methods, so this block proves the
     * subclasses carry their own data and their own operations.
     *
     * @param staff the clinic's staff
     */
    private static void demonstrateRoleSpecificBehaviour(List<MedicalProfessional> staff) {
        printSection("PART 2 - ROLE-SPECIFIC DATA AND BEHAVIOUR");

        GeneralPractitioner generalPractitioner = (GeneralPractitioner) staff.get(0);
        int bookedToday = 23;
        System.out.printf("GP %s is in %s. With %d patients already booked, "
                        + "can still accept another? %s%n",
                generalPractitioner.getName(),
                generalPractitioner.getConsultationRoom(),
                bookedToday,
                generalPractitioner.canAcceptMorePatients(bookedToday));

        SpecialistDoctor specialist = (SpecialistDoctor) staff.get(2);
        System.out.printf("Specialist %s (%s): standard fee $%.2f, review fee $%.2f%n",
                specialist.getName(),
                specialist.getSpecialty(),
                specialist.calculateFee(false),
                specialist.calculateFee(true));

        Nurse nurse = (Nurse) staff.get(4);
        System.out.printf("Nurse %s on %s holds duty level \"%s\" and can cover Ward A: %s%n",
                nurse.getName(), nurse.getWard(), nurse.getDutyLevel(),
                nurse.canCoverWard("Ward A"));

        Dietitian dietitian = (Dietitian) staff.get(6);
        System.out.printf("Dietitian %s (%s, %d-minute consults) recommends %d follow-up review(s) "
                        + "for an assessment score of 8.%n",
                dietitian.getName(), dietitian.getFocusArea(),
                dietitian.getConsultationMinutes(),
                dietitian.recommendFollowUpReviews(8));

        Physiotherapist physiotherapist = (Physiotherapist) staff.get(7);
        System.out.printf("Physiotherapist %s (%s) estimates %d sessions for severity 3.%n",
                physiotherapist.getName(), physiotherapist.getTreatmentApproach(),
                physiotherapist.estimateRecoverySessions(3));

        System.out.println();
        System.out.println("Each call above reached a method that only that subclass defines;");
        System.out.println("the shared base class deliberately does not expose them.");
    }

    /**
     * Parts 4, 5 and 6: runs the appointment system from booking through to
     * cancellation, then repeats the same operations with invalid input so the
     * warning paths are visible.
     *
     * @param staff the clinic's staff
     */
    private static void demonstrateAppointmentSystem(List<MedicalProfessional> staff) {
        LocalDate today = LocalDate.now();
        AppointmentManager manager = new AppointmentManager();

        GeneralPractitioner gpOne = (GeneralPractitioner) staff.get(0);
        GeneralPractitioner gpTwo = (GeneralPractitioner) staff.get(1);
        SpecialistDoctor cardiologist = (SpecialistDoctor) staff.get(2);
        Nurse nurse = (Nurse) staff.get(4);
        Dietitian dietitian = (Dietitian) staff.get(6);

        Patient mei = new Patient("Mei Lin", "0412345678");
        Patient jordan = new Patient("Jordan Pike", "0423456789");
        Patient ana = new Patient("Ana Duarte", "0434567890");
        Patient rob = new Patient("Rob Ellery", "0445678901");

        printSection("PART 4 & 5 - APPOINTMENT MANAGER CONFIGURATION");
        System.out.println("Appointments are same-day only. Today is " + today + ".");
        manager.printTimetable();

        printSection("PART 6.1 - SUCCESSFUL BOOKINGS");
        manager.addAppointment(mei, gpOne, today, LocalTime.of(9, 0));
        manager.addAppointment(jordan, gpOne, today, LocalTime.of(9, 30));
        manager.addAppointment(ana, gpTwo, today, LocalTime.of(9, 0));
        manager.addAppointment(rob, cardiologist, today, LocalTime.of(10, 0));
        manager.addAppointment(mei, nurse, today, LocalTime.of(11, 0));
        manager.addAppointment(jordan, dietitian, today, LocalTime.of(13, 30));

        System.out.println();
        System.out.println("Six bookings were accepted. The manager now holds "
                + manager.getAppointmentCount() + " appointments.");
        System.out.println("Note that Ana at 09:00 was accepted even though Mei is also at 09:00,");
        System.out.println("because the clash rule is per professional, not per clinic.");

        printSection("PART 6.2 - ALL APPOINTMENTS, SORTED BY TIME");
        manager.printAppointments("ALL APPOINTMENTS (CHRONOLOGICAL)", manager.getAllAppointmentsSorted());

        printSection("PART 5 - FILTERING BY PROFESSIONAL AND BY PATIENT");
        List<Appointment> gpOneBookings = manager.findAppointmentsByProfessional(gpOne.getStaffId());
        manager.printAppointments("FILTER: STAFF ID " + gpOne.getStaffId() + " (" + gpOne.getName() + ")",
                manager.sortAppointments(gpOneBookings));

        List<Appointment> meiBookings = manager.findAppointmentsByPatient(mei.getPhoneNumber());
        manager.printAppointments("FILTER: PATIENT " + mei.getPhoneNumber() + " (" + mei.getName() + ")",
                manager.sortAppointments(meiBookings));

        printSection("PART 6.3 - CANCELLATION AND UPDATED STATE");
        manager.cancelAppointment(gpOne.getStaffId(), today, LocalTime.of(9, 30));
        manager.printAppointments("AFTER CANCELLING GP 09:30 BOOKING", manager.getAllAppointmentsSorted());

        manager.cancelAppointmentsByPatient(jordan.getPhoneNumber());
        manager.printAppointments("AFTER CANCELLING PATIENT " + jordan.getPhoneNumber(),
                manager.getAllAppointmentsSorted());

        printSection("PART 6.4 - REJECTED OPERATIONS AND WARNINGS");
        System.out.println("The following calls are expected to fail.");

        System.out.println();
        System.out.println("1) Double booking: same professional, same time.");
        manager.addAppointment(ana, gpOne, today, LocalTime.of(9, 0));

        System.out.println();
        System.out.println("2) Cancelling a booking that does not exist.");
        manager.cancelAppointment(cardiologist.getStaffId(), today, LocalTime.of(15, 0));

        System.out.println();
        System.out.println("3) Booking outside the configured timetable.");
        manager.addAppointment(rob, nurse, today, LocalTime.of(7, 15));

        System.out.println();
        System.out.println("4) Booking a time that is not aligned to a slot.");
        manager.addAppointment(rob, nurse, today, LocalTime.of(9, 45));

        System.out.println();
        System.out.println("5) Cancelling by a phone number with no bookings.");
        manager.cancelAppointmentsByPatient("0499999999");

        System.out.println();
        System.out.println("6) Searching for a professional that does not exist.");
        manager.printAppointments("FILTER: STAFF ID 9999",
                manager.findAppointmentsByProfessional("9999"));

        System.out.println();
        System.out.println("7) Booking for a different day (the clinic is same-day only).");
        manager.addAppointment(mei, gpTwo, today.plusDays(1), LocalTime.of(10, 0));

        printSection("FINAL STATE AFTER THE DEMONSTRATION");
        manager.printAppointments("REMAINING APPOINTMENTS", manager.getAllAppointmentsSorted());
        System.out.println();
        System.out.println("Total remaining bookings: " + manager.getAppointmentCount());

        printSection("PART 5 - TIMETABLE IS CONFIGURATION, NOT HARD-CODED LOGIC");
        System.out.println("Reconfiguring the clinic to 09:00-17:00 in 20-minute slots.");
        manager.configureTimetable(LocalTime.of(9, 0), LocalTime.of(17, 0), 20);
        manager.printTimetable();
        System.out.println("Existing bookings were preserved: " + manager.getAppointmentCount()
                + " still held. The new hours govern future bookings only.");
    }

    /**
     * Counts how many distinct concrete roles exist in the staff list, using
     * the polymorphic role name rather than any type checking.
     *
     * @param staff the clinic's staff
     * @return the number of distinct role titles
     */
    private static int countDistinctRoles(List<MedicalProfessional> staff) {
        List<String> roles = new ArrayList<>();
        for (MedicalProfessional professional : staff) {
            if (!roles.contains(professional.getRole())) {
                roles.add(professional.getRole());
            }
        }
        return roles.size();
    }

    /**
     * Prints a ruled section heading so the demonstration output stays readable.
     *
     * @param title the heading text
     */
    private static void printSection(String title) {
        System.out.println();
        System.out.println(repeat('=', RULE_WIDTH));
        System.out.println(title);
        System.out.println(repeat('=', RULE_WIDTH));
    }

    /**
     * Builds a repeated character string without relying on external libraries.
     *
     * @param character the character to repeat
     * @param count     how many times to repeat it
     * @return the repeated string
     */
    private static String repeat(char character, int count) {
        char[] symbols = new char[count];
        Arrays.fill(symbols, character);
        return new String(symbols);
    }
}
