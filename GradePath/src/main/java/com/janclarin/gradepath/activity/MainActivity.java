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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.dialog.ReminderDialogFragment;
import com.janclarin.gradepath.dialog.SemesterDialogFragment;
import com.janclarin.gradepath.fragment.HomeFragment;
import com.janclarin.gradepath.fragment.ListAllCourseFragment;
import com.janclarin.gradepath.fragment.ListAllGradeFragment;
import com.janclarin.gradepath.fragment.ListAllReminderFragment;
import com.janclarin.gradepath.fragment.ListSemesterFragment;
import com.janclarin.gradepath.fragment.SettingsFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Reminder;
import com.janclarin.gradepath.model.Semester;

import java.lang.ref.WeakReference;


/**
 * Main activity that contains the navigation drawer.
 */
public class MainActivity extends BaseActivity
        implements HomeFragment.FragmentHomeListener,
        ListSemesterFragment.OnFragmentListSemesterListener,
        ListAllCourseFragment.OnFragmentListCourseListener,
        ListAllGradeFragment.OnFragmentListGradeListener,
        ListAllReminderFragment.OnFragmentListTaskListener,
        SemesterDialogFragment.OnDialogSemesterCallbacks,
        GradeDialogFragment.OnDialogGradeListener,
        ReminderDialogFragment.OnDialogReminderListener,
        SettingsFragment.OnFragmentSettingsListener {

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

        // Change soft input to adjust pan.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

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
        if (mDrawerToggle.onOptionsItemSelected(item) && mDrawerToggle.isDrawerIndicatorEnabled()) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Go back to home. If on home and back is pressed, leave app */
    @Override
    public void onBackPressed() {
        getActionBar().setTitle(mTitle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.onBackPressed();
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
                new DrawerItem(R.string.title_fragment_home, R.drawable.home),
                new DrawerItem(R.string.title_fragment_list_semesters, R.drawable.semester),
                new DrawerItem(R.string.title_fragment_settings, R.drawable.settings)
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

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        if (mCurrentFragment == null) {
            switch (position) {
                case 0:
                    mCurrentFragment = HomeFragment.newInstance();
                    break;
                case 1:
                    mCurrentFragment = ListSemesterFragment.newInstance();
                    break;
                case 2:
                    mCurrentFragment = SettingsFragment.newInstance();
                    break;
                default:
                    mCurrentFragment = new Fragment();
            }
            drawerItem.setFragment(mCurrentFragment);
        }

        fragmentTransaction.replace(R.id.container, mCurrentFragment).commit();

        mCurrentSelectedPosition = position;

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
            mDrawerListView.setSelection(position);
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
        if (mCurrentFragment instanceof HomeFragment) {
            ((HomeFragment) mCurrentFragment).updateListItems(false, true, false);
        } else if (mCurrentFragment instanceof ListAllCourseFragment) {
            ((ListAllCourseFragment) mCurrentFragment).updateListItems();
        }
    }

    /**
     * Refresh grade list.
     */
    private void refreshListGrade() {
        if (mCurrentFragment instanceof HomeFragment) {
            ((HomeFragment) mCurrentFragment).updateListItems(false, false, true);
        } else if (mCurrentFragment instanceof ListAllGradeFragment) {
            ((ListAllGradeFragment) mCurrentFragment).updateListItems();
        }
    }

    /**
     * Refresh task list.
     */
    private void refreshListReminder() {
        if (mCurrentFragment instanceof HomeFragment) {
            ((HomeFragment) mCurrentFragment).updateListItems(true, false, false);
        } else if (mCurrentFragment instanceof ListAllReminderFragment) {
            ((ListAllReminderFragment) mCurrentFragment).updateListItems();
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
    public void onHomeNewGrade() {
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_new_grade_dialog));
        gradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    @Override
    public void onHomeNewReminder() {
        ReminderDialogFragment taskDialog = ReminderDialogFragment.newInstance(
                getString(R.string.title_new_reminder_dialog));
        taskDialog.show(getFragmentManager(), NEW_REMINDER_TAG);
    }

    @Override
    public void onHomeNewCourse() {
        Intent intent = new Intent(this, CourseEditActivity.class);
        startActivityForResult(intent, REQUEST_LIST_COURSE_NEW_COURSE);

    }

    @Override
    public void onHomeAllGrades() {
        // Go to grades fragment.
        mCurrentFragment = ListAllGradeFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .replace(R.id.container, mCurrentFragment).commit();
        getActionBar().setTitle(R.string.title_fragment_list_grades);
        // Enable up arrow and disable navigation drawer.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onHomeAllReminders() {
        // Go to reminders fragment.
        mCurrentFragment = ListAllReminderFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .replace(R.id.container, mCurrentFragment).commit();
        getActionBar().setTitle(R.string.title_fragment_list_reminders);
        // Enable up arrow and disable navigation drawer.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onHomeAllCourses() {
        // Go to courses fragment.
        mCurrentFragment = ListAllCourseFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .replace(R.id.container, mCurrentFragment).commit();
        getActionBar().setTitle(R.string.title_fragment_list_courses);
        // Enable up arrow and disable navigation drawer.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onHomeViewCourse(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivity(intent);
    }

    @Override
    public void onHomeViewCourse(Course course, Reminder reminder) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra(COURSE_KEY, course);
        intent.putExtra(REMINDER_KEY, reminder);
        startActivity(intent);
    }

    @Override
    public void onHomeViewCourse(Course course, Grade grade) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra(COURSE_KEY, course);
        intent.putExtra(GRADE_KEY, grade);
        startActivity(intent);
    }

    @Override
    public void onListSemesterNew() {
        SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                getString(R.string.title_new_semester_dialog));
        semesterDialog.show(getFragmentManager(), NEW_SEMESTER_TAG);
    }

    @Override
    public void onListSemesterEdit(Semester semester) {
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
    public void onListCourseNewReminder(Course course) {
        // Show new task dialog.
        ReminderDialogFragment taskDialog = ReminderDialogFragment
                .newInstance(getString(R.string.title_new_reminder_dialog), course);
        taskDialog.show(getFragmentManager(), NEW_REMINDER_TAG);
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
                        mDatabase.deleteCourse(course.getId());
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
    public void onListGradeClick(Grade grade, Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra(COURSE_KEY, course);
        intent.putExtra(GRADE_KEY, grade);
        startActivity(intent);
    }

    @Override
    public void onListReminderNew() {
        // Show new task dialog.
        ReminderDialogFragment taskDialog = ReminderDialogFragment.newInstance(
                getString(R.string.title_new_reminder_dialog));
        taskDialog.show(getFragmentManager(), NEW_REMINDER_TAG);
    }

    @Override
    public void onListReminderEdit(Reminder reminder) {
        // Show edit task dialog.
        ReminderDialogFragment taskDialog = ReminderDialogFragment.newInstance(
                getString(R.string.title_edit_reminder_dialog), reminder);
        taskDialog.show(getFragmentManager(), EDIT_REMINDER_TAG);
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
    public void onReminderSaved(boolean isNew) {
        // String set to "task saved" if task is new, if updating "task updated."
        String toastMessage = isNew ? getString(R.string.toast_task_saved) :
                getString(R.string.toast_task_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        refreshListReminder();
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
                        .inflate(R.layout.fragment_list_item_navigation_drawer, parent, false);

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
                viewHolder.tvSectionName.setCompoundDrawablePadding(16);
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
