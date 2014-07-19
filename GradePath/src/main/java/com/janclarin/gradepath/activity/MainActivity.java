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
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Semester;
import com.janclarin.gradepath.model.Task;

import java.lang.ref.WeakReference;


/**
 * Main activity that contains the navigation drawer.
 */
public class MainActivity extends BaseActivity
        implements ListSemesterFragment.OnFragmentListSemesterListener,
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
     * Used to store the last screen mTitle.
     */
    private CharSequence mTitle;
    /**
     * List of drawer items.
     */
    private DrawerItem[] mDrawerItems;
    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private int mCurrentSelectedPosition;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private ListView mDrawerListView;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setUpNavigationDrawer((DrawerLayout) findViewById(R.id.drawer_layout));

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Set action bar title.
        mTitle = getString(mDrawerItems[mCurrentSelectedPosition].getTitle());
        getActionBar().setTitle(mTitle);

        // Select item from navigation drawer.
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

    /* Go back to home. If on home and back is pressed, leave app */
    @Override
    public void onBackPressed() {
        if (mCurrentFragment instanceof ListCourseFragment) {
            super.onBackPressed();
        } else {
            selectItem(0);
        }
    }

    /**
     * Sets up the navigation drawer.
     *
     * @param drawerLayout
     */
    private void setUpNavigationDrawer(DrawerLayout drawerLayout) {
        /* Set up list view first. */
        mDrawerListView = (ListView) findViewById(R.id.lv_navigation_drawer);

        // Drawer items.
        mDrawerItems = new DrawerItem[]{
                new DrawerItem(R.string.title_fragment_list_courses, 0),
                new DrawerItem(R.string.title_fragment_list_grades, 0),
                new DrawerItem(R.string.title_fragment_list_tasks, 0),
                new DrawerItem(R.string.title_fragment_list_semesters, 0),
                new DrawerItem(R.string.title_fragment_settings, 0)
        };

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
        // between the navigation drawer and the action bar app mIcon.
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

        // Selected drawer item.
        DrawerItem drawerItem = mDrawerItems[position];

        // Set mTitle to drawer item.
        mTitle = getString(drawerItem.getTitle());

        // Get drawer item's fragment.
        mCurrentFragment = drawerItem.getFragment();

        if (mCurrentFragment == null) {
            switch (position) {
                case 0:
                    mCurrentFragment = ListCourseFragment.newInstance();
                    break;
                case 1:
                    mCurrentFragment = ListGradeFragment.newInstance();
                    break;
                case 2:
                    mCurrentFragment = ListTaskFragment.newInstance();
                    break;
                case 3:
                    mCurrentFragment = ListSemesterFragment.newInstance();
                    break;
                case 4:
                    mCurrentFragment = SettingsFragment.newInstance();
                    break;
                default:
                    mCurrentFragment = new Fragment();
            }
            drawerItem.setFragment(mCurrentFragment);
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, mCurrentFragment).commit();

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
        ((ListSemesterFragment) mCurrentFragment).updateListItems();
    }

    /**
     * Refresh course list.
     */
    private void refreshListCourse() {
        ((ListCourseFragment) mCurrentFragment).updateListItems();
    }

    /**
     * Refresh grade list.
     */
    private void refreshListGrade() {
        if (mCurrentFragment instanceof ListCourseFragment) {
            ((ListCourseFragment) mCurrentFragment).updateListItems();
        } else {
            ((ListGradeFragment) mCurrentFragment).updateListItems();
        }
    }

    /**
     * Refresh task list.
     */
    private void refreshListTask() {
        if (mCurrentFragment instanceof ListCourseFragment) {
            ((ListCourseFragment) mCurrentFragment).updateListItems();
        } else {
            ((ListTaskFragment) mCurrentFragment).updateListItems();
        }
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

    @Override
    public void onListSemesterNew() {
        // Show new semester dialog.
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_new_semester_dialog));
        semesterDialog.show(getFragmentManager(), NEW_SEMESTER_TAG);
    }

    @Override
    public void onListSemesterEdit(Semester semester) {
        // show update semester dialog
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_edit_semester_dialog), semester);
        semesterDialog.show(getFragmentManager(), EDIT_SEMESTER_TAG);
    }

    @Override
    public void onListSemesterDelete(final Semester semester) {
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

    /* ListCourseFragment listeners */
    @Override
    public void onListCourseNew() {
        // Check if there are any semesters. If not, display message.
        Intent intent = new Intent(this, CourseEditActivity.class);
        startActivityForResult(intent, REQUEST_LIST_COURSE_NEW_COURSE);
    }

    @Override
    public void onListCourseNewGrade(Course course) {
        // Show new grade dialog.
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_new_grade_dialog), course);
        gradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    @Override
    public void onListCourseNewTask(Course course) {
        // Show new task dialog.
        TaskDialogFragment taskDialog = TaskDialogFragment
                .newInstance(getString(R.string.title_new_task_dialog), course);
        taskDialog.show(getFragmentManager(), NEW_TASK_TAG);
    }

    @Override
    public void onListCourseViewDetails(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivity(intent);
    }

    @Override
    public void onListCourseEdit(Course course) {
        Intent intent = new Intent(this, CourseEditActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivityForResult(intent, REQUEST_LIST_COURSE_EDIT_COURSE);
    }

    @Override
    public void onListCourseDelete(final Course course) {
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

    /* ListGradeFragment listeners */
    @Override
    public void onListGradeNew() {
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_new_grade_dialog));
        gradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    @Override
    public void onListGradeEdit(Grade grade) {
        // Show edit grade dialog.
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_edit_grade_dialog), grade);
        gradeDialog.show(getFragmentManager(), EDIT_GRADE_TAG);
    }

    @Override
    public void onListTaskNew() {
        // Show new task dialog.
        TaskDialogFragment taskDialog = TaskDialogFragment.newInstance(
                getString(R.string.title_new_task_dialog));
        taskDialog.show(getFragmentManager(), NEW_TASK_TAG);
    }

    @Override
    public void onListTaskEdit(Task task) {
        // Show edit task dialog.
        TaskDialogFragment taskDialog = TaskDialogFragment.newInstance(
                getString(R.string.title_edit_task_dialog), task);
        taskDialog.show(getFragmentManager(), EDIT_TASK_TAG);
    }

    /* Dialog listeners */
    @Override
    public void onSemesterSaved(boolean isNew, Semester semester) {
        // String is set to "semester saved" if grade is new, if updating "semester updated."
        String toastMessage = isNew ? getString(R.string.toast_semester_saved) :
                getString(R.string.toast_semester_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        // Refresh the semester list.
        refreshListSemester();
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

        refreshListTask();
    }

    /**
     * List adapter for drawer.
     */
    private class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDrawerItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mDrawerItems[position];
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

            // Set text view to mTitle.
            viewHolder.tvSectionName.setText(selectedItem.getTitle());

            // If item has an mIcon, apply it to text view. Remove default padding left.
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
        private final int mTitle;
        private final int mIcon;
        private WeakReference<Fragment> mFragmentReference;

        public DrawerItem(int title, int icon) {
            mTitle = title;
            mIcon = icon;
        }

        public int getTitle() {
            return mTitle;
        }

        public int getIcon() {
            return mIcon;
        }

        public Fragment getFragment() {
            return mFragmentReference == null ? null : mFragmentReference.get();
        }

        public void setFragment(Fragment fragment) {
            this.mFragmentReference = new WeakReference<Fragment>(fragment);
        }
    }
}
