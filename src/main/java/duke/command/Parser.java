package duke.command;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import duke.Duke;
import duke.command.TaskList;
import duke.command.Ui;
import duke.command.Storage;
import duke.task.Task;
import duke.task.Todo;
import duke.task.Deadline;
import duke.task.Event;
import duke.DukeException;

/**
 * Represents a Command manager that adds new Task to TaskList, deletes
 * tasks and printing the list of tasks.
 * Contains the fields TaskList that contains the list of Task,
 * Ui that prints the message for each commands and Storage that
 * updates the files when the list of Task is modified.
 */
public class Parser {

    private TaskList taskList;
    private Ui ui;
    private Storage storage;

    /**
     * Constructs parser object that takes in TaskList,
     * Ui and Storage as parameter.
     *
     * @param taskList  list of tasks contained in TaskList object.
     * @param ui User Interface object responsible for printing messages.
     * @param storage Storage that loads and updates files when tasks are
     *                deleted or added to the list.
     */
    public Parser(TaskList taskList, Ui ui, Storage storage) {
        this.taskList = taskList;
        this.ui = ui;
        this.storage = storage;
    }

    /**
     * Parse a one line input into a Task or Command.
     * Print message is produced every time an input is passed in.
     *
     * @param input  Command to be executed or Task to be created.
     * @throws DukeException  if Task has incomplete description.
     */
    public String parseLineInput(String input) throws DukeException, IOException, ParseException {
        String firstWord = input.split(" ")[0];
        if (firstWord.equals("bye")) {
            return new String("Bye. Hope to see you again soon!");
        } else if (firstWord.equals("list")) {
            return ui.printList(this.taskList.getList());
        } else if (firstWord.equals("done")) {
            String message = processTaskDone(input, this.taskList, this.storage);
            return message;
        } else if (firstWord.equals("todo")) {
            String message = processTaskTodo(input, this.taskList, this.storage);
            return message;
        } else if (firstWord.equals("deadline")) {
            String message = processTaskDeadline(input, this.taskList, this.storage);
            return message;
        } else if (firstWord.equals("event")) {
            String message = processTaskEvent(input, this.taskList, this.storage);
            return message;
        } else if (firstWord.equals("delete")) {
            String message = processDeleteTask(input, this.taskList, this.storage);
            return message;
        } else if (firstWord.equals("bye")) {
            ui.printByeMessage();
        } else if (firstWord.equals("find")) {
            String message = processFind(input, this.taskList, this.storage);
            return message;
        } else {
            throw new DukeException("OOPS!!! I'm sorry, but i don't know what that means :-(");
        }
        return "";
    }

    public String processTaskDone(String input, TaskList taskList, Storage storage) throws IOException {
        Integer taskNum = Integer.valueOf(input.substring(5));
        Task currTask = taskList.getTask(taskNum - 1);
        currTask.markAsDone();
        storage.updateFile();
        return ui.printTaskDone(currTask);
    }

    public String processTaskTodo(String input, TaskList taskList, Storage storage) throws DukeException, IOException {
        if (input.length() == 4) {
            throw new DukeException("OOPS!!! The description of a todo cannot be empty.");
        } else {
            Task toDoTask = new Todo(input.substring(5));
            taskList.addTask(toDoTask);
            storage.appendToFile(toDoTask);
            return ui.printToDoTask(toDoTask, this.taskList.getList());
        }
    }

    public String processTaskDeadline(String input, TaskList taskList, Storage storage) throws DukeException, ParseException, IOException {
        if (input.length() == 8) {
            throw new DukeException("OOPS!!! The description of a deadline cannot be empty.");
        } else {
            String time = input.split("/by", 2)[1];
            String description = input.split(" /by", 2)[0];
            Task deadlineTask = new Deadline(description, time);
            taskList.addTask(deadlineTask);
            storage.appendToFile(deadlineTask);
            return ui.printDeadlineTask(deadlineTask, this.taskList.getList());
        }
    }

    public String processTaskEvent(String input, TaskList taskList, Storage storage) throws DukeException, ParseException, IOException {
        if (input.length() == 5) {
            throw new DukeException("OOPS!!! The description of an event cannot be empty");
        } else {
            String time = input.split("/at", 2)[1];
            String description = input.split(" /at", 2)[0];
            Task eventTask = new Event(description, time);
            taskList.addTask(eventTask);
            storage.appendToFile(eventTask);
            return ui.printEventtTask(eventTask, this.taskList.getList());
        }
    }

    public String processDeleteTask(String input, TaskList taskList, Storage storage) throws IOException {
        Integer index = Integer.valueOf(input.substring(7));
        Task deletedTask = taskList.getTask(index - 1);
        int sizeBeforeDeletion = taskList.getList().size();
        taskList.deleteTask((int) index - 1);
        int sizeAfterDeletion = taskList.getList().size();
        storage.updateFile();
        int diffInSize = sizeBeforeDeletion - sizeAfterDeletion;
        assert diffInSize == 1: "Task not deleted";
        return ui.printDeleteTask(deletedTask, this.taskList.getList());
    }

    public String processFind(String input, TaskList taskList, Storage storage) throws DukeException {
        String item = "(.*)" + input.split(" ", 2)[1] + "(.*)";
        ArrayList<Task> list = taskList.getList();
        String message = "";
        for (int i = 0; i < list.size(); i++) {
            String description = list.get(i).getDescription();
            boolean isPresent = description.matches(item);
            if (isPresent) {
                message = message + list.get(i).toString() + "\n";
            }
        }
        if (message.equals("")) {
            throw new DukeException ("Sorry we cannot find the task :(");
        }
        return message;
    }
}