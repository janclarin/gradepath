package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.view.SlidingTabLayout;

public class SlidingTabFragment extends Fragment {

    private static final String LOG_TAG = SlidingTabFragment.class.getSimpleName();

    private FragmentSlidingTabCallbacks mListener;

    private ViewPager mViewPager;
    private TabItem[] mListTabItems;
    private TabItem mTabSemesters;
    private TabItem mTabCourses;
    private TabItem mTabGrades;
    private TabItem mTabTasks;

    public SlidingTabFragment() {
    }

    public static SlidingTabFragment newInstance() {
        SlidingTabFragment fragment = new SlidingTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTabSemesters = new TabItem(getString(R.string.title_fragment_list_semesters), new ListSemesterFragment());

        mTabCourses = new TabItem(getString(R.string.title_fragment_list_courses), new ListCourseFragment());

        mTabGrades = new TabItem(getString(R.string.title_fragment_list_grades), new ListGradeFragment());

        mTabTasks = new TabItem(getString(R.string.title_fragment_list_tasks), new ListTaskFragment());

        mListTabItems = new TabItem[]{mTabSemesters, mTabCourses, mTabGrades, mTabTasks};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sliding_tabs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Get the ViewPager and set its PagerAdapter so that it can display items.
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new TabPagerAdapter(getFragmentManager()));

        // Set SlidingTabLayout's ViewPager.
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1);

        ImageButton btnAdd = (ImageButton) view.findViewById(R.id.btn_sliding_tabs_add);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mViewPager.getCurrentItem()) {
                    case 0:
                        if (mListener != null) mListener.onSlidingTabAddSemester();
                        break;
                    case 1:
                        if (mListener != null) mListener.onSlidingTabAddCourse();
                        break;
                    case 2:
                        if (mListener != null) mListener.onSlidingTabAddGrade();
                        break;
                    case 3:
                        if (mListener != null) mListener.onSlidingTabAddTask();
                        break;
                }
            }
        });
    }

    /**
     * Updates the semester list within its tab.
     */
    public void updateSemesterList() {
        ((ListSemesterFragment) mTabSemesters.getFragment()).updateList();
    }

    /**
     * Updates the course list within its tab.
     */
    public void updateCourseList() {
        ((ListCourseFragment) mTabCourses.getFragment()).updateList();
    }

    /**
     * Updates the grade list within its tab.
     */
    public void updateGradeList() {
        ((ListGradeFragment) mTabGrades.getFragment()).updateList();
    }

    /**
     * Updates the task list within its tab.
     */
    public void updateTaskList() {
        ((ListTaskFragment) mTabTasks.getFragment()).updateList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentSlidingTabCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentSlidingTabCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentSlidingTabCallbacks {

        /**
         * Called when the add button is pressed while the semester list fragment focused.
         */
        void onSlidingTabAddSemester();

        /**
         * Called when the add button is pressed while the course list fragment focused.
         */
        void onSlidingTabAddCourse();

        /**
         * Called when the add button is pressed while the task list fragment is focused.
         */
        void onSlidingTabAddGrade();

        /**
         * Called when the add button is pressed while the task list fragment is focused.
         */
        void onSlidingTabAddTask();
    }

    /**
     * Tab item.
     */
    private class TabItem {

        private final String title;
        private final Fragment fragment;

        public TabItem(String title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }

        public String getTitle() {
            return title;
        }

        public Fragment getFragment() {
            return fragment;
        }
    }

    private class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return mListTabItems.length;
        }

        @Override
        public Fragment getItem(int index) {

            // Returns a fragment
            return mListTabItems[index].getFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mListTabItems[position].getTitle();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }
}
