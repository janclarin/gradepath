package com.janclarin.gradepath.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Reminder extends DatabaseItem implements Comparable<Reminder> {

    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a");

    private long courseId;
    private String name;
    private boolean isExam;
    private boolean completed;
    private Calendar addDate;
    private Calendar reminderDate;

    public Reminder() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExam() {
        return isExam;
    }

    public void setExam(boolean exam) {
        this.isExam = exam;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public Calendar getAddDate() {
        return addDate;
    }

    public void setAddDate(Calendar addDate) {
        this.addDate = addDate;
    }

    public Calendar getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(Calendar reminderDate) {
        this.reminderDate = reminderDate;
    }

    /**
     * Returns the time formatted.
     */
    public String getTimeString() {
        return TIME_FORMAT.format(reminderDate.getTime());
    }

    /**
     * Compares the due dates of two tasks for sorting.
     * Upcoming tasks first.
     *
     * @param another
     * @return
     */
    @Override
    public int compareTo(Reminder another) {
        if (this.isCompleted()) {
            if (another.isCompleted()) {
                // If both are complete, sort by due date. More recent first.
                return -this.reminderDate.compareTo(another.reminderDate);
            } else {
                return 1;
            }
        } else {
            if (another.isCompleted()) {
                return -1;
            } else {
                // If both are not completed sort by due date. More recent last.
                return this.reminderDate.compareTo(another.reminderDate);
            }
        }
    }
}
