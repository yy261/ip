/**
 * Signals that a command could not be carried out because of something the
 * user did, such as a missing description or a task number that does not exist.
 *
 * The message carried by this exception is written for the user to read, so it
 * can be printed as Haro's response without any further wording.
 */
public class HaroException extends Exception {
    /**
     * Creates an exception carrying an explanation meant for the user.
     *
     * @param message Explanation of what was wrong and, where useful, an
     *                example of the expected command.
     */
    public HaroException(String message) {
        super(message);
    }
}
