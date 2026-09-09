import java.util.Scanner;

/**
 * Entry point for the Haro chatbot.
 *
 * Greets the user, then repeatedly reads a command and responds to it, until
 * the user types "bye". Haro can list tasks, mark or unmark them as done, and
 * record three kinds of tasks: todos, deadlines and events.
 */
public class Haro {
    private static final String BANNER = "  _   _                 \n"
            + " | | | | __ _ _ __ ___  \n"
            + " | |_| |/ _` | '__/ _ \\ \n"
            + " |  _  | (_| | | | (_) |\n"
            + " |_| |_|\\__,_|_|  \\___/ \n";
    private static final String HORIZONTAL_LINE = "_".repeat(60);

    private static final String COMMAND_BYE = "bye";
    private static final String COMMAND_LIST = "list";
    private static final String COMMAND_MARK = "mark";
    private static final String COMMAND_UNMARK = "unmark";
    private static final String COMMAND_TODO = "todo";
    private static final String COMMAND_DEADLINE = "deadline";
    private static final String COMMAND_EVENT = "event";

    /** Separates a deadline's description from its due date, e.g. "return book /by Sunday". */
    private static final String DEADLINE_BY_DELIMITER = " /by ";

    /** Separates an event's description from its start time. */
    private static final String EVENT_FROM_DELIMITER = " /from ";

    /** Separates an event's start time from its end time. */
    private static final String EVENT_TO_DELIMITER = " /to ";

    /** Reminder of the expected deadline format, shown whenever a deadline cannot be read. */
    private static final String DEADLINE_FORMAT_HINT =
            "A deadline needs a description and a due date, "
            + "e.g. \"deadline return book /by Sunday\".";

    /** Reminder of the expected event format, shown whenever an event cannot be read. */
    private static final String EVENT_FORMAT_HINT =
            "An event needs a description, a start and an end, "
            + "e.g. \"event project meeting /from Mon 2pm /to 4pm\".";

    /** Maximum number of tasks Haro can store, since a fixed-size array is used. */
    private static final int MAX_TASKS = 100;

    private static final Task[] tasks = new Task[MAX_TASKS];
    private static int taskCount = 0;

    /**
     * Runs Haro's greet-read-respond loop until the user types "bye".
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        printGreeting();
        runCommandLoop();
        printFarewell();
    }

    /**
     * Reads and executes commands until the exit command is entered.
     *
     * A command that the user got wrong is reported and then forgotten, so a
     * mistake never ends the session. This is the one place where problems
     * raised anywhere inside a command are turned into a printed response.
     */
    private static void runCommandLoop() {
        Scanner scanner = new Scanner(System.in);
        boolean isExitRequested = false;
        while (!isExitRequested) {
            String input = scanner.nextLine().trim();
            try {
                isExitRequested = executeCommand(input);
            } catch (HaroException e) {
                printResponse(e.getMessage());
            }
        }
        scanner.close();
    }

    /**
     * Executes a single user command and prints its response.
     *
     * @param input Full line of input entered by the user.
     * @return True if the user asked to exit, false otherwise.
     * @throws HaroException If the command is not recognised, or its arguments
     *                       are missing or cannot be used.
     */
    private static boolean executeCommand(String input) throws HaroException {
        if (input.isEmpty()) {
            throw new HaroException("I didn't catch that. Type a command, or \"bye\" to leave.");
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
        default:
            throw new HaroException("Sorry, I don't know what \"" + commandWord + "\" means. "
                    + "I understand: todo, deadline, event, list, mark, unmark and bye.");
        }
        return false;
    }

    /**
     * Returns the first word of the input, which identifies the command.
     *
     * @param input Full line of input entered by the user.
     * @return The command word, or an empty string if the input is empty.
     */
    private static String getCommandWord(String input) {
        return input.split(" ", 2)[0];
    }

    /**
     * Returns everything after the command word.
     *
     * @param input Full line of input entered by the user.
     * @return The arguments, or an empty string if there are none.
     */
    private static String getCommandArguments(String input) {
        String[] parts = input.split(" ", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }

    /**
     * Creates a todo from the text following the "todo" command word.
     *
     * @param arguments Text following the "todo" command word.
     * @return The todo described by the arguments.
     * @throws HaroException If no description was given.
     */
    private static Todo parseTodo(String arguments) throws HaroException {
        if (arguments.isEmpty()) {
            throw new HaroException("A todo needs a description, e.g. \"todo borrow book\".");
        }
        return new Todo(arguments);
    }

    /**
     * Creates a deadline from arguments of the form "description /by date".
     *
     * @param arguments Text following the "deadline" command word.
     * @return The deadline described by the arguments.
     * @throws HaroException If the "/by" separator is missing, or either the
     *                       description or the due date is empty.
     */
    private static Deadline parseDeadline(String arguments) throws HaroException {
        String[] parts = arguments.split(DEADLINE_BY_DELIMITER, 2);
        if (parts.length < 2) {
            throw new HaroException(DEADLINE_FORMAT_HINT);
        }

        String description = parts[0].trim();
        String by = parts[1].trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new HaroException(DEADLINE_FORMAT_HINT);
        }
        return new Deadline(description, by);
    }

    /**
     * Creates an event from arguments of the form
     * "description /from start /to end".
     *
     * @param arguments Text following the "event" command word.
     * @return The event described by the arguments.
     */
    private static Event parseEvent(String arguments) {
        String[] descriptionAndTimes = arguments.split(EVENT_FROM_DELIMITER, 2);
        String[] times = descriptionAndTimes[1].split(EVENT_TO_DELIMITER, 2);
        return new Event(descriptionAndTimes[0].trim(), times[0].trim(), times[1].trim());
    }

    /**
     * Stores a task and confirms it to the user.
     *
     * @param task Task to store.
     */
    private static void addTask(Task task) {
        tasks[taskCount] = task;
        taskCount++;
        printResponse("Got it. I've added this task:",
                "  " + task,
                "Now you have " + taskCount + " tasks in the list.");
    }

    /**
     * Marks the task at the given position as done or not done, and reports it.
     *
     * @param arguments Text following the command word, holding the task number
     *                  as shown by the "list" command (starting from 1).
     * @param isDone True to mark the task as done, false to mark it as not done.
     */
    private static void setTaskDoneStatus(String arguments, boolean isDone) {
        int taskIndex = Integer.parseInt(arguments) - 1;
        Task task = tasks[taskIndex];
        if (isDone) {
            task.markAsDone();
            printResponse("Nice! I've marked this task as done:", "  " + task);
        } else {
            task.markAsNotDone();
            printResponse("OK, I've marked this task as not done yet:", "  " + task);
        }
    }

    /**
     * Prints all stored tasks, numbered from 1.
     */
    private static void printTaskList() {
        String[] lines = new String[taskCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < taskCount; i++) {
            lines[i + 1] = (i + 1) + "." + tasks[i];
        }
        printResponse(lines);
    }

    /**
     * Prints the welcome message shown when Haro starts.
     */
    private static void printGreeting() {
        printResponse(BANNER, "Hello! I'm Haro.", "What can I do for you?");
    }

    /**
     * Prints the message shown just before Haro exits.
     */
    private static void printFarewell() {
        printResponse("Bye. Hope to see you again soon!");
    }

    /**
     * Prints the given lines framed by horizontal lines, which is the format
     * Haro uses for every response.
     *
     * @param lines Lines of the response, printed one per line.
     */
    private static void printResponse(String... lines) {
        System.out.println(HORIZONTAL_LINE);
        for (String line : lines) {
            System.out.println(line);
        }
        System.out.println(HORIZONTAL_LINE);
    }
}
