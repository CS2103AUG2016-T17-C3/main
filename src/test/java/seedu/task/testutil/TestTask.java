package seedu.task.testutil;

import seedu.task.model.tag.UniqueTagList;
import seedu.task.model.task.*;

/**
 * A mutable task object. For testing only.
 */
public class TestTask implements ReadOnlyTask {

    private Name name;
    private Deadline location;
    private EndTime endTime;
    private StartTime startTime;
    private UniqueTagList tags;
    private Status status;
    private Recurring recurring;

    public TestTask() {
        tags = new UniqueTagList();
    }

    public void setName(Name name) {
        this.name = name;
    }

    public void setTask(Deadline location) {
        this.location = location;
    }

    public void setEndTime(EndTime endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(StartTime startTime) {
        this.startTime = startTime;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public void setRecurring(Recurring recurring) {
        this.recurring = recurring;
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public StartTime getStartTime() {
        return startTime;
    }

    @Override
    public EndTime getEndTime() {
        return endTime;
    }

    @Override
    public Deadline getDeadline() {
        return location;
    }

    @Override
    public UniqueTagList getTags() {
        return tags;
    }
    
    @Override
    public Status getStatus() {
        return status;
    }
    
    @Override
    public Recurring getRecurring() {
        return recurring;
    }

    @Override
    public String toString() {
        return getAsText();
    }

    // @@author A0147944U
    public String getAddCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append("add " + this.getName().fullName + "");
        if (!this.getStartTime().value.isEmpty()) {
            sb.append(", from " + this.getStartTime().value);
        }
        if (!this.getEndTime().value.isEmpty()) {
            sb.append(" to " + this.getEndTime().value + "");
        }
        if (!getDeadline().toString().isEmpty()) {       
            sb.append(" \nDeadline: ").append(getDeadline());
        }
        if (!this.getDeadline().value.isEmpty()) {
            sb.append(" by " + this.getDeadline().value + " ");
        }
        if (!getRecurring().toString().equals("false")) {
            sb.append(" \nRecurring: ").append(getDeadline());
        }
        this.getTags().getInternalList().stream().forEach(s -> sb.append("#" + s.tagName + " "));
        return sb.toString();
    }
    // @@author
   
}
