package seedu.task.logic.commands;

import java.util.ArrayList;

import seedu.task.commons.core.UnmodifiableObservableList;
import seedu.task.logic.RollBackCommand;
import seedu.task.model.TaskManager;
import seedu.task.model.task.ReadOnlyTask;
import seedu.task.model.task.Task;

/**
 * Clears the task manager.
 */
public class ClearCommand extends Command {

	public static final String COMMAND_WORD = "clear";
	public static final String MESSAGE_SUCCESS = "Task manager has been cleared!";

	// @@author A0147335E-reused
	@Override
	public CommandResult execute(boolean isUndo) {
		assert model != null;
		UnmodifiableObservableList<ReadOnlyTask> lastShownList = model.getFilteredTaskList();
		for (int i = 0; i < lastShownList.size(); i++) {
			ReadOnlyTask taskToDelete = lastShownList.get(i);
			if (!isUndo) {
				getUndoList().add(new RollBackCommand(COMMAND_WORD, (Task) taskToDelete, null));
			}
		}
		model.resetData(TaskManager.getEmptyTaskManager());
		return new CommandResult(MESSAGE_SUCCESS);
	}

	
	private ArrayList<RollBackCommand> getUndoList() {
		return history.getUndoList();
	}
	//@@author
}
