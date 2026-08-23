import java.util.Scanner;

public class Haro {
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

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            System.out.println(horizontalLine);
            if (input.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(horizontalLine);
                break;
            }
            System.out.println(input);
            System.out.println(horizontalLine);
        }
        scanner.close();
    }
}
