package com.janclarin.gradepath.model;

public class Course extends DatabaseItem implements Comparable<Course> {

    private long semesterId;
    private String name;
    private String instructorName;
    private String instructorEmail;
    private int finalGradeValue;
    private boolean completed;

    public Course() {
    }

    public long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(long semesterId) {
        this.semesterId = semesterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getInstructorEmail() {
        return instructorEmail;
    }

    public void setInstructorEmail(String instructorEmail) {
        this.instructorEmail = instructorEmail;
    }

    public int getFinalGradeValue() {
        return finalGradeValue;
    }

    public void setFinalGradeValue(int finalGradeValue) {
        this.finalGradeValue = finalGradeValue;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Course another) {
        return this.name.compareTo(another.name);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Course)) return false;
        Course other = (Course) object;

        // Checks if IDs are the same.
        return (this.id == other.id);
    }

    /**
     * Enumeration of letter grades with corresponding string representations.
     */
    public static enum LetterGrade {
        F("F", 0),
        D_MINUS("D-", 0.7), D("D", 1.0), D_PLUS("D+", 1.3),
        C_MINUS("C-", 1.7), C("C", 2.0), C_PLUS("C+", 2.3),
        B_MINUS("B-", 2.7), B("B", 3.0), B_PLUS("B+", 3.3),
        A_MINUS("A-", 3.7), A("A", 4.0), A_PLUS("A+", 4.0);

        private final String grade;
        private final double gpaEquivalent;

        private LetterGrade(String g, double gpa) {
            grade = g;
            gpaEquivalent = gpa;
        }

        public double getGpaEquivalent() {
            return gpaEquivalent;
        }

        @Override
        public String toString() {
            return grade;
        }
    }
}
