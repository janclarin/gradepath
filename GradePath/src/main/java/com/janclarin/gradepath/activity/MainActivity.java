package com.janclarin.gradepath.activity;

import android.app.ActionBar;
import android.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.dialog.SemesterDialogFragment;
import com.janclarin.gradepath.fragment.HomeFragment;
import com.janclarin.gradepath.fragment.ListSemesterFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Semester;

import java.lang.ref.WeakReference;
import java.util.List;


/**
 * Main activity that contains the navigation drawer.
 */
public class MainActivity extends BaseActivity
        implements ActionBar.OnNavigationListener,
        HomeFragment.FragmentHomeListener,
        SemesterDialogFragment.OnDialogSemesterListener,
        GradeDialogFragment.OnDialogGradeListener {

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
     * List of drawer items.
     */
    private static final DrawerItem[] mDrawerItems = new DrawerItem[]{
            new DrawerItem(R.string.title_fragment_home, R.drawable.home),
            new DrawerItem(R.string.title_fragment_settings, R.drawable.settings)
    };
    /**
     * Used to store the last screen mTitle.
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
    private ListView mDrawerListView;
    private Fragment mCurrentFragment;

    private Semester mCurrentSemester;
    private List<Semester> mSemesters;

    private final Semester mAllSemestersOption = new Semester() {
        @Override
        public String toString() {
            return getString(R.string.all_semesters);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Change soft input to adjust pan.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Get list of semesters from database.
        mSemesters = mDatabase.getSemesters();

        // If no semesters, set title to app name.
        if (mSemesters.isEmpty()) {
            // Ask for semester input.
            SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                    getString(R.string.current_semester));
            semesterDialog.show(getFragmentManager(), NEW_SEMESTER_TAG);
        }

        // Set action bar drawer
        setUpActionBarSpinner();

        // Set up navigation drawer.
        setUpNavigationDrawer((DrawerLayout) findViewById(R.id.drawer_layout));

        // Select item from navigation drawer.
        onNavDrawerItemSelected(mCurrentSelectedPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            return true;
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_semester) {
            // Show semester dialog.
            SemesterDialogFragment semesterDialog = SemesterDialogFragment.newInstance(
                    getString(R.string.title_new_semester_dialog));
            semesterDialog.show(getFragmentManager(), NEW_SEMESTER_TAG);
        }
        if (mDrawerToggle.onOptionsItemSelected(item) && mDrawerToggle.isDrawerIndicatorEnabled()) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Go back to home. If on home and back is pressed, leave app */
    @Override
    public void onBackPressed() {
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        super.onBackPressed();
    }

    private void showGlobalActionBar() {
        final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    /**
     * Sets up the navigation drawer.
     *
     * @param drawerLayout
     */
    private void setUpNavigationDrawer(DrawerLayout drawerLayout) {
        /* Set up list view first. */
        mDrawerListView = (ListView) findViewById(R.id.lv_navigation_drawer);

        BaseAdapter adapter = new NavigationDrawerAdapter();
        mDrawerListView.setAdapter(adapter);

        // Set on click listener for items in list view.
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onNavDrawerItemSelected(position);
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
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                setUpActionBarSpinner();
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

                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                showGlobalActionBar();
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mDrawerListView);
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Sync the toggle state after onRestoreInstanceState.
        mDrawerToggle.syncState();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Select item from navigation drawer.
     *
     * @param position
     */
    private void onNavDrawerItemSelected(int position) {

        // Selected drawer item.
        DrawerItem drawerItem = mDrawerItems[position];

        // Get drawer item's fragment.
        mCurrentFragment = drawerItem.getFragment();


        if (mCurrentFragment == null) {
            switch (position) {
                case 0: {
                    mCurrentFragment = HomeFragment.newInstance(mCurrentSemester);
                    drawerItem.setFragment(mCurrentFragment);

                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, mCurrentFragment)
                            .commit();
                    break;
                }
                case 1: {
                    // Open settings activity.
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    break;
                }
            }
        }

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
     * Set up action bar spinner.
     */
    private void setUpActionBarSpinner() {
        if (mSemesters.size() > 0) {
            final ActionBar actionBar = getActionBar();

            // Add a semester to provide action to go to all semesters.
            if (!mSemesters.get(mSemesters.size() - 1).equals(mAllSemestersOption)) {
                mSemesters.add(mAllSemestersOption);
            }

            final ArrayAdapter<Semester> spinnerAdapter = new ArrayAdapter<Semester>(this,
                    R.layout.actionbar_item_spinner, mSemesters);


            spinnerAdapter.setDropDownViewResource(R.layout.actionbar_item_spinner_dropdown);

            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setListNavigationCallbacks(spinnerAdapter, this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // Set current semester selected semester.
        mCurrentSemester = mSemesters.get(position);

        if (position == mSemesters.size() - 1) {
            // Open semesters list when "All semesters is chosen".
            Intent intent = new Intent(this, ListFragmentActivity.class);
            intent.putExtra(ListFragmentActivity.FRAGMENT_TYPE, 2);
            startActivity(intent);
        } else {
            // Selected drawer item.
            DrawerItem drawerItem = mDrawerItems[position];
            mCurrentFragment = HomeFragment.newInstance(mCurrentSemester);
            drawerItem.setFragment(mCurrentFragment);

            // Otherwise, just open the home fragment properly.
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, mCurrentFragment)
                    .commit();
        }
        return mCurrentFragment != null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass configuration changes to drawer toggle.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    /**
     * Refresh semester list in action bar.
     */
    private void refreshListSemester() {
        if (mCurrentFragment instanceof ListSemesterFragment) {
            ((ListSemesterFragment) mCurrentFragment).updateListItems();
        } else {
            mSemesters = mDatabase.getSemesters();
            setUpActionBarSpinner();
        }
    }

    /**
     * Refresh course list.
     */
    private void refreshListCourse() {
        ((HomeFragment) mCurrentFragment).updateListItems();
    }

    /**
     * Refresh grade list.
     */
    private void refreshListGrade() {
        ((HomeFragment) mCurrentFragment).updateListItems();
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
    public void onHomeAddCourse() {
        Intent intent = new Intent(this, CourseEditActivity.class);
        intent.putExtra(SEMESTER_KEY, mCurrentSemester);
        startActivityForResult(intent, REQUEST_LIST_COURSE_NEW_COURSE);
    }

    @Override
    public void onHomeAddGrade() {
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_new_grade_dialog));
        gradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    @Override
    public void onHomeEditGrade(Grade grade) {
        // Show edit grade dialog.
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_edit_grade_dialog), grade);
        gradeDialog.show(getFragmentManager(), EDIT_GRADE_TAG);
    }


    @Override
    public void onHomeViewCourse(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra(COURSE_KEY, course);
        startActivity(intent);
    }

    @Override
    public void onHomeViewGrades() {
        Intent intent = new Intent(this, ListFragmentActivity.class);
        intent.putExtra(ListFragmentActivity.FRAGMENT_TYPE, 0);
        startActivity(intent);
    }

    @Override
    public void onHomeViewCourses() {
        Intent intent = new Intent(this, ListFragmentActivity.class);
        intent.putExtra(ListFragmentActivity.FRAGMENT_TYPE, 1);
        startActivity(intent);
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

    /**
     * List adapter for drawer.
     */
    private class NavigationDrawerAdapter extends BaseAdapter {
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
                        .inflate(R.layout.list_item_navigation_drawer, parent, false);

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
    private static class DrawerItem {
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
