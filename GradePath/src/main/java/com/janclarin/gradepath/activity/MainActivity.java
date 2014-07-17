package com.janclarin.gradepath.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.dialog.SemesterDialogFragment;
import com.janclarin.gradepath.dialog.TaskDialogFragment;
import com.janclarin.gradepath.fragment.ListCourseFragment;
import com.janclarin.gradepath.fragment.ListGradeFragment;
import com.janclarin.gradepath.fragment.ListSemesterFragment;
import com.janclarin.gradepath.fragment.ListTaskFragment;
import com.janclarin.gradepath.fragment.SettingsFragment;
import com.janclarin.gradepath.fragment.SlidingTabFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Semester;
import com.janclarin.gradepath.model.Task;

import java.util.ArrayList;
import java.util.List;


/**
 * Main activity that contains the navigation drawer.
 */
public class MainActivity extends BaseActivity
        implements SlidingTabFragment.OnFragmentSlidingTabsListener,
        ListSemesterFragment.OnFragmentListSemesterListener,
        ListCourseFragment.OnFragmentListCourseListener,
        ListGradeFragment.OnFragmentListGradeListener,
        ListTaskFragment.OnFragmentListTaskListener,
        SemesterDialogFragment.OnDialogSemesterCallbacks,
        GradeDialogFragment.OnDialogGradeListener,
        TaskDialogFragment.OnDialogTaskListener,
        SettingsFragment.OnFragmentSettingsListener {

    public static final int REQUEST_LIST_COURSE_NEW_COURSE = 101;
    public static final int REQUEST_LIST_COURSE_EDIT_COURSE = 102;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;
    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private int mCurrentSelectedPosition;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private List<DrawerItem> mDrawerItems;
    private ListView mDrawerListView;
    private SlidingTabFragment mSlidingTabFragment;
    private SettingsFragment mSettingsFragment;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTitle = getString(R.string.title_fragment_home);

        setUpNavigationDrawer((DrawerLayout) findViewById(R.id.drawer_layout));

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        selectItem(mCurrentSelectedPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the navigation drawer.
     *
     * @param drawerLayout
     */
    private void setUpNavigationDrawer(DrawerLayout drawerLayout) {
        /* Set up list view first. */
        mDrawerListView = (ListView) findViewById(R.id.lv_navigation_drawer);

        // List of navigation drawer items.
        mDrawerItems = new ArrayList<DrawerItem>();

        // Add drawer options.
        mDrawerItems.add(new DrawerItem(getString(R.string.title_fragment_home), 0));
        mDrawerItems.add(new DrawerItem(getString(R.string.title_fragment_settings), 0));

        BaseAdapter adapter = new ListAdapter();
        mDrawerListView.setAdapter(adapter);

        // Set on click listener for items in list view.
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        /* Set up drawer now. */
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mDrawerListView);
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass configuration changes to drawer toggle.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Select item from navigation drawer.
     *
     * @param position
     */
    private void selectItem(int position) {
        FragmentTransaction fragmentTransaction;

        switch (position) {
            case 0:
                mTitle = getString(R.string.app_name);
                if (mCurrentFragment instanceof SlidingTabFragment) {
                    break;
                }
                fragmentTransaction = getFragmentManager().beginTransaction();

                // If necessary, instantiate the SlidingTabFragment and display it.
                // Otherwise, remove the previous fragment and show the existing SlidingTabFragment.
                if (mSlidingTabFragment == null) {
                    mSlidingTabFragment = SlidingTabFragment.newInstance();
                    fragmentTransaction.replace(R.id.container, mSlidingTabFragment).commit();
                } else {
                    fragmentTransaction.remove(mCurrentFragment);
                    fragmentTransaction.show(mSlidingTabFragment).commit();
                }

                mCurrentFragment = mSlidingTabFragment;
                break;
            case 1:
                mTitle = getString(R.string.title_fragment_settings);
                if (mCurrentFragment instanceof SettingsFragment) {
                    break;
                }
                fragmentTransaction = getFragmentManager().beginTransaction();

                if (mCurrentFragment instanceof SlidingTabFragment) {
                    fragmentTransaction.hide(mCurrentFragment);
                }

                if (mSettingsFragment == null) {
                    mSettingsFragment = SettingsFragment.newInstance();
                }

                fragmentTransaction.add(R.id.container, mSettingsFragment);
                fragmentTransaction.show(mSettingsFragment).commit();

                mCurrentFragment = mSettingsFragment;
                break;
        }

        mCurrentSelectedPosition = position;

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerListView);
        }


    }

    /**
     * Refresh semester list.
     */
    private void refreshListSemester() {
        ((SlidingTabFragment) mCurrentFragment).updateSemesterList();
    }

    /**
     * Refresh course list.
     */
    private void refreshListCourse() {
        ((SlidingTabFragment) mCurrentFragment).updateCourseList();
    }

    /**
     * Refresh grade list.
     */
    private void refreshListGrade() {
        ((SlidingTabFragment) mCurrentFragment).updateGradeList();
    }

    /**
     * Refresh task list.
     */
    private void refreshListTask() {
        ((SlidingTabFragment) mCurrentFragment).updateTaskList();
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

    /**
     * List adapter for drawer.
     */
    private class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDrawerItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mDrawerItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Selected item.
            DrawerItem selectedItem = (DrawerItem) getItem(position);

            // View holder.
            ViewHolder viewHolder;

            // Inflate item view if it doesn't exist already depending on item type.
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater()
                        .inflate(R.layout.fragment_navigation_drawer_item, parent, false);

                viewHolder.tvSectionName = (TextView) convertView.findViewById(R.id.tv_drawer_list_item);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Set text view to title.
            viewHolder.tvSectionName.setText(selectedItem.getTitle());

            // If item has an icon, apply it to text view. Remove default padding left.
            int iconId = selectedItem.getIcon();
            if (iconId != 0) {
                viewHolder.tvSectionName.setCompoundDrawablePadding(8);
                viewHolder.tvSectionName.setPadding(0, 0, 0, 0);
                viewHolder.tvSectionName.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
            }

            return convertView;
        }

        class ViewHolder {
            TextView tvSectionName;
        }
    }

    /**
     * Navigation drawer item.
     */
    private class DrawerItem {
        private final String title;
        private final int icon;

        public DrawerItem(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public int getIcon() {
            return icon;
        }

    }
}
