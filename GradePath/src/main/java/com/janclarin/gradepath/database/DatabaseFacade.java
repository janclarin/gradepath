package com.janclarin.gradepath.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.GradeComponent;
import com.janclarin.gradepath.model.Reminder;
import com.janclarin.gradepath.model.Semester;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Provides methods to retrieve information from mDatabase.
 */
public class DatabaseFacade {

    private static final String[] SEMESTER_COLUMNS = {DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_SEASON, DatabaseHelper.COLUMN_YEAR,
            DatabaseHelper.COLUMN_SEMESTER_GPA, DatabaseHelper.COLUMN_IS_CURRENT,
            DatabaseHelper.COLUMN_DATE_END};

    private static final String[] COURSE_COLUMNS = {DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_SEMESTER_ID, DatabaseHelper.COLUMN_COURSE_NAME,
            DatabaseHelper.COLUMN_INSTRUCTOR_NAME, DatabaseHelper.COLUMN_INSTRUCTOR_EMAIL,
            DatabaseHelper.COLUMN_FINAL_GRADE, DatabaseHelper.COLUMN_IS_COMPLETED};

    private static final String[] COMPONENT_COLUMNS = {DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_COURSE_ID, DatabaseHelper.COLUMN_COMPONENT_NAME,
            DatabaseHelper.COLUMN_COMPONENT_WEIGHT, DatabaseHelper.COLUMN_COMPONENT_NUM_ITEMS};

    private static final String[] GRADE_COLUMNS = {DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_COURSE_ID, DatabaseHelper.COLUMN_COMPONENT_ID,
            DatabaseHelper.COLUMN_GRADE_NAME, DatabaseHelper.COLUMN_POINTS_RECEIVED,
            DatabaseHelper.COLUMN_POINTS_POSSIBLE, DatabaseHelper.COLUMN_DATE_ADDED};

    private static final String[] REMINDER_COLUMNS = {DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_COURSE_ID, DatabaseHelper.COLUMN_REMINDER_NAME,
            DatabaseHelper.COLUMN_IS_EXAM, DatabaseHelper.COLUMN_IS_COMPLETED,
            DatabaseHelper.COLUMN_DATE_ADDED, DatabaseHelper.COLUMN_DATE_REMIND};

    /**
     * Instance of the mDatabase following the singleton pattern.
     */
    private static final DatabaseFacade INSTANCE = new DatabaseFacade();
    private static DatabaseHelper sDatabaseHelper;
    private SQLiteDatabase mDatabase;

    // Private constructor for singleton implementation.
    private DatabaseFacade() {
    }

