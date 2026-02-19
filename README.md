# java-filedialog-ex

An experiment that demonstrates the behaviour of
[`FileDialog.setDirectory()`](https://docs.oracle.com/en/java/se/17/docs/api/java.desktop/java/awt/FileDialog.html#setDirectory(java.lang.String))
and
[`FileDialog.setFile()`](https://docs.oracle.com/en/java/se/17/docs/api/java.desktop/java/awt/FileDialog.html#setFile(java.lang.String))
on modern macOS (Sequoia 15 / Tahoe 26).

> **Why this exists:** macOS rounds-trips for AWT `FileDialog` have subtle
> platform-specific quirks when pre-seeding the dialog with a directory or file
> name.  This project isolates those two methods so their behaviour can be
> observed cleanly across JDK versions (11, 17, 21, 25) and architectures
> (x64, aarch64).

---

## Build

Requires Java 11+ and Maven 3.6+.

```bash
mvn package
```

This produces a self-contained fat JAR at:

```
target/java-filedialog-ex-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Lint

```bash
mvn checkstyle:check
```

Uses the project-level `checkstyle.xml` (practical subset of Sun/Google Java
style: naming, Javadoc on public API, no star-imports, 4-space indentation,
120-char line limit).

---

## Run

```bash
java -jar target/java-filedialog-ex-1.0-SNAPSHOT-jar-with-dependencies.jar [OPTIONS]
```

### Options

| Switch | Description |
|--------|-------------|
| `--help` | Print usage and exit. |
| `--dir <path>` | Directory passed to `FileDialog.setDirectory()`. Defaults to the current working directory. |
| `--fname <file>` | File passed to `FileDialog.setFile()`. May be absolute or relative. If relative, `--dir` (or CWD) is used as the base. If omitted, `setFile()` is not called. |

### Examples

```bash
# Open in CWD (no setFile call)
java -jar java-filedialog-ex.jar

# Specific directory
java -jar java-filedialog-ex.jar --dir /Users/alice/Documents

# Directory + pre-selected filename
java -jar java-filedialog-ex.jar --dir /Users/alice --fname report.pdf

# Absolute fname — directory is inferred from the path
java -jar java-filedialog-ex.jar --fname /Users/alice/report.pdf
```

### Output

When the user clicks **OK**:
```
Result : OK
  Directory : /Users/alice/Documents/
  File      : report.pdf
  Full path : /Users/alice/Documents/report.pdf
```

When the user cancels or closes the dialog:
```
Result : Cancelled / closed.
```

---

## CI

GitHub Actions runs on every push and pull request:

* **Matrix:** JDK 11, 17, 21, 25 (Temurin) on `ubuntu-latest`.
* **Steps per matrix job:** compile → assemble fat JAR → checkstyle → smoke
  test (`--help` exits before opening any AWT window, so it runs safely in a
  headless CI environment).
* **Artifact:** The fat JAR is uploaded once (from the Java 21 build).  The
  same JAR works on all supported JVM versions and architectures because it
  targets Java 11 bytecode.

---

## Code notes

`FileDialogApp.java` is intentionally a single, self-contained class.

* **`main()`** — parses CLI arguments and resolves the effective directory and
  filename before touching any AWT class, so `--help` is always safe to run
  headlessly.
* **`openFileDialog()`** — the core of the experiment.  Calls
  `setDirectory()` unconditionally and `setFile()` only when `--fname` was
  given, so either method can be tested in isolation.
* **`printHelp()`** — plain `System.out` to keep it testable without a display.

### Filename resolution rules

| `--dir` | `--fname` | `setDirectory()` receives | `setFile()` receives |
|---------|-----------|--------------------------|----------------------|
| given   | not given | `--dir` value            | _(not called)_       |
| not given | not given | CWD                    | _(not called)_       |
| given   | relative  | `--dir` value            | filename as-is       |
| not given | relative | CWD                    | filename as-is       |
| given   | absolute  | `--dir` value            | `File.getName()`     |
| not given | absolute | parent of `--fname`    | `File.getName()`     |
