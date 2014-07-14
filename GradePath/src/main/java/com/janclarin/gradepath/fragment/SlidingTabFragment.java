package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.view.SlidingTabLayout;

public class SlidingTabFragment extends Fragment {

    private static final String LOG_TAG = SlidingTabFragment.class.getSimpleName();

    private Context mContext;
    private FragmentSlidingTabCallbacks mListener;

    private ViewPager mViewPager;
    private ImageButton mAddButton;
    private SlidingTabLayout mSlidingTabLayout;

    private TabPagerAdapter mAdapter;
    private int mCurrentPage;

    public SlidingTabFragment() {
    }

    public static SlidingTabFragment newInstance() {
        return new SlidingTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sliding_tabs, container, false);

        mViewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        mAddButton = (ImageButton) rootView.findViewById(R.id.btn_sliding_tabs_add);
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAddButton.setOnClickListener(new View.OnClickListener() {
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
                }
            }
        });

        // TODO: Fix. Doesn't work.
        // Page change listener to change the add button drawable.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                        mAddButton.setImageResource(R.drawable.list_semester_add);
                        break;
                    case 1:
                        mAddButton.setImageResource(R.drawable.list_course_add);
                        break;
                    case 2:
                    case 3:
                        mAddButton.setImageResource(R.drawable.list_grade_task_add);
                }
            }
        });

        // Get the ViewPager and set its PagerAdapter so that it can display items.
        mAdapter = new TabPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(1);

        // Set SlidingTabLayout's ViewPager.
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    /**
     * Updates the semester list within its tab.
     */
    public void updateSemesterList() {
        try {
            ((BaseListFragment) mAdapter.getTab(0)).updateListItems();
        } catch (NullPointerException e) {
            mAdapter.getItem(0);
        }
    }

    /**
     * Updates the course list within its tab.
     */
    public void updateCourseList() {
        try {
            ((BaseListFragment) mAdapter.getTab(1)).updateListItems();
        } catch (NullPointerException e) {
            mAdapter.getItem(1);
        }
    }

    /**
     * Updates the grade list within its tab.
     */
    public void updateGradeList() {
        try {
            ((BaseListFragment) mAdapter.getTab(2)).updateListItems();
        } catch (NullPointerException e) {
            mAdapter.getItem(2);
        }
    }

    /**
     * Updates the task list within its tab.
     */
    public void updateTaskList() {
        try {
            ((BaseListFragment) mAdapter.getTab(3)).updateListItems();
        } catch (NullPointerException e) {
            mAdapter.getItem(3);
        }
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

    private class TabPagerAdapter extends FragmentPagerAdapter {

        private final int NUM_PAGES = 4;
        private final SparseArray<Fragment> REGISTERED_FRAGMENTS = new SparseArray<Fragment>();

        public TabPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return ListSemesterFragment.newInstance();
                case 1:
                    return ListCourseFragment.newInstance();
                case 2:
                    return ListGradeFragment.newInstance();
                case 3:
                    return ListTaskFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            REGISTERED_FRAGMENTS.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            REGISTERED_FRAGMENTS.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return mContext.getString(R.string.title_fragment_list_semesters);
                case 1:
                    return mContext.getString(R.string.title_fragment_list_courses);
                case 2:
                    return mContext.getString(R.string.title_fragment_list_grades);
                case 3:
                    return mContext.getString(R.string.title_fragment_list_tasks);
                default:
                    return null;
            }
        }

        protected Fragment getTab(int position) {
            return REGISTERED_FRAGMENTS.get(position);
        }
    }
}
