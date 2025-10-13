# Repository Guidelines

## Project Structure & Module Organization
The JavaFX application lives under `src/main/java/com/tsymq`; focus blockers sit in the `AppBlocker*` classes, scheduling rules in `mode/`, configuration loaders in `config/`, UI controllers in `ui/`, and AppleScript helpers in `utils/CommandUtil.java`. Resources for layouts, CSS, and icons are stored in `src/main/resources`, macOS packaging scripts in `scripts/`, and persisted user data within `config/`. Mirror the package tree below `src/test/java/com/tsymq` when adding tests and keep temporary fixtures in `src/test/resources`.

## Build, Test, and Development Commands
- `mvn clean compile` — resolve dependencies and compile sources.
- `mvn javafx:run` — launch the app via `com.tsymq.Main` for UI smoke checks.
- `mvn test` — execute the headless JUnit 5/TestFX suite.
- `./scripts/run.sh` — ensure Java is available, build if missing, then start the UI.
- `./scripts/build.sh` — clean, test, and emit `target/MyFocusme-1.0-SNAPSHOT.jar`.
- `./scripts/package-mac-minimal.sh` — produce the minimal macOS distribution.

## Coding Style & Naming Conventions
Use UTF-8 files with 4-space indentation. Keep classes in `PascalCase`, fields and methods in `camelCase`, and constants in `SCREAMING_SNAKE_CASE`. Package names stay lowercase and controller members must align with their FXML `fx:id`. Favor JavaFX bindings instead of manual listeners, keep imports tidy (static first), and remove diagnostic logging before committing.

## Testing Guidelines
Co-locate unit tests with their modules using `*Test` or `*Tests` suffixes so Surefire picks them up. Mockito covers service seams, and TestFX drives UI flows—tag long-running cases with `@Tag("slow")`. Stage synthetic config inputs in `src/test/resources`, protect real `config/*.txt`, and run `mvn test` (or `./scripts/build.sh`) before submitting changes.

## Commit & Pull Request Guidelines
Follow Conventional Commits (for example, `feat(mode): add weekly schedule`). Keep subjects short, present tense, and include scopes when that clarifies impact. Pull requests should outline the change, reference issues, list verification steps, and attach screenshots or GIFs for UI updates. Document AppleScript or configuration migrations in `doc/` and surface any macOS prerequisites for reviewers.

## Platform & Automation Notes
Route all macOS automation through `CommandUtil.executeAppleScript()` so focus rules and logging stay centralized. Remember that focus mode blocks only while `ModeManager` reports focus mode; hard-coded domains remain active in every mode. The daily automation resets to normal mode at 17:00—update that schedule inside `ModeManager` if requirements shift.
