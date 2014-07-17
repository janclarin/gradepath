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

import java.lang.ref.WeakReference;

public class SlidingTabFragment extends Fragment {

    private static final String LOG_TAG = SlidingTabFragment.class.getSimpleName();

    private static final String STATE_SELECTED_POSITION = "selected_sliding_tab_position";

    private Context mContext;
    private OnFragmentSlidingTabsListener mListener;


    private TabPagerAdapter mAdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private ImageButton mAddButton;

    private int mCurrentSelectedPosition = 1;

    public SlidingTabFragment() {
    }

    public static SlidingTabFragment newInstance() {
        return new SlidingTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sliding_tabs, container, false);

        mViewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        mAddButton = (ImageButton) rootView.findViewById(R.id.btn_sliding_tabs_add);
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);

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
        mAdapter = new TabPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(TabPagerAdapter.NUM_PAGES - 1);
        mViewPager.setCurrentItem(mCurrentSelectedPosition);

        // Set SlidingTabLayout's ViewPager.
        mSlidingTabLayout.setViewPager(mViewPager);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentSlidingTabsListener) activity;
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

    /**
     * Save position.
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    /**
     * Updates the semester list within its tab.
     */
    public void updateSemesterList() {
        try {
            ((BaseListFragment) mAdapter.getTabFragment(0)).updateListItems();
        } catch (NullPointerException e) {
            mAdapter.getItem(0);
        }
    }

    /**
     * Updates the course list within its tab.
     */
    public void updateCourseList() {
        try {
            ((BaseListFragment) mAdapter.getTabFragment(1)).updateListItems();
        } catch (NullPointerException e) {
            mAdapter.getItem(1);
        }
    }

    /**
     * Updates the grade list within its tab.
     */
    public void updateGradeList() {
        try {
            ((BaseListFragment) mAdapter.getTabFragment(2)).updateListItems();
        } catch (NullPointerException e) {
            mAdapter.getItem(2);
        }
    }

    /**
     * Updates the task list within its tab.
     */
    public void updateTaskList() {
        try {
            ((BaseListFragment) mAdapter.getTabFragment(3)).updateListItems();
        } catch (NullPointerException e) {
            mAdapter.getItem(3);
        }
    }

    public interface OnFragmentSlidingTabsListener {

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

        public static final int NUM_PAGES = 4;
        private final SparseArray<WeakReference<Fragment>> REGISTERED_FRAGMENTS
                = new SparseArray<WeakReference<Fragment>>(4);

        public TabPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;

            switch (position) {
                case 0:
                    fragment = ListSemesterFragment.newInstance();
                    break;
                case 1:
                    fragment = ListCourseFragment.newInstance();
                    break;
                case 2:
                    fragment = ListGradeFragment.newInstance();
                    break;
                case 3:
                    fragment = ListTaskFragment.newInstance();
                    break;
                default:
                    return null;
            }

            REGISTERED_FRAGMENTS.put(position, new WeakReference<Fragment>(fragment));
            return fragment;
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

        /**
         * @return the fragment tab at position parameter.
         */
        public Fragment getTabFragment(int position) {
            WeakReference<Fragment> reference = REGISTERED_FRAGMENTS.get(position);
            return reference == null ? null : reference.get();
        }
    }
}
