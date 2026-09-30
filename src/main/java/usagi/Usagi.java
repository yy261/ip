package usagi;

import java.util.ArrayList;

/**
 * Entry point for the Usagi chatbot.
 *
 * Greets the user, then repeatedly reads a command and responds to it, until
 * the user types "bye". Usagi can list tasks, mark or unmark them as done,
 * delete them, and record three kinds of tasks: todos, deadlines and events.
 */
public class Usagi {
    private static final String COMMAND_BYE = "bye";
    private static final String COMMAND_LIST = "list";
    private static final String COMMAND_MARK = "mark";
    private static final String COMMAND_UNMARK = "unmark";
    private static final String COMMAND_TODO = "todo";
    private static final String COMMAND_DEADLINE = "deadline";
    private static final String COMMAND_EVENT = "event";
    private static final String COMMAND_DELETE = "delete";

    /** Separates a deadline's description from its due date, e.g. "return book /by Sunday". */
    private static final String DEADLINE_BY_DELIMITER = " /by ";

    /** Separates an event's description from its start time. */
    private static final String EVENT_FROM_DELIMITER = " /from ";

    /** Separates an event's start time from its end time. */
    private static final String EVENT_TO_DELIMITER = " /to ";

    /** Reminder of the expected deadline format, shown whenever a deadline cannot be read. */
    private static final String DEADLINE_FORMAT_HINT =
            "hAA, HAAA->(A deadline needs a description and a due date, "
            + "e.g. \"deadline return book /by Sunday\".)";

    /** Reminder of the expected event format, shown whenever an event cannot be read. */
    private static final String EVENT_FORMAT_HINT =
            "eeeeeYAHA->(An event needs a description, a start and an end, "
            + "e.g. \"event project meeting /from Mon 2pm /to 4pm\".)";

    /**
     * Tasks recorded so far, in the order they were added.
     *
     * An {@code ArrayList} is used rather than a fixed-size array so that the
     * list can grow as needed and so that removing a task automatically shifts
     * the tasks after it, which is what the "delete" command needs.
     */
    private final ArrayList<Task> tasks = new ArrayList<>();

    /** Reads the user's commands and prints Usagi's responses. */
    private final Ui ui;

    /** Saves the tasks to, and loads them from, the hard disk. */
    private final Storage storage;

    /**
     * Creates a Usagi chatbot that saves its tasks to the given file.
     *
     * @param filePath Location of the save file.
     */
    public Usagi(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
    }

    /**
     * Starts the Usagi chatbot.
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        new Usagi("./data/tasks.txt").run();
    }

    /**
     * Runs Usagi's greet-read-respond loop until the user types "bye".
     */
    public void run() {
        ui.showWelcome();
        loadTasks();
        runCommandLoop();
        ui.showFarewell();
    }

    /**
     * Restores the tasks saved by the previous run.
     *
     * A failure to load is reported and then ignored, so that a missing or
     * unreadable save file leaves the user with an empty list rather than no
     * program at all.
     */
    private void loadTasks() {
        try {
            tasks.addAll(storage.load());
        } catch (UsagiException e) {
            ui.showError(e.getMessage());
        }
    }

    /**
     * Writes the current tasks to the hard disk.
     *
     * Called after every change to the list, so that the saved file always
     * matches what the user sees and nothing is lost if the program stops
     * without reaching the "bye" command.
     */
    private void saveTasks() {
        try {
            storage.save(tasks);
        } catch (UsagiException e) {
            ui.showError(e.getMessage());
        }
    }

    /**
     * Reads and executes commands until the exit command is entered.
     *
     * A command that the user got wrong is reported and then forgotten, so a
     * mistake never ends the session. This is the one place where problems
     * raised anywhere inside a command are turned into a printed response.
     */
    private void runCommandLoop() {
        boolean isExitRequested = false;
        while (!isExitRequested) {
            String input = ui.readCommand();
            try {
                isExitRequested = executeCommand(input);
            } catch (UsagiException e) {
                ui.showError(e.getMessage());
            }
        }
        ui.close();
    }

    /**
     * Executes a single user command and prints its response.
     *
     * @param input Full line of input entered by the user.
     * @return True if the user asked to exit, false otherwise.
     * @throws UsagiException If the command is not recognised, or its arguments
     *                       are missing or cannot be used.
     */
    private boolean executeCommand(String input) throws UsagiException {
        if (input.isEmpty()) {
            throw new UsagiException("HAAAAAA->(I didn't catch that. Type a command, or \"bye\" to leave.)");
        }

        String commandWord = getCommandWord(input);
        String arguments = getCommandArguments(input);

        switch (commandWord) {
        case COMMAND_BYE:
            return true;
        case COMMAND_LIST:
            printTaskList();
            break;
        case COMMAND_MARK:
            setTaskDoneStatus(arguments, true);
            break;
        case COMMAND_UNMARK:
            setTaskDoneStatus(arguments, false);
            break;
        case COMMAND_TODO:
            addTask(parseTodo(arguments));
            break;
        case COMMAND_DEADLINE:
            addTask(parseDeadline(arguments));
            break;
        case COMMAND_EVENT:
            addTask(parseEvent(arguments));
            break;
        case COMMAND_DELETE:
            deleteTask(arguments);
            break;
        default:
            throw new UsagiException("Una Yahaha->(Sorry, I don't know what \"" + commandWord + "\" means.) "
                    + "Yaha Una yahauna->(I understand: todo, deadline, event, list, mark, unmark, delete and bye.)");
        }
        return false;
    }

