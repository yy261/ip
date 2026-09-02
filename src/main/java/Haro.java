import java.util.Scanner;

/**
 * Entry point for the Haro chatbot.
 *
 * Greets the user, then repeatedly reads a line of input and adds it as a
 * task, lists the stored tasks, marks or unmarks a task as done, or exits
 * on "bye".
 */
public class Haro {
    /**
     * Runs Haro's greet-read-respond loop until the user types "bye".
     *
     * @param args Not used.
     */
    public static void main(String[] args) {
        String banner = "  _   _                 \n"
                + " | | | | __ _ _ __ ___  \n"
                + " | |_| |/ _` | '__/ _ \\ \n"
                + " |  _  | (_| | | | (_) |\n"
                + " |_| |_|\\__,_|_|  \\___/ \n";
        String horizontalLine = "____________________________________________________________";

        System.out.println(horizontalLine);
        System.out.println(banner);
        System.out.println("Hello! I'm Haro.");
        System.out.println("What can I do for you?");
        System.out.println(horizontalLine);

        Task[] tasks = new Task[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            System.out.println(horizontalLine);
            if (input.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(horizontalLine);
                break;
            } else if (input.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println((i + 1) + "." + tasks[i]);
                }
            } else if (input.startsWith("mark ")) {
                int taskIndex = Integer.parseInt(input.substring("mark ".length())) - 1;
                tasks[taskIndex].markAsDone();
                System.out.println("Nice! I've marked this task as done:");
                System.out.println("  " + tasks[taskIndex]);
            } else if (input.startsWith("unmark ")) {
                int taskIndex = Integer.parseInt(input.substring("unmark ".length())) - 1;
                tasks[taskIndex].markAsNotDone();
                System.out.println("OK, I've marked this task as not done yet:");
                System.out.println("  " + tasks[taskIndex]);
            } else if (input.startsWith("todo ")) {
                String description = input.substring("todo ".length()).trim();
                tasks[taskCount] = new Todo(description);
                taskCount++;
                printAddedTask(tasks[taskCount - 1], taskCount);
            } else if (input.startsWith("deadline ")) {
                String arguments = input.substring("deadline ".length());
                String[] parts = arguments.split(" /by ", 2);
                tasks[taskCount] = new Deadline(parts[0].trim(), parts[1].trim());
                taskCount++;
                printAddedTask(tasks[taskCount - 1], taskCount);
            } else if (input.startsWith("event ")) {
                String arguments = input.substring("event ".length());
                String[] descriptionAndTimes = arguments.split(" /from ", 2);
                String[] times = descriptionAndTimes[1].split(" /to ", 2);
                tasks[taskCount] = new Event(descriptionAndTimes[0].trim(),
                        times[0].trim(), times[1].trim());
                taskCount++;
                printAddedTask(tasks[taskCount - 1], taskCount);
            } else {
                System.out.println("Sorry, I don't know what that means.");
            }
            System.out.println(horizontalLine);
        }
        scanner.close();
    }
    /**
     * Prints the confirmation shown after a task is added to the list.
     *
     * @param task Task that was just added.
     * @param taskCount Number of tasks in the list after the addition.
     */
    private static void printAddedTask(Task task, int taskCount) {
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }
}
