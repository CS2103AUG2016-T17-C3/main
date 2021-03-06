package seedu.task.model;

import javafx.collections.transformation.FilteredList;
import seedu.task.commons.core.ComponentManager;
import seedu.task.commons.core.Config;
import seedu.task.commons.core.LogsCenter;
import seedu.task.commons.core.UnmodifiableObservableList;
import seedu.task.commons.events.model.TaskManagerChangedEvent;
import seedu.task.commons.exceptions.IllegalValueException;
import seedu.task.commons.util.ConfigUtil;
import seedu.task.commons.util.FileUtil;
import seedu.task.logic.parser.TimeParser;
import seedu.task.logic.parser.TimeParserResult;
import seedu.task.logic.parser.TimeParserResult.DateTimeStatus;
import seedu.task.model.task.Deadline;
import seedu.task.model.task.EndTime;
import seedu.task.model.task.ReadOnlyTask;
import seedu.task.model.task.StartTime;
import seedu.task.model.task.Status;
import seedu.task.model.task.Task;
import seedu.task.model.task.UniqueTaskList;
import seedu.task.model.task.UniqueTaskList.DuplicateTaskException;
import seedu.task.model.task.UniqueTaskList.TaskNotFoundException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents the in-memory model of the task manager data. All changes to any
 * model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final TaskManager taskManager;
    private final FilteredList<Task> filteredTasks;
    
    private final String currentSortPreference;

    /**
     * Initializes a ModelManager with the given TaskManager TaskManager and its
     * variables should not be null
     */
    public ModelManager(TaskManager src, UserPrefs userPrefs) {
        super();
        assert src != null;
        assert userPrefs != null;

        logger.fine("Initializing with task manager: " + src + " and user prefs " + userPrefs);

        taskManager = new TaskManager(src);
        filteredTasks = new FilteredList<>(taskManager.getTasks());
        
        Config config = new Config();
        File configFile = new File("config.json");
        try {
            config = FileUtil.deserializeObjectFromJsonFile(configFile, Config.class);
        } catch (IOException e) {
        }
        currentSortPreference = config.getsortPreference();
    }

    public ModelManager() {
        this(new TaskManager(), new UserPrefs());
    }

    public ModelManager(ReadOnlyTaskManager initialData) {
        taskManager = new TaskManager(initialData);
        filteredTasks = new FilteredList<>(taskManager.getTasks());
        
        Config config = new Config();
        File configFile = new File("config.json");
        try {
            config = FileUtil.deserializeObjectFromJsonFile(configFile, Config.class);
        } catch (IOException e) {
        }
        currentSortPreference = config.getsortPreference();
    }

    @Override
    public void resetData(ReadOnlyTaskManager newData) {
        taskManager.resetData(newData);
        indicateTaskManagerChanged();
    }

    @Override
    public ReadOnlyTaskManager getTaskManager() {
        return taskManager;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateTaskManagerChanged() {
        raise(new TaskManagerChangedEvent(taskManager));
    }

    @Override
    public synchronized void deleteTask(ReadOnlyTask target) throws TaskNotFoundException {
        taskManager.removeTask(target);
        indicateTaskManagerChanged();
    }

    // @@author A0147335E-reused
    @Override
    public synchronized void addTask(Task task) throws UniqueTaskList.DuplicateTaskException {
        Task newTask = task;
        if (!isDeadlineExist(task)) {
            String strDatewithTime = newTask.getDeadline().toString().replace(" ", "T");
            LocalDateTime taskDateTime = LocalDateTime.parse(strDatewithTime);

            Date currentDate = new Date();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());

            if (taskDateTime.isBefore(localDateTime)) {
                newTask = new Task(newTask.getName(), newTask.getStartTime(), newTask.getEndTime(),
                        newTask.getDeadline(), newTask.getTags(),
                        new Status(newTask.getStatus().getDoneStatus(), true, newTask.getStatus().getFavoriteStatus()),
                        newTask.getRecurring());
            } else {
                newTask = new Task(newTask.getName(), newTask.getStartTime(), newTask.getEndTime(),
                        newTask.getDeadline(), newTask.getTags(),
                        new Status(newTask.getStatus().getDoneStatus(), false, newTask.getStatus().getFavoriteStatus()),
                        newTask.getRecurring());
            }
        } else {
            newTask = new Task(newTask.getName(), newTask.getStartTime(), newTask.getEndTime(), newTask.getDeadline(),
                    newTask.getTags(),
                    new Status(newTask.getStatus().getDoneStatus(), false, newTask.getStatus().getFavoriteStatus()),
                    newTask.getRecurring());
        }
        taskManager.addTask(newTask);
        updateFilteredListToShowAll();
        indicateTaskManagerChanged();
    }

    @Override
    public synchronized void addTask(int index, Task task) throws UniqueTaskList.DuplicateTaskException {
        Task newTask = task;
        if (!isDeadlineExist(newTask)) {
            String strDatewithTime = newTask.getDeadline().toString().replace(" ", "T");
            LocalDateTime newTaskDateTime = LocalDateTime.parse(strDatewithTime);

            Date currentDate = new Date();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());

            if (newTaskDateTime.isBefore(localDateTime)) {
                newTask = new Task(newTask.getName(), newTask.getStartTime(), newTask.getEndTime(),
                        newTask.getDeadline(), newTask.getTags(),
                        new Status(newTask.getStatus().getDoneStatus(), true, newTask.getStatus().getFavoriteStatus()),
                        newTask.getRecurring());
            } else {
                newTask = new Task(newTask.getName(), newTask.getStartTime(), newTask.getEndTime(),
                        newTask.getDeadline(), newTask.getTags(),
                        new Status(newTask.getStatus().getDoneStatus(), false, newTask.getStatus().getFavoriteStatus()),
                        newTask.getRecurring());
            }

        } else {
            newTask = new Task(newTask.getName(), newTask.getStartTime(), newTask.getEndTime(), newTask.getDeadline(),
                    newTask.getTags(),
                    new Status(newTask.getStatus().getDoneStatus(), false, newTask.getStatus().getFavoriteStatus()),
                    newTask.getRecurring());
        }

        taskManager.addTask(index, newTask);
        indicateTaskManagerChanged();
    }

    private boolean isDeadlineExist(Task task) {
        return task.getDeadline().toString().isEmpty();
    }
    
    public String getCurrentSortPreference() {
        return currentSortPreference;
    }
    // @@author

    // @@author A0147944U
    /**
     * Repeats a task with time added to it based on the interval it is set to
     * 
     * @param recurringTask
     *            task to repeat
     */
    @Override
    public void repeatRecurringTask(Task recurringTask) {
        if (!"false".equals(recurringTask.getRecurring().toString())) {
            String newStartTime = recurringTask.getStartTime().toString();
            String newEndTime = recurringTask.getEndTime().toString();
            String newDeadline = recurringTask.getDeadline().toString();

            if (!"".equals(newStartTime)) {
                newStartTime = addPeriodicTimeToTask(recurringTask.getStartTime().toString(),
                        recurringTask.getRecurring().toString());
            }
            if (!"".equals(newEndTime)) {
                newEndTime = addPeriodicTimeToTask(recurringTask.getEndTime().toString(),
                        recurringTask.getRecurring().toString());
            }
            if (!"".equals(newDeadline)) {
                newDeadline = addPeriodicTimeToTask(recurringTask.getDeadline().toString(),
                        recurringTask.getRecurring().toString());
            }

            try {
                Task newTask = new Task(recurringTask.getName(), new StartTime(newStartTime), new EndTime(newEndTime),
                        new Deadline(newDeadline), recurringTask.getTags(),
                        new Status(false, false, recurringTask.getStatus().getFavoriteStatus()),
                        recurringTask.getRecurring());
                taskManager.addTask(newTask);
            } catch (DuplicateTaskException e) {
                logger.info("Next iteration of this recurring task already exists");
            } catch (IllegalValueException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds time to the original time based on the interval
     * 
     * @param originalTime
     *            original time
     * @param interval
     *            duration to add
     * @return date and time with interval added
     */
    private String addPeriodicTimeToTask(String originalTime, String interval) {
        String newTime = "one week after " + originalTime;
        if ("daily".equals(interval)) {
            newTime = "one day after " + originalTime;
        } else if ("weekly".equals(interval)) {
            newTime = "one week after " + originalTime;
        } else if ("fortnightly".equals(interval)) {
            newTime = "two weeks after " + originalTime;
        } else if ("monthly".equals(interval)) {
            newTime = "one month after " + originalTime;
        } else if ("yearly".equals(interval)) {
            newTime = "one year after " + originalTime;
        }
        TimeParser parserTime = new TimeParser();
        TimeParserResult time = parserTime.parseTime(newTime);
        StringBuilder newTimeString = new StringBuilder();
        if (time.getRawDateTimeStatus() == DateTimeStatus.START_DATE_START_TIME) {
            newTimeString.append(time.getFirstDate().toString());
            newTimeString.append(" ");
            newTimeString.append(time.getFirstTime().toString().substring(0, 5));
        }
        return newTimeString.toString();
    }
    // @@author

    // =========== Filtered Task List Accessors
    // ===============================================================

    @Override
    public UnmodifiableObservableList<ReadOnlyTask> getFilteredTaskList() {
        return new UnmodifiableObservableList<>(filteredTasks);
    }

    @Override
    public void updateFilteredListToShowAll() {
        filteredTasks.setPredicate(null);
    }

    @Override
    public void updateFilteredTaskList(Set<String> keywords) {
        updateFilteredTaskList(new PredicateExpression(new NameQualifier(keywords)));
    }

    private void updateFilteredTaskList(Expression expression) {
        filteredTasks.setPredicate(expression::satisfies);
    }

    // @@author A0147944U
    /**
     * Select sorting method based on keyword
     * 
     * @param keyword
     *            keyword to sort tasks by
     */
    public void sortFilteredTaskList(String keyword) {
        switch (keyword) {
        case "Deadline":
            taskManager.sortByDeadline();
            break;
        case "Start Time":
            taskManager.sortByStartTime();
            break;
        case "End Time":
            taskManager.sortByEndTime();
            break;
        case "Completed":
            taskManager.sortByDoneStatus();
            break;
        case "Favorite":
            taskManager.sortByFavoriteStatus();
            break;
        case "Overdue":
            taskManager.sortByOverdueStatus();
            break;
        case "Name":
            taskManager.sortByName();
            break;
        default:
            taskManager.sortByDefaultRules();
            break;
        }
        // Save data in that order
        indicateTaskManagerChanged();
    }

    /**
     * Updates sorting method in config based on keyword
     * 
     * @param keyword
     *            keyword to sort tasks by
     */
    @Override
    public void saveCurrentSortPreference(String keyword) {
        Config config = new Config();
        File configFile = new File("config.json");
        try {
            config = FileUtil.deserializeObjectFromJsonFile(configFile, Config.class);
        } catch (IOException e) {
            logger.warning("Error reading from config file " + "config.json" + " : " + e);
        }
        config.setsortPreference(keyword);
        try {
            ConfigUtil.saveConfig(config, "config.json");
        } catch (IOException e) {
            logger.warning("Error saving to config file " + "config.json" + " : " + e);
            e.printStackTrace();
        }
    }

    /**
     * Automatically sorts tasks based on current sort preferences in config
     */
    public void autoSortBasedOnCurrentSortPreference() {
        Config config = new Config();
        File configFile = new File("config.json");
        try {
            config = FileUtil.deserializeObjectFromJsonFile(configFile, Config.class);
        } catch (IOException e) {
            logger.warning("Error reading from config file " + "config.json" + " : " + e);
        }
        String currentSortPreference = config.getsortPreference();
        if (!"None".equals(currentSortPreference)) {
            sortFilteredTaskList(currentSortPreference);
        }
    }
    // @@author

    // ========== Inner classes/interfaces used for filtering
    // ==================================================

    interface Expression {
        boolean satisfies(ReadOnlyTask task);

        String toString();
    }

    private class PredicateExpression implements Expression {

        private final Qualifier qualifier;

        PredicateExpression(Qualifier qualifier) {
            this.qualifier = qualifier;
        }

        @Override
        public boolean satisfies(ReadOnlyTask task) {
            return qualifier.run(task);
        }

        @Override
        public String toString() {
            return qualifier.toString();
        }
    }

    interface Qualifier {
        boolean run(ReadOnlyTask task);

        String toString();
    }

    private class NameQualifier implements Qualifier {
        private Set<String> nameKeyWords;

        NameQualifier(Set<String> nameKeyWords) {
            this.nameKeyWords = nameKeyWords;
        }

        @Override
        public boolean run(ReadOnlyTask task) {

            String name = task.getAsText().toLowerCase();

            return nameKeyWords.stream().filter(keyword -> name.indexOf(keyword.toLowerCase()) >= 0).findAny()
                    .isPresent();
        }

        @Override
        public String toString() {
            return "name=" + String.join(", ", nameKeyWords);
        }
    }
    
    
}