    /**
     * Returns the first word of the input, which identifies the command.
     *
     * @param input Full line of input entered by the user.
     * @return The command word, or an empty string if the input is empty.
     */
    private String getCommandWord(String input) {
        return input.split(" ", 2)[0];
    }

    /**
     * Returns everything after the command word.
     *
     * @param input Full line of input entered by the user.
     * @return The arguments, or an empty string if there are none.
     */
    private String getCommandArguments(String input) {
        String[] parts = input.split(" ", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }

    /**
     * Creates a todo from the text following the "todo" command word.
     *
     * @param arguments Text following the "todo" command word.
     * @return The todo described by the arguments.
     * @throws UsagiException If no description was given.
     */
    private Todo parseTodo(String arguments) throws UsagiException {
        if (arguments.isEmpty()) {
            throw new UsagiException("Unana yaha->(A todo needs a description, e.g. \"todo borrow book\".)");
        }
        return new Todo(arguments);
    }

    /**
     * Creates a deadline from arguments of the form "description /by date".
     *
     * @param arguments Text following the "deadline" command word.
     * @return The deadline described by the arguments.
     * @throws UsagiException If the "/by" separator is missing, or either the
     *                       description or the due date is empty.
     */
    private Deadline parseDeadline(String arguments) throws UsagiException {
        String[] parts = arguments.split(DEADLINE_BY_DELIMITER, 2);
        if (parts.length < 2) {
            throw new UsagiException(DEADLINE_FORMAT_HINT);
        }

        String description = parts[0].trim();
        String by = parts[1].trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new UsagiException(DEADLINE_FORMAT_HINT);
        }
        return new Deadline(description, by);
    }

    /**
     * Creates an event from arguments of the form
     * "description /from start /to end".
     *
     * @param arguments Text following the "event" command word.
     * @return The event described by the arguments.
     * @throws UsagiException If either separator is missing or out of order, or
     *                       any of the three parts is empty.
     */
    private Event parseEvent(String arguments) throws UsagiException {
        String[] descriptionAndTimes = arguments.split(EVENT_FROM_DELIMITER, 2);
        if (descriptionAndTimes.length < 2) {
            throw new UsagiException(EVENT_FORMAT_HINT);
        }

        String[] times = descriptionAndTimes[1].split(EVENT_TO_DELIMITER, 2);
        if (times.length < 2) {
            throw new UsagiException(EVENT_FORMAT_HINT);
        }

        String description = descriptionAndTimes[0].trim();
        String from = times[0].trim();
        String to = times[1].trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new UsagiException(EVENT_FORMAT_HINT);
        }
        return new Event(description, from, to);
    }

    /**
     * Stores a task and confirms it to the user.
     *
     * @param task Task to store.
     */
    private void addTask(Task task) {
        tasks.add(task);
        saveTasks();
        ui.showResponse("Puru Yaha->(Got it. I've added this task:",
                "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.)");
    }

    /**
     * Removes the task at the given position from the list, and reports it.
     *
     * The removed task is read before it is removed so that it can still be
     * shown to the user in the confirmation message.
     *
     * @param arguments Text following the command word, holding the task number
     *                  as shown by the "list" command (starting from 1).
     * @throws UsagiException If no task number was given, if it is not a number,
     *                       or if no task has that number.
     */
    private void deleteTask(String arguments) throws UsagiException {
        int taskIndex = parseTaskIndex(arguments);
        Task removedTask = tasks.remove(taskIndex);
        saveTasks();
        ui.showResponse("HaAAA->(Noted. I've removed this task:",
                "  " + removedTask,
                "Now you have " + tasks.size() + " tasks in the list.)");
    }

    /**
     * Marks the task at the given position as done or not done, and reports it.
     *
     * @param arguments Text following the command word, holding the task number
     *                  as shown by the "list" command (starting from 1).
     * @param isDone True to mark the task as done, false to mark it as not done.
     * @throws UsagiException If no task number was given, if it is not a number,
     *                       or if no task has that number.
     */
    private void setTaskDoneStatus(String arguments, boolean isDone) throws UsagiException {
        int taskIndex = parseTaskIndex(arguments);
        Task task = tasks.get(taskIndex);
        if (isDone) {
            task.markAsDone();
            saveTasks();
            ui.showResponse("Nice! I've marked this task as done:", "  " + task);
        } else {
            task.markAsNotDone();
            saveTasks();
            ui.showResponse("OK, I've marked this task as not done yet:", "  " + task);
        }
    }

    /**
     * Converts a task number typed by the user into an index into the task list.
     *
     * Rejecting a bad number here, rather than letting the list or
     * {@code Integer.parseInt} fail later, is what lets the caller assume the
     * index it receives is always safe to use.
     *
     * @param arguments Text following the command word, holding the task number
     *                  as shown by the "list" command (starting from 1).
     * @return The matching index into {@code tasks} (starting from 0).
     * @throws UsagiException If the text is missing, is not a number, or names a
     *                       task that does not exist.
     */
    private int parseTaskIndex(String arguments) throws UsagiException {
        if (arguments.isEmpty()) {
            throw new UsagiException("Huunnn->(Which task? Give me its number, e.g. \"mark 2\" or \"delete 2\".)");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(arguments);
        } catch (NumberFormatException e) {
            throw new UsagiException("Prrurururur->(\"" + arguments + "\" is not a task number. Try \"mark 2\".)");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new UsagiException("uNAAA->(There is no task " + taskNumber + ". You have "
                    + tasks.size() + " task(s) so far.)");
        }
        return taskNumber - 1;
    }

    /**
     * Prints all stored tasks, numbered from 1.
     */
    private void printTaskList() {
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        ui.showResponse(lines);
    }
}
