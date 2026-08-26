/**
 * Represents a single task with a description and a done/not-done status.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates a new, not-done task with the given description.
     *
     * @param description Text describing the task.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the single-character icon representing this task's done status.
     *
     * @return {@code "X"} if the task is done, or a single space otherwise.
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
