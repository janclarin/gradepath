package com.janclarin.gradepath.model;

import android.content.Context;

import com.janclarin.gradepath.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Task extends DatabaseItem implements Comparable<Task> {

    private long courseId;
    private String name;
    private boolean graded;
    private boolean completed;
    private Calendar addDate;
    private Calendar dueDate;

    public Task() {
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
        long daysLeftBeforeDue = super.getDayDifference(dueDate, today) + 1;

        String due = context.getString(R.string.due) + " ";

        // Late.
        if (daysLeftBeforeDue == -1) {
            due = context.getString(R.string.task_due_date_late);
        } else if (daysLeftBeforeDue == 0) {
            // Today.
            due += context.getString(R.string.task_due_date_today);
        } else if (daysLeftBeforeDue == 1) {
            // Tomorrow.
            due += context.getString(R.string.task_due_date_tomorrow);
        } else if (daysLeftBeforeDue <= 2) {
            // 2 days.
            due += context.getString(R.string.task_due_date_two_days);
        } else if (daysLeftBeforeDue < 7) {
            // This week.
            due += context.getString(R.string.task_due_date_on) + " "
                    + dueDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        } else {
            due += new SimpleDateFormat("E, MMM d").format(dueDate.getTime());
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

        long daysLeftBeforeDue = super.getDayDifference(dueDate, today) + 1;

        if (daysLeftBeforeDue < 3) {
            return R.color.course_urgency_1;
        } else if (daysLeftBeforeDue < 14) {
            return R.color.course_urgency_2;
        } else {
            return R.color.course_urgency_3;
        }
    }

    /**
     * Compares the due dates of two tasks for sorting.
     * Most recent tasks are at the beginning of the list.
     *
     * @param another
     * @return
     */
    @Override
    public int compareTo(Task another) {
        return this.dueDate.compareTo(another.dueDate);
    }
}
