package com.janclarin.gradepath.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.dialog.SemesterDialogFragment;
import com.janclarin.gradepath.dialog.TaskDialogFragment;
import com.janclarin.gradepath.fragment.ListCourseFragment;
import com.janclarin.gradepath.fragment.ListGradeFragment;
import com.janclarin.gradepath.fragment.ListSemesterFragment;
import com.janclarin.gradepath.fragment.ListTaskFragment;
import com.janclarin.gradepath.fragment.NavigationDrawerFragment;
import com.janclarin.gradepath.fragment.SettingFragment;
import com.janclarin.gradepath.fragment.SlidingTabFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Semester;
import com.janclarin.gradepath.model.Task;


/**
 * Main activity that contains the navigation drawer.
 */
public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerListener,
        SlidingTabFragment.FragmentSlidingTabCallbacks,
        ListSemesterFragment.FragmentListSemesterListener,
        ListCourseFragment.FragmentListCourseListener,
        ListGradeFragment.FragmentListGradeListener,
        ListTaskFragment.FragmentListTaskListener,
        SemesterDialogFragment.DialogSemesterCallbacks,
        GradeDialogFragment.DialogGradeCallbacks,
        TaskDialogFragment.DialogTaskCallbacks {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_LIST_COURSE_NEW_COURSE = 101;
    public static final int REQUEST_LIST_COURSE_EDIT_COURSE = 102;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Keeps track of current fragment.
     */
    private Fragment mFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getString(R.string.title_fragment_home);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check if request was successful.
        if (resultCode == RESULT_OK) {
            String toastMessage = "";

            switch (requestCode) {
                case REQUEST_LIST_COURSE_NEW_COURSE:
                    refreshListCourse();
                    toastMessage = getString(R.string.course_saved_new);
                    break;
                case REQUEST_LIST_COURSE_EDIT_COURSE:
                    refreshListCourse();
                    toastMessage = getString(R.string.course_saved_update);
                    break;
            }

            // Display toast.
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    /**
     * Refresh semester list.
     */
    private void refreshListSemester() {
        ((SlidingTabFragment) mFragment).updateSemesterList();
    }

    /**
     * Refresh course list.
     */
    private void refreshListCourse() {
        ((SlidingTabFragment) mFragment).updateCourseList();
    }

    /**
     * Refresh grade list.
     */
    private void refreshListGrade() {
        ((SlidingTabFragment) mFragment).updateGradeList();
    }

    /**
     * Refresh task list.
     */
    private void refreshListTask() {
        ((SlidingTabFragment) mFragment).updateTaskList();
    }

    /**
     * Opens fragments based on navigation drawer selections.
     * Only opens them if they're not the current fragment.
     *
     * @param drawerIndex
     */
    @Override
    public void onNavigationDrawerItemSelected(NavigationDrawerFragment.DRAWER_OPTION drawerIndex) {

        // Get fragment transaction object.
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        switch (drawerIndex) {
            case HOME:
                if (mFragment instanceof SlidingTabFragment) {
                    return;
                }
                mFragment = SlidingTabFragment.newInstance();
                mTitle = getString(R.string.title_fragment_home);
                break;
            case SETTINGS:
                if (mFragment instanceof SettingFragment) {
                    return;
                }
                mFragment = SettingFragment.newInstance();
                mTitle = getString(R.string.title_fragment_settings);
                break;
        }

        // Replace fragment with manage list_course fragment.
        fragmentTransaction.replace(R.id.container, mFragment);

        // Add fragment to back stack.
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction.
        fragmentTransaction.commit();
    }

    /**
     * Callback from SlidingTabFragment when add button is pressed.
     */
    @Override
    public void onSlidingTabAddSemester() {
        // Show new semester dialog.
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_new_semester_dialog));
        semesterDialog.show(getFragmentManager(), NEW_SEMESTER_TAG);
    }

    /**
     * Callback from ListCourseFragment to add a new course.
     */
    @Override
    public void onSlidingTabAddCourse() {
        // Check if there are any semesters. If not, display message.
        if (mDatabase.semestersExist()) {
            Intent intent = new Intent(this, EditCourseActivity.class);
            startActivityForResult(intent, REQUEST_LIST_COURSE_NEW_COURSE);
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.warning_no_semesters), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSlidingTabAddGrade() {
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_new_grade_dialog));
        gradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    @Override
    public void onSlidingTabAddTask() {
        // Show new task dialog.
        TaskDialogFragment taskDialog = TaskDialogFragment.newInstance(
                getString(R.string.title_new_task_dialog));
        taskDialog.show(getFragmentManager(), NEW_TASK_TAG);
    }

    /**
     * Callback from ListSemesterFragment to edit a semester.
     */
    @Override
    public void onListSemesterEdit(Semester semester) {
        // show update semester dialog
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_edit_semester_dialog), semester);
        semesterDialog.show(getFragmentManager(), EDIT_SEMESTER_TAG);
    }

    /**
     * Callback from ListSemesterFragment to delete a semester.
     */
    @Override
    public void onListSemesterDelete(final Semester semester) {
        // Title contains arguments.
        final String title = String.format(getString(R.string.title_delete_semester_dialog), semester.toString());
        final String positiveMessage =
                String.format(getString(R.string.toast_alert_delete_confirmation), semester.toString());

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(R.string.message_delete_semester_dialog)
                .setIcon(R.drawable.ic_action_contextual_delete)
                .setPositiveButton(R.string.btn_alert_delete_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete course, display text, and refresh list.
                        mDatabase.deleteSemester(semester);
                        Toast.makeText(getApplicationContext(), positiveMessage, Toast.LENGTH_SHORT).show();
                        // Refresh lists.
                        refreshListSemester();
                        refreshListCourse();
                    }
                })
                .setNegativeButton(R.string.btn_alert_delete_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, just dismiss.
                    }
                })
                .show();
    }

    /**
     * Callback from ListCourseFragment to add a grade.
     *
     * @param course
     */
    @Override
    public void onListCourseAddGrade(Course course) {
        // Show new grade dialog.
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_new_grade_dialog), course);
        gradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    /**
     * Callback from ListCourseFragment to add a task.
     *
     * @param course
     */
    @Override
    public void onListCourseAddTask(Course course) {
        // Show new task dialog.
        TaskDialogFragment taskDialog = TaskDialogFragment
                .newInstance(getString(R.string.title_new_task_dialog), course);
        taskDialog.show(getFragmentManager(), NEW_TASK_TAG);
    }

    /**
     * Callback from ListCourseFragment to view course detail.
     *
     * @param course
     */
    @Override
    public void onListCourseViewDetails(Course course) {
        Intent intent = new Intent(this, DetailCourseActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivity(intent);
    }

    /**
     * Callback from ListCourseFragment to edit a course.
     *
     * @param course
     */
    @Override
    public void onListCourseEdit(Course course) {
        Intent intent = new Intent(this, EditCourseActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivityForResult(intent, REQUEST_LIST_COURSE_EDIT_COURSE);
    }

    /**
     * Callback from ListCourseFragment to delete a course.
     *
     * @param course
     */
    @Override
    public void onListCourseDelete(final Course course) {
        // Title contains arguments.
        final String title = String.format(getString(R.string.title_delete_course_dialog), course.getName());
        final String positiveMessage =
                String.format(getString(R.string.toast_alert_delete_confirmation), course.getName());

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(R.string.message_delete_course_dialog)
                .setIcon(R.drawable.ic_action_contextual_delete)
                .setPositiveButton(R.string.btn_alert_delete_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete course, display text, and refresh list.
                        mDatabase.deleteCourse(course);
                        Toast.makeText(getApplicationContext(), positiveMessage, Toast.LENGTH_SHORT).show();
                        refreshListCourse();
                    }
                })
                .setNegativeButton(R.string.btn_alert_delete_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, just dismiss.
                    }
                })
                .show();
    }

    @Override
    public void onListGradeEdit(Grade grade) {
        // Show edit grade dialog.
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_edit_grade_dialog), grade);
        gradeDialog.show(getFragmentManager(), EDIT_GRADE_TAG);
    }

    @Override
    public void onListTaskEdit(Task task) {
        // Show edit task dialog.
        TaskDialogFragment taskDialog = TaskDialogFragment.newInstance(
                getString(R.string.title_edit_task_dialog), task);
        taskDialog.show(getFragmentManager(), EDIT_TASK_TAG);
    }

    @Override
    public void onSemesterSaved(boolean isNew) {
        // String is set to "semester saved" if grade is new, if updating "semester updated."
        String toastMessage = isNew ? getString(R.string.toast_semester_saved) :
                getString(R.string.toast_semester_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        // Refresh the semester list.
        refreshListSemester();
        refreshListCourse();
    }

    @Override
    public void onGradeSaved(boolean isNew) {
        // String is set to "grade saved" if grade is new, if updating "grade updated."
        String toastMessage = isNew ? getString(R.string.toast_grade_saved) :
                getString(R.string.toast_grade_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        refreshListGrade();
    }

    @Override
    public void onTaskSaved(boolean isNew) {
        // String set to "task saved" if task is new, if updating "task updated."
        String toastMessage = isNew ? getString(R.string.toast_task_saved) :
                getString(R.string.toast_task_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        refreshListCourse();
        refreshListTask();
    }
}
