package com.janclarin.gradepath.activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.GradeDialogFragment;
import com.janclarin.gradepath.dialog.ReminderDialogFragment;
import com.janclarin.gradepath.fragment.BaseListFragment;
import com.janclarin.gradepath.fragment.CourseDetailFragment;
import com.janclarin.gradepath.fragment.ListCourseGradeFragment;
import com.janclarin.gradepath.fragment.ListCourseReminderFragment;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Reminder;

import java.lang.ref.WeakReference;

public class CourseDetailActivity extends BaseActivity
        implements ListCourseGradeFragment.FragmentListCourseGradeListener,
        ListCourseReminderFragment.FragmentListCourseReminderListener,
        GradeDialogFragment.OnDialogGradeListener,
        ReminderDialogFragment.OnDialogReminderListener {

    private static final String LOG_TAG = CourseDetailActivity.class.getSimpleName();
    private static final int NUM_TABS = 3;

    private TabPagerAdapter mAdapter;
    private ViewPager mViewPager;
    private Course mCourse;
    private Grade mGrade;
    private Reminder mReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_course);

        mCourse = (Course) getIntent().getSerializableExtra(COURSE_KEY);
        mGrade = (Grade) getIntent().getSerializableExtra(GRADE_KEY);
        mReminder = (Reminder) getIntent().getSerializableExtra(REMINDER_KEY);

        final ActionBar actionBar = getActionBar();

        // Set action bar navigation to tabs.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set action bar title to course name.
        actionBar.setTitle(mCourse.getName());

        final ImageButton btnAddItem = (ImageButton) findViewById(R.id.btn_add_item);

        mAdapter = new TabPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(NUM_TABS - 1);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        actionBar.setSelectedNavigationItem(position);
                        // Switch the floating button depending on page and set its on-click listener.
                        switch (position) {
                            case 0:
                                btnAddItem.setImageResource(R.drawable.add_list_item);
                                btnAddItem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onListCourseGradeAdd();
                                    }
                                });
                                break;
                            case 1:
                                btnAddItem.setImageResource(R.drawable.add_list_item);
                                btnAddItem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onListCourseReminderAdd();
                                    }
                                });
                                break;
                            case 2:
                                btnAddItem.setImageResource(R.drawable.edit);
                                btnAddItem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onCourseEdit();
                                    }
                                });
                        }
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

        // Add tabs. If coming from reminder item, set reminder option as selected tab.
        for (int i = 0; i < NUM_TABS; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAdapter.getPageTitle(i))
                            .setTabListener(tabListener)
            );
        }

        // Go to reminders page if being opened from reminder item click.
        if (mReminder != null) {
            mViewPager.setCurrentItem(1, false);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_LIST_COURSE_EDIT_COURSE) {
            mAdapter.getItem(2);
            Toast.makeText(this, R.string.course_saved_update, Toast.LENGTH_SHORT).show();
        }
    }

    public void onListCourseGradeAdd() {
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_new_grade_dialog), mCourse);
        gradeDialog.show(getFragmentManager(), NEW_GRADE_TAG);
    }

    public void onListCourseReminderAdd() {
        ReminderDialogFragment taskDialog = ReminderDialogFragment.newInstance(
                getString(R.string.title_new_reminder_dialog), mCourse);
        taskDialog.show(getFragmentManager(), NEW_REMINDER_TAG);
    }

    public void onCourseEdit() {
        Intent intent = new Intent(this, CourseEditActivity.class);
        intent.putExtra(COURSE_KEY, mCourse);
        startActivityForResult(intent, REQUEST_LIST_COURSE_EDIT_COURSE);
    }

    @Override
    public void onListCourseGradeEdit(Grade grade) {
        GradeDialogFragment gradeDialog = GradeDialogFragment.newInstance(
                getString(R.string.title_edit_grade_dialog), grade);
        gradeDialog.show(getFragmentManager(), EDIT_GRADE_TAG);
    }

    @Override
    public void onListCourseReminderEdit(Reminder reminder) {
        ReminderDialogFragment taskDialog = ReminderDialogFragment.newInstance(
                getString(R.string.title_edit_reminder_dialog, reminder), reminder);
        taskDialog.show(getFragmentManager(), EDIT_REMINDER_TAG);
    }

    @Override
    public void onGradeSaved(boolean isNew) {
        // String is set to "grade saved" if grade is new, if updating "grade updated."
        String toastMessage = isNew ? getString(R.string.toast_grade_saved) :
                getString(R.string.toast_grade_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        ((BaseListFragment) mAdapter.getTabFragment(0)).updateListItems();
    }

    @Override
    public void onReminderSaved(boolean isNew) {
        // String set to "task saved" if task is new, if updating "task updated."
        String toastMessage = isNew ? getString(R.string.toast_task_saved) :
                getString(R.string.toast_task_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        ((BaseListFragment) mAdapter.getTabFragment(1)).updateListItems();
    }

    private class TabPagerAdapter extends FragmentStatePagerAdapter {

        SparseArray<WeakReference<Fragment>> mTabs = new SparseArray<WeakReference<Fragment>>();

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
                    fragment = CourseDetailFragment.newInstance(mCourse);
                    break;
                default:
                    fragment = new Fragment();
            }

            mTabs.put(position, new WeakReference<Fragment>(fragment));
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
                    return getString(R.string.title_fragment_list_reminders);
                case 2:
                    return getString(R.string.title_fragment_details);
                default:
                    return null;
            }
        }

        public Fragment getTabFragment(int position) {
            Fragment fragment = mTabs.get(position).get();
            if (fragment != null) {
                return fragment;
            } else {
                return getItem(position);
            }
        }
    }
}