    public static DatabaseFacade getInstance(Context context) {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = DatabaseHelper.getInstance(context);
        }
        return INSTANCE;
    }

    public void open() throws SQLException {
        mDatabase = sDatabaseHelper.getWritableDatabase();
    }

    public void close() {
        sDatabaseHelper.close();
    }

    /**
     * Inserts a new semester into the mDatabase.
     *
     * @return semester ID
     */
    public Semester insertSemester(String season, int year, double gpa, boolean isCurrent,
                                   Calendar endDate) {

        // Cursor to check if this semester exists before inserting it.
        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_SEMESTERS, SEMESTER_COLUMNS,
                DatabaseHelper.COLUMN_SEASON + " = '" + season + "' AND " +
                        DatabaseHelper.COLUMN_YEAR + " = '" + year + "'", null, null, null, null
        );

        // Set all other semesters as not current if this semester is.
        if (isCurrent) setOtherSemestersToNotCurrent();

        // If there are no semesters with this season and year, insert it into mDatabase.
        // Otherwise, get the semester from the cursor.
        if (!cursor.moveToFirst()) {

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_SEASON, season);
            values.put(DatabaseHelper.COLUMN_YEAR, year);
            values.put(DatabaseHelper.COLUMN_SEMESTER_GPA, gpa);
            values.put(DatabaseHelper.COLUMN_IS_CURRENT, isCurrent ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_DATE_END, endDate.getTimeInMillis());

            long semesterId = mDatabase.insert(DatabaseHelper.TABLE_SEMESTERS, null, values);

            cursor = mDatabase.query(DatabaseHelper.TABLE_SEMESTERS, SEMESTER_COLUMNS,
                    DatabaseHelper.COLUMN_ID + " = '" + semesterId + "'", null, null, null, null);

            // Set cursor to first row.
            cursor.moveToFirst();
        }

        // Get semester object from cursor.
        Semester semester = cursorToSemester(cursor);

        // Close cursor.
        cursor.close();

        return semester;
    }

    /**
     * Updates a semester with new values using semester fields.
     *
     * @return -1 if update failed.
     */
    public int updateSemester(long semesterId, String season, int year, double gpa,
                              boolean isCurrent, Calendar endDate) {

        // Set all other semesters as not current if this semester is.
        if (isCurrent) setOtherSemestersToNotCurrent();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SEASON, season);
        values.put(DatabaseHelper.COLUMN_YEAR, year);
        values.put(DatabaseHelper.COLUMN_SEMESTER_GPA, gpa);
        values.put(DatabaseHelper.COLUMN_IS_CURRENT, isCurrent ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_DATE_END, endDate.getTimeInMillis());

        return mDatabase.update(DatabaseHelper.TABLE_SEMESTERS, values,
                DatabaseHelper.COLUMN_ID + " = '" + semesterId + "'", null);
    }

    /**
     * Deletes a semester using its semester object.
     */
    public void deleteSemester(Semester semester) {
        long semesterId = semester.getId();

        // Deletes semester from mDatabase.
        mDatabase.delete(DatabaseHelper.TABLE_SEMESTERS,
                DatabaseHelper.COLUMN_ID + " = '" + semesterId + "'", null);

        // Get cursor pointing to all list_course in this semester.
        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_COURSES,
                new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_SEMESTER_ID},
                DatabaseHelper.COLUMN_SEMESTER_ID + " = '" + semesterId + "'", null, null, null, null);

        cursor.moveToFirst();

        // Delete all list_course related to semester.
        while (!cursor.isAfterLast()) {
            deleteCourse(cursor.getLong(0));
            cursor.moveToNext();
        }

        cursor.close();
    }

    /**
     * Sets this semester as the current one and all others to not be current.
     */
    public int setOtherSemestersToNotCurrent() {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_CURRENT, 0);

        return mDatabase.update(DatabaseHelper.TABLE_SEMESTERS, values, null, null);
    }

    /**
     * @return {@code Semester} object from database.
     */
    public Semester getSemester(long semesterId) {

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_SEMESTERS, SEMESTER_COLUMNS,
                DatabaseHelper.COLUMN_ID + " = '" + semesterId + "'", null, null, null, null);

        cursor.moveToFirst();

        Semester semester = cursorToSemester(cursor);

        cursor.close();

        return semester;
    }

    /**
     * @return Current {@code Semester} if it exists, otherwise return null.
     */
    public Semester getCurrentSemester() {
        Semester semester;

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_SEMESTERS, SEMESTER_COLUMNS,
                DatabaseHelper.COLUMN_IS_CURRENT + " = '1'", null, null, null, null);

        if (cursor.moveToFirst()) {
            semester = cursorToSemester(cursor);
            cursor.close();
            return semester;
        } else {
            cursor.close();
            return null;
        }
    }

    /**
     * @return list of all {@code Semester}, sorted by most recent first.
     */
    public List<Semester> getSemesters() {
        List<Semester> semesters = new ArrayList<Semester>();

        // Cursor of all semesters.
        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_SEMESTERS, SEMESTER_COLUMNS,
                null, null, null, null, null);

        cursor.moveToFirst();

        // Read distinct semesters from mDatabase into a list.
        while (!cursor.isAfterLast()) {
            semesters.add(cursorToSemester(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        // Sort semesters using custom comparator within class.
        Collections.sort(semesters);

        return semesters;
    }

    /**
     * Gets a list of all completed semesters.
     */
    public List<Semester> getPastSemestersWithGPA() {
        List<Semester> semesters = new ArrayList<Semester>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_SEMESTERS, SEMESTER_COLUMNS,
                DatabaseHelper.COLUMN_IS_CURRENT + " = '0' AND "
                        + DatabaseHelper.COLUMN_SEMESTER_GPA + " > '-1'", null, null, null, null);

        cursor.moveToFirst();

        // Read distinct semesters from mDatabase into a list.
        while (!cursor.isAfterLast()) {
            semesters.add(cursorToSemester(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return semesters;
    }

    /**
     * @return boolean indicating if there are no semesters.
     */
    public boolean noSemesters() {

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_SEMESTERS,
                new String[]{DatabaseHelper.COLUMN_ID}, null, null, null, null, null);

        // Move cursor to first course.
        cursor.moveToFirst();

        // If it is already past the end, then there was no course to begin with.
        if (cursor.isAfterLast()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    /**
     * Inserts new course into mDatabase.
     *
     * @return course id.
     */
    public long insertCourse(long semesterId, String courseName, String instructorName,
                             String instructorEmail, int letterGradeValue, boolean isCompleted) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SEMESTER_ID, semesterId);
        values.put(DatabaseHelper.COLUMN_COURSE_NAME, courseName);
        values.put(DatabaseHelper.COLUMN_INSTRUCTOR_NAME, instructorName);
        values.put(DatabaseHelper.COLUMN_INSTRUCTOR_EMAIL, instructorEmail);
        values.put(DatabaseHelper.COLUMN_FINAL_GRADE, letterGradeValue);
        values.put(DatabaseHelper.COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);
        // Booleans not supported in SQLite, 1 = true, 0 = false.

        // Inserts course into table and returns its id.
        return mDatabase.insert(DatabaseHelper.TABLE_COURSES, null, values);
    }

    /**
     * Updates a course using its fields.
     *
     * @return -1 if update failed.
     */
    public int updateCourse(long courseId, long semesterId, String courseName, String instructorName,
                            String instructorEmail, int letterGradeValue, boolean isCompleted) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SEMESTER_ID, semesterId);
        values.put(DatabaseHelper.COLUMN_COURSE_NAME, courseName);
        values.put(DatabaseHelper.COLUMN_INSTRUCTOR_NAME, instructorName);
        values.put(DatabaseHelper.COLUMN_INSTRUCTOR_EMAIL, instructorEmail);
        values.put(DatabaseHelper.COLUMN_FINAL_GRADE, letterGradeValue);
        values.put(DatabaseHelper.COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);

        return mDatabase.update(DatabaseHelper.TABLE_COURSES, values,
                DatabaseHelper.COLUMN_ID + " = '" + courseId + "'", null);
    }

    /**
     * Deletes a course and all related information using its id.
     */
    public void deleteCourse(long courseId) {
        mDatabase.delete(DatabaseHelper.TABLE_COURSES,
                DatabaseHelper.COLUMN_ID + " = '" + courseId + "'", null);
        mDatabase.delete(DatabaseHelper.TABLE_COMPONENTS,
                DatabaseHelper.COLUMN_COURSE_ID + " = '" + courseId + "'", null);
        mDatabase.delete(DatabaseHelper.TABLE_GRADES,
                DatabaseHelper.COLUMN_COURSE_ID + " = '" + courseId + "'", null);
    }

    /**
     * Get a course from {@code Course} id.
     */
    public Course getCourse(long courseId) {

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_COURSES, COURSE_COLUMNS,
                DatabaseHelper.COLUMN_ID + " ='" + courseId + "'", null, null, null, null);

        Course course;
        if (cursor.moveToFirst()) {
            course = cursorToCourse(cursor);
        } else {
            course = null;
        }

        cursor.close();
        return course;
    }

    /**
     * @return list of all {@code Course}.
     */
    public List<Course> getCourses() {

        List<Course> courses = new ArrayList<Course>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_COURSES, COURSE_COLUMNS,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            courses.add(cursorToCourse(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return courses;
    }

    /**
     * @return list of all {@code Course} for a {@code Semester}.
     */
    public List<Course> getCourses(long semesterId) {
        List<Course> courses = new ArrayList<Course>();

        Cursor cursorCourse = mDatabase.query(DatabaseHelper.TABLE_COURSES, COURSE_COLUMNS,
                DatabaseHelper.COLUMN_SEMESTER_ID + " = '" + semesterId + "'", null, null, null, null);

        cursorCourse.moveToFirst();

        // Read all list_course from mDatabase into a list.
        while (!cursorCourse.isAfterLast()) {
            courses.add(cursorToCourse(cursorCourse));
            cursorCourse.moveToNext();
        }

        cursorCourse.close();
        return courses;
    }

    /**
     * @return the list of {@code Course} for a semester.
     */
    public List<Course> getCurrentCourses() {
        Semester currentSemester = getCurrentSemester();

        if (currentSemester != null) {
            return getCourses(currentSemester.getId());
        } else {
            return new ArrayList<Course>();
        }
    }

    /**
     * Inserts a new grade component into the mDatabase.
     *
     * @return grade component ID.
     */
    public long insertGradeComponent(long courseId, String name,
                                     double weight, int numberOfItems) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_COURSE_ID, courseId);
        values.put(DatabaseHelper.COLUMN_COMPONENT_NAME, name);
        values.put(DatabaseHelper.COLUMN_COMPONENT_WEIGHT, weight);
        values.put(DatabaseHelper.COLUMN_COMPONENT_NUM_ITEMS, numberOfItems);

        // Return grade component id.
        return mDatabase.insert(DatabaseHelper.TABLE_COMPONENTS, null, values);
    }

    /**
     * Updates a grade component using its fields.
     *
     * @return -1 if update failed.
     */
    public int updateGradeComponent(long componentId, long courseId, String name,
                                    double weight, int numberOfItems) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_COURSE_ID, courseId);
        values.put(DatabaseHelper.COLUMN_COMPONENT_NAME, name);
        values.put(DatabaseHelper.COLUMN_COMPONENT_WEIGHT, weight);
        values.put(DatabaseHelper.COLUMN_COMPONENT_NUM_ITEMS, numberOfItems);

        return mDatabase.update(DatabaseHelper.TABLE_COMPONENTS, values,
                DatabaseHelper.COLUMN_ID + " = '" + componentId + "'", null);
    }

    /**
     * Deletes a grade component.
     */
    public void deleteGradeComponent(GradeComponent gradeComponent) {

        long componentId = gradeComponent.getId();

        // Delete grade component.
        mDatabase.delete(DatabaseHelper.TABLE_COMPONENTS,
                DatabaseHelper.COLUMN_ID + " = '" + componentId + "'", null);

        // Delete all grades for component.
        mDatabase.delete(DatabaseHelper.TABLE_GRADES,
                DatabaseHelper.COLUMN_COMPONENT_ID + " = '" + componentId + "'", null);
    }

    /**
     * Get a course from {@code Course} id.
     */
    public GradeComponent getGradeComponent(long componentId) {

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_COMPONENTS, COMPONENT_COLUMNS,
                DatabaseHelper.COLUMN_ID + " ='" + componentId + "'", null, null, null, null);

        GradeComponent gradeComponent;

        cursor.moveToFirst();
        gradeComponent = cursorToGradeComponent(cursor);
        cursor.close();

        return gradeComponent;
    }

    /**
     * @return list of all GradeComponent for a course.
     */
    public List<GradeComponent> getGradeComponents(long courseId) {

        List<GradeComponent> gradeComponents = new ArrayList<GradeComponent>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_COMPONENTS, COMPONENT_COLUMNS,
                DatabaseHelper.COLUMN_COURSE_ID + " = '" + courseId + "'", null, null, null, null);

        cursor.moveToFirst();

        // Read all grade components for a course into a list.
        while (!cursor.isAfterLast()) {
            gradeComponents.add(cursorToGradeComponent(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return gradeComponents;
    }

    /**
     * Inserts a new grade into the mDatabase.
     *
     * @return Grade ID.
     */
    public long insertGrade(long courseId, long componentId, String name,
                            double pointsEarned, double pointsPossible) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_COURSE_ID, courseId);
        values.put(DatabaseHelper.COLUMN_COMPONENT_ID, componentId);
        values.put(DatabaseHelper.COLUMN_GRADE_NAME, name);
        values.put(DatabaseHelper.COLUMN_POINTS_RECEIVED, pointsEarned);
        values.put(DatabaseHelper.COLUMN_POINTS_POSSIBLE, pointsPossible);
        values.put(DatabaseHelper.COLUMN_DATE_ADDED, Calendar.getInstance().getTimeInMillis());

        return mDatabase.insert(DatabaseHelper.TABLE_GRADES, null, values);
    }

    /**
     * Updates a grade using its fields.
     *
     * @return -1 if update failed.
     */
    public int updateGrade(long gradeId, long courseId, long componentId, String name,
                           double pointsEarned, double pointsPossible) {
        // Calendar instance of year and day of year columns.

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_COURSE_ID, courseId);
        values.put(DatabaseHelper.COLUMN_COMPONENT_ID, componentId);
        values.put(DatabaseHelper.COLUMN_GRADE_NAME, name);
        values.put(DatabaseHelper.COLUMN_POINTS_RECEIVED, pointsEarned);
        values.put(DatabaseHelper.COLUMN_POINTS_POSSIBLE, pointsPossible);

        return mDatabase.update(DatabaseHelper.TABLE_GRADES, values,
                DatabaseHelper.COLUMN_ID + " = '" + gradeId + "'", null);
    }

    /**
     * Deletes a grade from mDatabase.
     */
    public void deleteGrade(Grade grade) {
        long gradeId = grade.getId();

        mDatabase.delete(DatabaseHelper.TABLE_GRADES,
                DatabaseHelper.COLUMN_ID + " = '" + gradeId + "'", null);
    }

    /**
     * Get recent grades.
     */
    public List<Grade> getGrades() {

        List<Grade> grades = new ArrayList<Grade>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_GRADES, GRADE_COLUMNS,
                null, null, null, null, null);

        cursor.moveToFirst();

        // Repeat for all grades.
        while (!cursor.isAfterLast()) {
            grades.add(cursorToGrade(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        Collections.sort(grades);

        return grades;
    }

    /**
     * Gets all grades for a course.
     */
    public List<Grade> getGrades(long courseId) {

        // List of grades.
        List<Grade> grades = new ArrayList<Grade>();

        // Cursor to find all grades based on course id.
        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_GRADES, GRADE_COLUMNS,
                DatabaseHelper.COLUMN_COURSE_ID + " = '" + courseId + "'", null, null, null, null);

        cursor.moveToFirst();

        // Repeat for all grades.
        while (!cursor.isAfterLast()) {
            grades.add(cursorToGrade(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return grades;
    }

    /**
     * Get all grades for a grade component.
     */
    public List<Grade> getComponentGrades(long componentId) {

        // List of grades.
        List<Grade> grades = new ArrayList<Grade>();

        // Cursor to find all the grades related to component id.
        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_GRADES, GRADE_COLUMNS,
                DatabaseHelper.COLUMN_COMPONENT_ID + " = '" + componentId + "'", null, null, null, null);

        cursor.moveToFirst();

        // Repeat for all grades.
        while (!cursor.isAfterLast()) {
            grades.add(cursorToGrade(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return grades;
    }

    /**
     * Gets cumulative gpa by querying all completed list_course for final grade
     *
     * @return cumulative gpa of all completed courses. -1 if there are no completed courses.
     */
    public double getCumulativeGPA() {

        List<Semester> completedSemesters = getPastSemestersWithGPA();

        double gpa = 0;

        if (completedSemesters.isEmpty()) {
            // Gets gpa from completed courses.
            int numCompletedCourses = 0;

            Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_COURSES,
                    new String[]{DatabaseHelper.COLUMN_FINAL_GRADE, DatabaseHelper.COLUMN_IS_COMPLETED},
                    DatabaseHelper.COLUMN_IS_COMPLETED + " = '1'", null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                // Course final grade is integer of position of LetterGrade enum.
                int letterGradeIndex = cursor.getInt(0);
                gpa += Course.LetterGrade.values()[letterGradeIndex].getGpaEquivalent();
                numCompletedCourses++;
                cursor.moveToNext();
            }
            cursor.close();

            // Divide GPA by number of list_course.
            gpa = gpa / numCompletedCourses;

            // If there are not completed list_course, return -1.
            if (numCompletedCourses == 0) {
                return -1;
            } else {
                return gpa;
            }
        } else {
            for (Semester semester : completedSemesters) {
                gpa += semester.getGpa();
            }

            // Divide gpa by number of completed semesters.
            return gpa / completedSemesters.size();
        }
    }

    /**
     * Inserts a task into the mDatabase.
     *
     * @return reminder ID.
     */
    public long insertReminder(long courseId, String name, boolean isGraded, boolean isCompleted,
                               Calendar remindDate) {

        // Calendar instance for day added to mDatabase.
        Calendar addDate = Calendar.getInstance();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_COURSE_ID, courseId);
        values.put(DatabaseHelper.COLUMN_REMINDER_NAME, name);
        values.put(DatabaseHelper.COLUMN_IS_EXAM, isGraded ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_DATE_ADDED, Calendar.getInstance().getTimeInMillis());
        values.put(DatabaseHelper.COLUMN_DATE_REMIND, remindDate.getTimeInMillis());

        return mDatabase.insert(DatabaseHelper.TABLE_REMINDERS, null, values);
    }

    /**
     * Update task with task fields.
     *
     * @return -1 if update failed.
     */
    public int updateReminder(long reminderId, long courseId, String name, boolean isGraded,
                              boolean isCompleted, Calendar remindDate) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_COURSE_ID, courseId);
        values.put(DatabaseHelper.COLUMN_REMINDER_NAME, name);
        values.put(DatabaseHelper.COLUMN_IS_EXAM, isGraded ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_DATE_REMIND, remindDate.getTimeInMillis());

        return mDatabase.update(DatabaseHelper.TABLE_REMINDERS, values,
                DatabaseHelper.COLUMN_ID + " = '" + reminderId + "'", null);
    }

    /**
     * Deletes a task from database.
     */
    public void deleteReminder(Reminder reminder) {

        mDatabase.delete(DatabaseHelper.TABLE_REMINDERS,
                DatabaseHelper.COLUMN_ID + " = '" + reminder.getId() + "'", null);
    }

    /**
     * @return list of all reminders.
     */
    public List<Reminder> getReminders() {
        List<Reminder> reminders = new ArrayList<Reminder>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_REMINDERS, REMINDER_COLUMNS,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            reminders.add(cursorToReminder(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return reminders;
    }

    /**
     * @return list of tasks for a course.
     */
    public List<Reminder> getReminders(long courseId) {
        List<Reminder> reminders = new ArrayList<Reminder>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_REMINDERS, REMINDER_COLUMNS,
                DatabaseHelper.COLUMN_COURSE_ID + " = '" + courseId + "'", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            reminders.add(cursorToReminder(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return reminders;
    }

    /**
     * @return list of all current reminders.
     */
    public List<Reminder> getUpcomingReminders() {
        List<Reminder> reminders = new ArrayList<Reminder>();

        // Yesterday calendar for comparison.
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_REMINDERS, REMINDER_COLUMNS,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Reminder reminder = cursorToReminder(cursor);

            // Only add the reminder if it's current.
            if (reminder.getReminderDate().after(yesterday)) {
                reminders.add(reminder);
            }
            cursor.moveToNext();
        }
        cursor.close();

        return reminders;
    }

    /**
     * @return list of all current reminders.
     */
    public List<Reminder> getCurrentReminders(long courseId) {
        List<Reminder> reminders = new ArrayList<Reminder>();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_REMINDERS, REMINDER_COLUMNS,
                DatabaseHelper.COLUMN_COURSE_ID + " = '" + courseId + "'", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Reminder reminder = cursorToReminder(cursor);

            // Only add the reminder if it's current.
            if (reminder.getReminderDate().after(yesterday)) {
                reminders.add(reminder);
            }
            cursor.moveToNext();
        }
        cursor.close();

        return reminders;
    }

    /**
     * @return list of all incomplete tasks for a course.
     */
    public List<Reminder> getIncompleteReminders() {

        List<Reminder> reminders = new ArrayList<Reminder>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_REMINDERS, REMINDER_COLUMNS,
                DatabaseHelper.COLUMN_IS_COMPLETED + " = '0'", null, null, null, null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            reminders.add(cursorToReminder(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return reminders;
    }

    /**
     * @return list of all incomplete tasks for a course.
     */
    public List<Reminder> getIncompleteReminders(long courseId) {

        List<Reminder> reminders = new ArrayList<Reminder>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_REMINDERS, REMINDER_COLUMNS,
                DatabaseHelper.COLUMN_COURSE_ID + " = '" + courseId + "' AND " +
                        DatabaseHelper.COLUMN_IS_COMPLETED + " = '0'", null, null, null, null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            reminders.add(cursorToReminder(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return reminders;
    }

    /**
     * @return list of all complete tasks for a course.
     */
    public List<Reminder> getPastReminders(long courseId) {

        List<Reminder> reminders = new ArrayList<Reminder>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_REMINDERS, REMINDER_COLUMNS,
                DatabaseHelper.COLUMN_COURSE_ID + " = '" + courseId + "' AND " +
                        DatabaseHelper.COLUMN_IS_COMPLETED + " = '1'", null, null, null, null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            reminders.add(cursorToReminder(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return reminders;
    }

    /**
     * Reads cursor information and returns a Semester object.
     *
     * @return Semester object.
     */
    private Semester cursorToSemester(Cursor cursor) {
        Semester semester = new Semester();

        try {
            semester.setId(cursor.getLong(0));
        } catch (CursorIndexOutOfBoundsException e) {
            // If the cursor is out of bounds, it does not exist so return null.
            return null;
        }

        semester.setSeason(cursor.getString(1));
        semester.setYear(cursor.getInt(2));
        semester.setGpa(cursor.getDouble(3));
        semester.setCurrent(cursor.getInt(4) == 1);

        Calendar addDate = Calendar.getInstance();
        addDate.setTimeInMillis(cursor.getLong(5));
        semester.setEndDate(addDate);

        return semester;
    }

    /**
     * Reads cursor information and returns complete Course object.
     *
     * @return Course object.
     */
    private Course cursorToCourse(Cursor cursor) {
        Course course = new Course();

        try {
            course.setId(cursor.getLong(0));
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }

        course.setSemesterId(cursor.getLong(1));
        course.setName(cursor.getString(2));
        course.setInstructorName(cursor.getString(3));
        course.setInstructorEmail(cursor.getString(4));
        course.setFinalGradeValue(cursor.getInt(5));
        course.setCompleted(cursor.getInt(6) == 1);

        return course;
    }

    /**
     * Reads cursor information and returns complete GradeComponent object.
     *
     * @return GradeComponent object.
     */
    private GradeComponent cursorToGradeComponent(Cursor cursor) {
        GradeComponent gradeComponent = new GradeComponent();

        try {
            gradeComponent.setId(cursor.getLong(0));
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }

        gradeComponent.setCourseId(cursor.getLong(1));
        gradeComponent.setName(cursor.getString(2));
        gradeComponent.setWeight(cursor.getDouble(3));
        gradeComponent.setNumberOfItems(cursor.getInt(4));

        return gradeComponent;
    }

    /**
     * Reads cursor information and returns complete Grade object.
     *
     * @return Grade object.
     */
    private Grade cursorToGrade(Cursor cursor) {
        Grade grade = new Grade();

        try {
            grade.setId(cursor.getLong(0));
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }

        grade.setCourseId(cursor.getLong(1));
        grade.setComponentId(cursor.getLong(2));
        grade.setName(cursor.getString(3));
        grade.setPointsReceived(cursor.getDouble(4));
        grade.setPointsPossible(cursor.getDouble(5));

        Calendar addDate = Calendar.getInstance();
        addDate.setTimeInMillis(cursor.getLong(6));
        grade.setAddDate(addDate);

        return grade;
    }

    /**
     * Reads cursor information and returns complete Task object.
     *
     * @return Reminder object.
     */
    private Reminder cursorToReminder(Cursor cursor) {
        Reminder reminder = new Reminder();

        try {
            reminder.setId(cursor.getLong(0));
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }

        reminder.setCourseId(cursor.getLong(1));
        reminder.setName(cursor.getString(2));
        reminder.setExam((cursor.getInt(3) == 1));
        reminder.setCompleted((cursor.getInt(4) == 1));

        Calendar addDate = Calendar.getInstance();
        addDate.setTimeInMillis(cursor.getLong(5));
        reminder.setAddDate(addDate);

        Calendar remindDate = Calendar.getInstance();
        remindDate.setTimeInMillis(cursor.getLong(6));
        reminder.setReminderDate(remindDate);

        return reminder;
    }
}
