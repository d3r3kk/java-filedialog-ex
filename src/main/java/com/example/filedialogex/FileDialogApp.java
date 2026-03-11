package com.example.filedialogex;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

/**
 * Demonstrates {@link FileDialog#setDirectory} and {@link FileDialog#setFile} on macOS.
 *
 * <p>Opens an AWT FileDialog with a configurable initial directory and file pre-selection,
 * then reports the user's choice (or cancellation) to stdout.
 *
 * <p>The interesting behaviour being tested is how macOS (Sequoia / Tahoe) reacts to
 * {@code setDirectory()} and {@code setFile()} — the two AWT methods under investigation.
 * All other FileDialog configuration is intentionally kept minimal to keep the focus there.
 */
public final class FileDialogApp {

    private FileDialogApp() {
        // Utility class — not instantiated.
    }

    /**
     * Application entry point.
     *
     * <p>Supported switches:
     * <ul>
     *   <li>{@code --help}  — print usage and exit</li>
     *   <li>{@code --dir <path>}  — directory passed to {@link FileDialog#setDirectory}</li>
     *   <li>{@code --fname <file>}  — file passed to {@link FileDialog#setFile}</li>
     * </ul>
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        String dir = null;
        String fname = null;

        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                printHelp();
                return;
            } else if ("--dir".equals(args[i])) {
                if (++i < args.length) {
                    dir = args[i];
                } else {
                    System.err.println("Error: --dir requires a path argument.");
                    System.exit(1);
                }
            } else if ("--fname".equals(args[i])) {
                if (++i < args.length) {
                    fname = args[i];
                } else {
                    System.err.println("Error: --fname requires a file argument.");
                    System.exit(1);
                }
            } else {
                System.err.println("Unknown option: " + args[i]);
                printHelp();
                System.exit(1);
            }
        }

        // --- Resolve directory and filename ---
        //
        // Rules (see README for rationale):
        //  • --dir always wins for setDirectory() unless --fname is absolute and --dir absent.
        //  • --fname may be absolute or relative; relative is resolved against the directory.
        final String resolvedDir;
        final String resolvedFile;

        if (fname != null) {
            File fFile = new File(fname);
            if (fFile.isAbsolute()) {
                // Absolute --fname: use its parent as directory when --dir is not supplied.
                resolvedDir = (dir != null) ? dir : fFile.getParent();
                resolvedFile = fFile.getName();
            } else {
                // Relative --fname: the filename stays as-is; directory comes from --dir / CWD.
                resolvedDir = (dir != null) ? dir : System.getProperty("user.dir");
                resolvedFile = fname;
            }
        } else {
            resolvedDir = (dir != null) ? dir : System.getProperty("user.dir");
            resolvedFile = null;
        }

        openFileDialog(resolvedDir, resolvedFile);
    }

    /**
     * Opens a {@link FileDialog} pre-configured with the supplied directory and optional filename,
     * then prints the result of the user interaction.
     *
     * <p>This method is the core of the experiment: it calls
     * {@link FileDialog#setDirectory(String)} unconditionally and
     * {@link FileDialog#setFile(String)} only when a filename was provided, so the effects
     * of each method can be observed independently.
     *
     * @param directory initial directory for {@link FileDialog#setDirectory}; never {@code null}
     * @param filename  initial filename for {@link FileDialog#setFile}, or {@code null} to skip
     */
    private static void openFileDialog(final String directory, final String filename) {
        Frame frame = new Frame();
        FileDialog dialog = new FileDialog(frame, "Select a file", FileDialog.LOAD);

        System.out.println("Opening FileDialog:");
        System.out.println("  setDirectory(\"" + directory + "\")");
        dialog.setDirectory(directory);

        if (filename != null) {
            System.out.println("  setFile(\"" + filename + "\")");
            dialog.setFile(filename);
        } else {
            System.out.println("  setFile() not called  (--fname not provided)");
        }

        dialog.setVisible(true);

        String selectedDir = dialog.getDirectory();
        String selectedFile = dialog.getFile();

        System.out.println();
        if (selectedFile != null) {
            System.out.println("Result : OK");
            System.out.println("  Directory : " + selectedDir);
            System.out.println("  File      : " + selectedFile);
            System.out.println("  Full path : " + selectedDir + selectedFile);
        } else {
            System.out.println("Result : Cancelled / closed.");
        }

        dialog.dispose();
        frame.dispose();
        System.exit(0);
    }

    /**
     * Prints usage information to stdout.
     */
    private static void printHelp() {
        System.out.println("Usage: java -jar java-filedialog-ex.jar [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --help           Print this help message and exit.");
        System.out.println("  --dir  <path>    Directory to open in the file dialog.");
        System.out.println("                   Default: current working directory.");
        System.out.println("  --fname <file>   File to pre-select in the file dialog.");
        System.out.println("                   May be an absolute or relative path.");
        System.out.println("                   If relative, --dir (or CWD) is used as the base.");
        System.out.println("                   If omitted, FileDialog.setFile() is not called.");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Open dialog in CWD (no args)");
        System.out.println("  java -jar java-filedialog-ex.jar");
        System.out.println();
        System.out.println("  # Open dialog in a specific directory");
        System.out.println("  java -jar java-filedialog-ex.jar --dir /Users/alice/Documents");
        System.out.println();
        System.out.println("  # Open dialog in a directory with a filename pre-filled");
        System.out.println("  java -jar java-filedialog-ex.jar --dir /Users/alice --fname report.pdf");
        System.out.println();
        System.out.println("  # Absolute --fname (directory extracted from path)");
        System.out.println("  java -jar java-filedialog-ex.jar --fname /Users/alice/report.pdf");
    }
}
