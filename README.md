# PROG2004 Assessment One - Health Service Appointment System

A Java object model for a small health clinic that books patients into
appointment slots with medical professionals. The assessment brief requires the
model only: there is no graphical interface, no login, no menu and no
persistent storage. Everything is simulated from `AssignmentOne.main`.

## Assessment mapping

| Part | Requirement | Where it is implemented |
| --- | --- | --- |
| 1 | Generic professional model, encapsulated, readable `toString`, overloaded constructors | `MedicalProfessional` (abstract, private fields, validated setters, immutable working-day view, two constructors) |
| 2 | At least four specialisations including a GP, each with unique data and behaviour | `GeneralPractitioner`, `SpecialistDoctor`, `Nurse`, `Dietitian`, `Physiotherapist` (five subclasses) |
| 3 | Use the professionals, print them through one shared loop | `AssignmentOne.demonstrateStaffModel` keeps a `List<MedicalProfessional>` and never checks concrete type |
| 4 | Appointment model, patients, time ordering, readable `toString` | `Appointment` (`Comparable<Appointment>`), `Patient` |
| 5 | Appointment manager: add, prevent double booking, filter, sort, cancel, warnings, configurable timetable | `AppointmentManager` |
| 6 | End-to-end demonstration including rejected operations | `AssignmentOne.demonstrateAppointmentSystem` |

## Class structure

```
MedicalProfessional        (abstract base: staffId, name, workingDays)
├── GeneralPractitioner    (consultation room, daily patient limit)
├── SpecialistDoctor       (specialty, standard fee)
├── Nurse                  (ward, years of experience)
├── Dietitian              (focus area, consultation length)
└── Physiotherapist        (treatment approach, treatment rooms)

Patient                    (name, validated ten-digit phone number)
Appointment                (patient, professional, date, start time) implements Comparable
AppointmentManager         (ArrayList<Appointment>, configurable timetable)
AssignmentOne              (main, demonstration only)
```

The project uses the default package, matching the brief's instruction to
create `AssignmentOne` directly under `src`.

## How the four OOP pillars appear

**Abstraction.** `MedicalProfessional` is abstract because a "generic
professional" is not a real, bookable role. It declares `getRole()` and
`getRoleDetails()` without implementing them, so every concrete role must state
what it is. `AppointmentManager` hides timetable arithmetic behind
`getAvailableSlots()`, so callers never compute slot boundaries themselves.

**Encapsulation.** Every field in `MedicalProfessional`, `Patient` and
`Appointment` is private. The staff ID is final and validated as digits-only.
The working-day set is copied on the way in and returned as an unmodifiable view,
so callers cannot push an object into an invalid state. Phone numbers are
validated to exactly ten digits, so no appointment can hold an unusable contact.

**Inheritance.** All five roles extend `MedicalProfessional` through `super(...)`,
inheriting identifier validation, working-day handling and `toString` assembly
instead of duplicating them. Each subclass adds only its own fields and its own
behaviour.

**Polymorphism.** Part 3 and Part 6 drive every professional through a single
`List<MedicalProfessional>` loop. Adding a sixth role requires no change to that
loop: the new class supplies its own `getRole()`, `getRoleDetails()` and
`toString()`. Overloading appears in the subclasses' two constructors
(full roster vs default Monday-to-Friday).

## Business rules implemented

- The clinic runs 08:00-16:00 in 30-minute slots, stored as configuration
  (opening time, closing time, slot length) rather than hard-coded values.
- Appointments are same-day only; other dates are rejected.
- Times must fall exactly on a generated slot boundary.
- One professional cannot hold two bookings at the same time. The check uses
  `MedicalProfessional.equals` (by staff ID) and `Appointment.equals`
  (by professional, date and start time).
- Filtering is available by staff ID and by patient phone number.
- Sorting is chronological, and is applied to filtered results as well as the
  full list.
- Cancellation works by staff ID plus time, or by patient phone number.
- Every rejection prints a `WARNING:` line instead of silently failing.

## Build and run

Requires JDK 17 or newer.

From the project root:

```
javac -d bin src/*.java
java -cp bin AssignmentOne
```

In VS Code: open the project folder, install the *Extension Pack for Java*,
then run `AssignmentOne` from the Run and Debug view. The bundled
`.vscode/settings.json` points the Java runtime at JDK 17.

## Development records

- `git log --oneline` shows the work was committed incrementally rather than in
  a single final commit.
- WakaTime tracks coding time through the WakaTime VS Code extension
  (`wakatime.vscode-wakatime`); the project `wakatime.cfg` API key is
  configured locally and is not committed.

## Generative AI declaration

See `GENAI-DECLARATION.md`. Fill in whichever statement applies and paste it
into the submission comments box on Blackboard.
