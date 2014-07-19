package com.janclarin.gradepath.model;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Semester extends DatabaseItem implements Comparable<Semester> {

    public static final Season[] seasons = {Season.FALL, Season.WINTER, Season.SPRING, Season.SUMMER};
    private String name;
    private Season season;
    private int year;
    private double gpa;
    private boolean isCurrent;
    private Calendar endDate;

    public Semester() {
    }

    public String getSeason() {
        return season.toString();
    }

    /**
     * Sets season using String representation of the season.
     */
    public void setSeason(String seasonName) {

        // Assigns season object for sorting.
        if (seasonName.equals(seasons[0].toString())) {
            this.season = Season.FALL;
        } else if (seasonName.equals(seasons[1].toString())) {
            this.season = Season.WINTER;
        } else if (seasonName.equals(seasons[2].toString())) {
            this.season = Season.SPRING;
        } else if (seasonName.equals(seasons[3].toString())) {
            this.season = Season.SUMMER;
        }
    }

    public Season getSeasonEnum() {
        return season;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets the number of days left for the semester.
     *
     * @return number of days left if over 0 days. return 0 if negative number of days.
     */
    public String getDaysLeft() {
        long timeDiff = endDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        long dayDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

        return (dayDiff < 0) ? Long.toString(0) : Long.toString(dayDiff);
    }


    /**
     * Compares two semesters to figure out which is older.
     * Negated to reverse order to most recent first.
     *
     * @param another
     * @return
     */
    @Override
    public int compareTo(Semester another) {
        if (this.year > another.year) {
            return -1;
        } else if (this.year < another.year) {
            return 1;
        } else {
            // If years are the same, compare the seasons.
            return -this.season.compareTo(another.season);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Semester)) return false;
        Semester other = (Semester) object;
        return (this.id == other.id);
    }

    @Override
    public String toString() {
        return (season.toString() + " " + year);
    }

    /**
     * Seasons enumerated
     */
    public enum Season {
        WINTER("Winter"),
        SPRING("Spring"),
        SUMMER("Summer"),
        FALL("Fall");

        private String title;

        private Season(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

}
