import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Reads tasks from, and writes tasks to, a text file on the hard disk.
 *
 * The file holds one task per line, with its fields separated by " | ":
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | June 6th
 * E | 0 | project meeting | Aug 6th 2pm | 4pm
 * </pre>
 * A plain text format is used rather than Java serialization so that the file
 * stays readable, which makes it easy to check what was saved.
 *
 * Known limitation: a description that itself contains " | " is split into
 * separate fields when read back, so such a task loses everything after the
 * first bar. Escaping the separator would fix this, at the cost of a format
 * that is harder to read and harder to write.
 */
public class Storage {
    /** Location of the save file, relative to the project root. */
    private static final String FILE_PATH = "./data/tasks.txt";

    /** Separates the fields of one task, both when writing and when reading. */
    public static final String FIELD_SEPARATOR = " | ";

    /**
     * Pattern matching the field separator when splitting a line.
     *
     * The "|" is escaped because {@code String.split} takes a regular
     * expression, and an unescaped "|" means "or" there rather than a
     * literal bar.
     */
    private static final String FIELD_SEPARATOR_PATTERN = " \\| ";

    /** Value written in the second field of a task that is done. */
    private static final String DONE_MARKER = "1";

    /** Number of fields a line needs before the task-specific details. */
    private static final int COMMON_FIELD_COUNT = 3;

    /**
     * Loads saved tasks into the given array.
     *
     * A missing save file is treated as an empty task list rather than an
     * error, so that the very first run of the program works normally. Lines
     * that cannot be understood are skipped, so one damaged line does not cost
     * the user every other task.
     *
     * @param tasks Array to fill, starting from index 0.
     * @return Number of tasks loaded.
     * @throws HaroException If the save file exists but cannot be read.
     */
    public static int load(Task[] tasks) throws HaroException {
        File saveFile = new File(FILE_PATH);
        if (!saveFile.exists()) {
            return 0;
        }

        int taskCount = 0;
        try (Scanner fileScanner = new Scanner(saveFile)) {
            while (fileScanner.hasNextLine() && taskCount < tasks.length) {
                Task task = parseTask(fileScanner.nextLine());
                if (task != null) {
                    tasks[taskCount] = task;
                    taskCount++;
                }
            }
        } catch (FileNotFoundException e) {
            throw new HaroException("I couldn't read " + FILE_PATH
                    + ", so I'm starting with an empty list.");
        }
        return taskCount;
    }

    /**
     * Writes the given tasks to the save file, replacing whatever was there.
     *
     * The whole list is rewritten on every change. That is simpler than
     * editing the one line that changed, and the list is far too small for the
     * extra writing to matter.
     *
     * @param tasks Array holding the tasks.
     * @param taskCount Number of tasks in the array, counting from index 0.
     * @throws HaroException If the file or its folder cannot be written to.
     */
    public static void save(Task[] tasks, int taskCount) throws HaroException {
        File saveDirectory = new File(FILE_PATH).getParentFile();
        if (saveDirectory != null && !saveDirectory.exists()) {
            saveDirectory.mkdirs();
        }

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            for (int i = 0; i < taskCount; i++) {
                writer.write(tasks[i].toFileFormat() + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new HaroException("I couldn't save to " + FILE_PATH
                    + ", so this change may be lost when I close.");
        }
    }

    /**
     * Turns one line of the save file back into a task.
     *
     * @param line Line read from the save file.
     * @return The task the line describes, or null if the line cannot be read.
     */
    private static Task parseTask(String line) {
        String[] fields = line.split(FIELD_SEPARATOR_PATTERN);
        if (fields.length < COMMON_FIELD_COUNT) {
            return null;
        }

        String typeIcon = fields[0];
        boolean isDone = fields[1].equals(DONE_MARKER);
        String description = fields[2];

        Task task;
        switch (typeIcon) {
        case "T":
            task = new Todo(description);
            break;
        case "D":
            if (fields.length < COMMON_FIELD_COUNT + 1) {
                return null;
            }
            task = new Deadline(description, fields[3]);
            break;
        case "E":
            if (fields.length < COMMON_FIELD_COUNT + 2) {
                return null;
            }
            task = new Event(description, fields[3], fields[4]);
            break;
        default:
            return null;
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }
}
