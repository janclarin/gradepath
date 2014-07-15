package com.janclarin.gradepath.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.fragment.ListCourseGradeFragment;
import com.janclarin.gradepath.model.Course;

public class DetailCourseActivity extends Activity
        implements ListCourseGradeFragment.FragmentListCourseGradeListener {

    private static final String LOG_TAG = DetailCourseActivity.class.getSimpleName();
    private static final int NUM_TABS = 3;

    private ViewPager mViewPager;

    private DatabaseFacade mDatabase;
    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_course);

        mDatabase = DatabaseFacade.getInstance(getApplicationContext());
        mDatabase.open();

        mCourse = (Course) getIntent().getSerializableExtra(MainActivity.COURSE_KEY);

        final ActionBar actionBar = getActionBar();

        // Set action bar navigation to tabs.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(tabPagerAdapter);
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
                            .setText(tabPagerAdapter.getPageTitle(i))
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

    }

    private class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return ListCourseGradeFragment.newInstance(mCourse);
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_overview);
                case 1:
                    return getString(R.string.title_fragment_list_grades);
                case 2:
                    return getString(R.string.title_fragment_list_tasks);
                default:
                    return null;
            }
        }
    }
}
