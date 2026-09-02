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
     */
    private static void runCommandLoop() {
        Scanner scanner = new Scanner(System.in);
        boolean isExitRequested = false;
        while (!isExitRequested) {
            String input = scanner.nextLine().trim();
            isExitRequested = executeCommand(input);
        }
        scanner.close();
    }

    /**
     * Executes a single user command and prints its response.
     *
     * @param input Full line of input entered by the user.
     * @return True if the user asked to exit, false otherwise.
     */
    private static boolean executeCommand(String input) {
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
            addTask(new Todo(arguments));
            break;
        case COMMAND_DEADLINE:
            addTask(parseDeadline(arguments));
            break;
        case COMMAND_EVENT:
            addTask(parseEvent(arguments));
            break;
        default:
            printResponse("Sorry, I don't know what \"" + input + "\" means.");
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
     * Creates a deadline from arguments of the form "description /by date".
     *
     * @param arguments Text following the "deadline" command word.
     * @return The deadline described by the arguments.
     */
    private static Deadline parseDeadline(String arguments) {
        String[] parts = arguments.split(DEADLINE_BY_DELIMITER, 2);
        return new Deadline(parts[0].trim(), parts[1].trim());
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
