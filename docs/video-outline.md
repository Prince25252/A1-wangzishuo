# Presentation Video - Speaking Outline

The brief requires **your own** spoken explanation: name the pillar, explain it
in your own words, point at the code, and justify why it is used there. Use this
outline only as a checklist of what to cover and where to look, then speak it
yourself in your own words. Keep the video under five minutes.

## Before recording

- Open the project in VS Code so the class files are visible on screen.
- Have `MedicalProfessional.java` and `AssignmentOne.java` open in tabs.
- Say each point once, in plain language, as if explaining to a classmate.

## 1. Abstraction (about 60 seconds)

- **What it is:** keeping only the shared, essential characteristics in one
  place and leaving the specifics to subclasses.
- **Where:** `MedicalProfessional` is declared `abstract`. It is abstract
  because a "generic medical professional" is not a real bookable role - a
  receptionist cannot book "a professional", only a GP, a nurse, a dietitian.
  It also declares `getRole()` and `getRoleDetails()` without bodies, so each
  subclass must say what it is.
- **Why here:** the five roles share identifier, name and working days, but
  nothing else; abstracting the shared part is what stops the shared code from
  being copied five times.
- Second example to mention if time allows: `AppointmentManager`
  `getAvailableSlots()` hides the slot arithmetic, so `main` never computes
  slot boundaries.

## 2. Encapsulation (about 60 seconds)

- **What it is:** private state, controlled access, guarantees that an object
  can never sit in an invalid state.
- **Where:**
  - all fields are `private`;
  - `staffId` is `final` and validated as digits-only in `requireNumericId`;
  - `setName` validates rather than accepting anything;
  - `getWorkingDays()` returns `Collections.unmodifiableSet`, and the
  constructor stores a defensive `EnumSet.copyOf`, so outside code cannot add
  or remove days;
  - `Patient` validates the phone number to exactly ten digits, so an
  appointment can never hold an unusable contact.
- **Why here:** the appointment manager trusts these objects. If any caller
  could rewrite the ID or the roster, clash detection and booking rules would
  silently break.
- Show it live if you can: the program prints the blocked attempt to modify
  working days and the rejected `"ABC"` staff ID.

## 3. Inheritance (about 60 seconds)

- **What it is:** a subclass reuses its parent's state and behaviour and adds
  only what is new.
- **Where:** `GeneralPractitioner`, `SpecialistDoctor`, `Nurse`, `Dietitian`
  and `Physiotherapist` all extend `MedicalProfessional` and call `super(...)`
  for the shared fields. Each one adds only its own data - a room, a specialty
  and fee, a ward and years of experience, a focus area and consultation length,
  a treatment approach - and only its own behaviour, such as
  `calculateFee`, `canCoverWard` or `estimateRecoverySessions`.
- **Why here:** identifier validation, roster handling and `toString` assembly
  exist once. Adding a sixth role is a new file, not five edited files.
- Also mention constructor overloading: each subclass has a full constructor and
  a shorter one that falls back to the default Monday-to-Friday roster.

## 4. Polymorphism (about 60 seconds)

- **What it is:** one piece of code handles objects of different concrete types
  through a shared type, and each object behaves according to its own class.
- **Where:** `AssignmentOne.demonstrateStaffModel` holds everything in a single
  `List<MedicalProfessional>` and prints them in one loop. That loop never
  checks whether an object is a GP or a nurse; `toString()` is resolved on each
  object at run time, so every line prints its own role and its own details.
- **Why here:** Part 3 explicitly asks that adding a new subclass require
  minimal change to the printing code. Adding `Pharmacist` would need zero
  changes to that loop.
- Show it: run the program and point at how each printed line labels a different
  role but comes from the same loop.
- Mention overriding: `toString()` is overridden in every subclass and each one
  calls `super.toString()` first, then appends its own detail.

## 5. Close (about 20 seconds)

- One sentence on what the system does: books patients into slots with
  professionals, prevents double bookings, sorts and filters, and cancels.
- Confirm you wrote it and can explain any part on request.
