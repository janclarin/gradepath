package com.janclarin.gradepath.model;

import android.content.Context;

import com.janclarin.gradepath.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Grade extends DatabaseItem implements Comparable<Grade> {

    private long courseId;
    private long componentId;
    private String name;
    private double pointsReceived;
    private double pointsPossible;
    private Calendar addDate;

    public Grade() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getComponentId() {
        return componentId;
    }

    public void setComponentId(long componentId) {
        this.componentId = componentId;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public double getPointsReceived() {
        return pointsReceived;
    }

    public void setPointsReceived(double pointsReceived) {
        this.pointsReceived = pointsReceived;
    }

    public double getPointsPossible() {
        return pointsPossible;
    }

    public void setPointsPossible(double pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    public Calendar getAddDate() {
        return addDate;
    }

    public void setAddDate(Calendar addDate) {
        this.addDate = addDate;
    }

    /**
     * Returns grade as percentage rounded to two decimal places.
     *
     * @return
     */
    public String getGradePercentage() {
        double grade = (pointsReceived / pointsPossible) * 100;
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(grade) + "%";
    }

    /**
     * Returns the add date as a String.
     */
    public String getAddDate(Context context) {
        Calendar today = Calendar.getInstance();

        final long daysApart = super.getDayDifference(today, addDate);

        String addDateString = context.getString(R.string.added) + " ";

        if (daysApart == 0) {
            addDateString += context.getString(R.string.today);
        } else if (daysApart == 1) {
            addDateString += context.getString(R.string.yesterday);
        } else {
            addDateString += context.getString(R.string.on) + " "
                    + new SimpleDateFormat("E, MMM d").format(addDate.getTime());
        }

        return addDateString;
    }

    /**
     * Comparison method for sorting grades with most recent grades at the beginning.
     *
     * @param another
     * @return
     */
    @Override
    public int compareTo(Grade another) {
        return -(this.addDate.compareTo(another.addDate));
    }

    /**
     * @return string representation of grade as a fraction.
     */
    @Override
    public String toString() {
        // Return grade to 2 decimal points.
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(pointsReceived) + "/" + df.format(pointsPossible);
    }
}
