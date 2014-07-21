package com.janclarin.gradepath.activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.dialog.ReminderDialogFragment;
import com.janclarin.gradepath.fragment.BaseListFragment;
import com.janclarin.gradepath.fragment.ListCourseGradeFragment;
import com.janclarin.gradepath.fragment.ListCourseReminderFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Reminder;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends BaseActivity
        implements ListCourseGradeFragment.FragmentListCourseGradeListener,
        ListCourseReminderFragment.FragmentListCourseTaskListener,
        GradeDialogFragment.OnDialogGradeListener,
        ReminderDialogFragment.OnDialogTaskListener {

    private static final String LOG_TAG = CourseDetailActivity.class.getSimpleName();
    private static final int NUM_TABS = 3;

    private TabPagerAdapter mAdapter;
    private ViewPager mViewPager;
    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_course);

        mCourse = (Course) getIntent().getSerializableExtra(MainActivity.COURSE_KEY);

        final ActionBar actionBar = getActionBar();

        // Set action bar navigation to tabs.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set action bar title to course name.
        actionBar.setTitle(mCourse.getName());

        mAdapter = new TabPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(NUM_TABS - 1);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                }
        );

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // When a tab is selected, switch the tab in the ViewPager.
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        for (int i = 0; i < NUM_TABS; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAdapter.getPageTitle(i))
                            .setTabListener(tabListener)
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListCourseGradeAdd(Course course) {
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_new_grade_dialog), course);
        gradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    @Override
    public void onListCourseGradeEdit(Grade grade) {
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_edit_grade_dialog), grade);
        gradeDialog.show(getFragmentManager(), EDIT_GRADE_TAG);
    }

    @Override
    public void onListCourseReminderAdd(Course course) {
        ReminderDialogFragment taskDialog = ReminderDialogFragment.newInstance(
                getString(R.string.title_new_task_dialog), course);
        taskDialog.show(getFragmentManager(), NEW_TASK_TAG);
    }

    @Override
    public void onListCourseReminderEdit(Reminder reminder) {
        ReminderDialogFragment taskDialog = ReminderDialogFragment.newInstance(
                getString(R.string.title_edit_task_dialog, reminder));
        taskDialog.show(getFragmentManager(), EDIT_TASK_TAG);
    }

    @Override
    public void onGradeSaved(boolean isNew) {
        // String is set to "grade saved" if grade is new, if updating "grade updated."
        String toastMessage = isNew ? getString(R.string.toast_grade_saved) :
                getString(R.string.toast_grade_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        ((BaseListFragment) mAdapter.getTab(1)).updateListItems();
    }

    @Override
    public void onReminderSaved(boolean isNew) {
        // String set to "task saved" if task is new, if updating "task updated."
        String toastMessage = isNew ? getString(R.string.toast_task_saved) :
                getString(R.string.toast_task_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        ((BaseListFragment) mAdapter.getTab(2)).updateListItems();
    }

    private class TabPagerAdapter extends FragmentStatePagerAdapter {

        List<Fragment> mTabs = new ArrayList<Fragment>();

        public TabPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = ListCourseGradeFragment.newInstance(mCourse);
                    break;
                case 1:
                    fragment = ListCourseReminderFragment.newInstance(mCourse);
                    break;
                case 2:
                    fragment = new Fragment();
                default:
                    fragment = new Fragment();
            }

            mTabs.add(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_list_grades);
                case 1:
                    return getString(R.string.title_fragment_list_tasks);
                case 2:
                    return getString(R.string.title_fragment_details);
                default:
                    return null;
            }
        }

        public Fragment getTab(int position) {
            Fragment fragment = mTabs.get(position);
            if (fragment != null) {
                return fragment;
            } else {
                return getItem(position);
            }
        }
    }
}
