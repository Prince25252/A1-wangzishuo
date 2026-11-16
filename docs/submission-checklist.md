# Submission Checklist - PROG2004 Assessment One

Due: Monday 16 November 2026, 23:59 (Week 4). No resubmission after the deadline,
so check everything before you submit.

## 1. Source archive

- [ ] Folder is named `A1-<your MySCU username>`.
- [ ] `src/` contains all ten `.java` files.
- [ ] Archive is named `A1-<your MySCU username>.zip`.
- [ ] Archive opens cleanly and contains the folder, not loose files.
- [ ] `javac -d bin src/*.java` succeeds from a clean checkout.
- [ ] `java -cp bin AssignmentOne` runs without exceptions.

## 2. GitHub repository

- [ ] A repository exists and the history shows incremental commits
      (`git log --oneline` shows seven commits here, not one).
- [ ] If the repository is private, the lecturer has been invited as a collaborator.
- [ ] Link submitted with the archive.

## 3. WakaTime

- [ ] The WakaTime VS Code extension was enabled while you worked.
- [ ] Screenshot or report exported from the WakaTime dashboard shows time
      logged against the project.

## 4. Presentation video

- [ ] Video is five minutes or shorter.
- [ ] All four pillars are covered: name it, explain it in your own words,
      locate it in the code, justify why it is used there.
- [ ] Shared through a OneDrive link that the lecturer can open.
- [ ] Audio is audible and the code is readable on screen.

## 5. GenAI declaration

- [ ] One statement from `GENAI-DECLARATION.md` is pasted into the submission
      comments, matching what you actually did.

## 6. Before you press submit

- [ ] You can explain every class, every method and every design decision in
      this project. The brief allows a short interview and treats an
      unexplainable submission as an academic integrity breach.
- [ ] You have read the code yourself end to end, not only run it.
- [ ] Last submission before the deadline is the one you want marked.

## Local commands used for this project

```
# compile
javac -encoding UTF-8 -d bin src/*.java

# run: opens the interactive console (menu option 13 shows the demonstration)
java -cp bin AssignmentOne

# run: the demonstration on its own (this is what docs/demo-output.txt records)
java -cp bin AssignmentOne demo

# review history
git log --oneline
```
