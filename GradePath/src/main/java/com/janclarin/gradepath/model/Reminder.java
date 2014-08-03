package com.janclarin.gradepath.model;

import android.content.Context;

import com.janclarin.gradepath.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
     * Returns the due date formatted like in the Dialog task fragment.
     *
     * @return
     */
    public String getDateString(Context context) {
        Calendar today = Calendar.getInstance();

        // Add one because it rounds down.
        long daysLeftBeforeDue = super.getDayDifference(reminderDate, today);

        String due;

        // Check if completed.
        if (isCompleted()) {
            due = context.getString(R.string.completed);
        } else {
            if (daysLeftBeforeDue < 0) {
                if (daysLeftBeforeDue == -1) {
                    due = context.getString(R.string.yesterday);
                } else {
                    due = Long.toString(Math.abs(daysLeftBeforeDue)) + " "
                            + context.getString(R.string.days_ago);
                }
            } else if (daysLeftBeforeDue == 0) {
                // Today.
                due = context.getString(R.string.task_due_date_today);
            } else if (daysLeftBeforeDue == 1) {
                // Tomorrow.
                due = context.getString(R.string.task_due_date_tomorrow);
            } else if (daysLeftBeforeDue < 7) {
                // This week.
                due = reminderDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            } else {
                due = new SimpleDateFormat("MMMM d").format(reminderDate.getTime());
            }
        }

        return due;
    }

    /**
     * Returns the time formatted.
     */
    public String getTimeString() {
        return TIME_FORMAT.format(reminderDate.getTime());
    }

    /**
     * Returns type as a string.
     */
    public String getTypeString(Context context) {
        return isExam ? context.getString(R.string.exam) : "";
    }

    /**
     * Compares the due dates of two tasks for sorting.
     * Incomplete tasks first.
     * Most recent tasks are at the beginning of the list.
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
