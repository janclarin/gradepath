package com.janclarin.gradepath.model;

import android.content.Context;

import com.janclarin.gradepath.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Reminder extends DatabaseItem implements Comparable<Reminder> {

    private long courseId;
    private String name;
    private boolean graded;
    private boolean completed;
    private Calendar addDate;
    private Calendar dueDate;

    public Reminder() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGraded() {
        return graded;
    }

    public void setGraded(boolean graded) {
        this.graded = graded;
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

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Returns the due date formatted like in the Dialog task fragment.
     *
     * @return
     */
    public String getDueDate(Context context) {
        Calendar today = Calendar.getInstance();

        // Add one because it rounds down.
        long daysLeftBeforeDue = super.getDayDifference(dueDate, today);

        String due;

        // Check if completed.
        if (isCompleted()) {
            due = context.getString(R.string.completed);
        } else {
            due = context.getString(R.string.due) + " ";

            if (daysLeftBeforeDue < 0) {
                if (daysLeftBeforeDue == -1) {
                    due += context.getString(R.string.yesterday);
                } else {
                    due += Long.toString(Math.abs(daysLeftBeforeDue)) + " "
                            + context.getString(R.string.days_ago);
                }
            } else if (daysLeftBeforeDue == 0) {
                // Today.
                due += context.getString(R.string.task_due_date_today);
            } else if (daysLeftBeforeDue == 1) {
                // Tomorrow.
                due += context.getString(R.string.task_due_date_tomorrow);
            } else if (daysLeftBeforeDue <= 3) {
                // 2 days.
                due += context.getString(R.string.task_due_date_two_days);
            } else if (daysLeftBeforeDue < 7) {
                // This week.
                due += context.getString(R.string.task_due_date_on) + " "
                        + dueDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            } else {
                due += new SimpleDateFormat("MMMM d").format(dueDate.getTime());
            }
        }

        return due;
    }

    /**
     * Returns the color to display beside a course based on its due date urgency
     *
     * @return
     */
    public int getUrgencyColor(Context context) {
        Calendar today = Calendar.getInstance();

        long daysLeftBeforeDue = super.getDayDifference(dueDate, today);

        if (isCompleted() || daysLeftBeforeDue > 14) {
            return R.color.course_urgency_0;
        } else if (daysLeftBeforeDue < 2) {
            return R.color.course_urgency_2;
        } else {
            return R.color.course_urgency_1;
        }
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
                return -this.dueDate.compareTo(another.dueDate);
            } else {
                return 1;
            }
        } else {
            if (another.isCompleted()) {
                return -1;
            } else {
                // If both are not completed sort by due date. More recent last.
                return this.dueDate.compareTo(another.dueDate);
            }
        }
    }
}
