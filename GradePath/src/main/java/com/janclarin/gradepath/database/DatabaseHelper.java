package com.janclarin.gradepath.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database object allowing control of SQL mDatabase.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Primary key column name.
     */
    public static final String COLUMN_ID = "_id";

    /**
     * Shared columns.
     */
    public static final String COLUMN_COURSE_ID = "course_id";
    public static final String COLUMN_DATE_ADDED = "date_added";

    /**
     * Semester table.
     */
    public static final String TABLE_SEMESTERS = "semesters";
    public static final String COLUMN_SEASON = "season";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_SEMESTER_GPA = "semester_gpa";
    private static final String CREATE_TABLE_SEMESTERS = "CREATE TABLE "
            + TABLE_SEMESTERS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_SEASON + " TEXT NOT NULL, "
            + COLUMN_YEAR + " INTEGER NOT NULL, "
            + COLUMN_SEMESTER_GPA + " REAL NOT NULL"
            + ");";

    /**
     * Course table.
     */
    public static final String TABLE_COURSES = "list_course";
    public static final String COLUMN_SEMESTER_ID = "semester_id";
    public static final String COLUMN_COURSE_NAME = "course_name";
    public static final String COLUMN_INSTRUCTOR_NAME = "instructor_name";
    public static final String COLUMN_INSTRUCTOR_EMAIL = "instructor_email";
    public static final String COLUMN_CREDITS = "credits";
    public static final String COLUMN_FINAL_GRADE = "final_grade";
    public static final String COLUMN_COURSE_COLOR = "course_color";
    private static final String CREATE_TABLE_COURSES = "CREATE TABLE "
            + TABLE_COURSES + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_SEMESTER_ID + " INTEGER NOT NULL, "
            + COLUMN_COURSE_NAME + " TEXT NOT NULL, "
            + COLUMN_INSTRUCTOR_NAME + " TEXT NOT NULL, "
            + COLUMN_INSTRUCTOR_EMAIL + " TEXT NOT NULL, "
            + COLUMN_CREDITS + " REAL NOT NULL, "
            + COLUMN_FINAL_GRADE + " INTEGER NOT NULL, "
            + COLUMN_COURSE_COLOR + " INTEGER NOT NULL"
            + ");";

    /**
     * Grade component table.
     */
    public static final String TABLE_COMPONENTS = "grade_components";
    public static final String COLUMN_COMPONENT_NAME = "component_name";
    public static final String COLUMN_COMPONENT_WEIGHT = "component_weight";
    public static final String COLUMN_COMPONENT_NUM_ITEMS = "component_num_items";
    private static final String CREATE_TABLE_COMPONENTS = "CREATE TABLE "
            + TABLE_COMPONENTS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_COURSE_ID + " INTEGER NOT NULL, "
            + COLUMN_COMPONENT_NAME + " TEXT NOT NULL, "
            + COLUMN_COMPONENT_WEIGHT + " REAL NOT NULL, "
            + COLUMN_COMPONENT_NUM_ITEMS + " INTEGER NOT NULL"
            + ");";

    /**
     * Grade table.
     * Includes course id, year added, day of year added.
     */
    public static final String TABLE_GRADES = "grades";
    public static final String COLUMN_COMPONENT_ID = "component_id";
    public static final String COLUMN_GRADE_NAME = "grade_name";
    public static final String COLUMN_POINTS_RECEIVED = "points_received";
    public static final String COLUMN_POINTS_POSSIBLE = "points_possible";
    private static final String CREATE_TABLE_GRADES = "CREATE TABLE "
            + TABLE_GRADES + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_COURSE_ID + " INTEGER NOT NULL, "
            + COLUMN_COMPONENT_ID + " INTEGER NOT NULL, "
            + COLUMN_GRADE_NAME + " TEXT NOT NULL, "
            + COLUMN_POINTS_RECEIVED + " REAL NOT NULL, "
            + COLUMN_POINTS_POSSIBLE + " REAL NOT NULL, "
            + COLUMN_DATE_ADDED + " INTEGER NOT NULL"
            + ");";

    /**
     * Database variables.
     */
    private static final String DATABASE_NAME = "gradepath.db";
    private static final int DATABASE_VERSION = 10;
    private static DatabaseHelper sInstance;

    /**
     * Constructor.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SEMESTERS);
        database.execSQL(CREATE_TABLE_COURSES);
        database.execSQL(CREATE_TABLE_COMPONENTS);
        database.execSQL(CREATE_TABLE_GRADES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading mDatabase version from " + oldVersion + " to "
                        + newVersion + " deleting all old data"
        );
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_SEMESTERS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPONENTS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        onCreate(database);
    }
}
