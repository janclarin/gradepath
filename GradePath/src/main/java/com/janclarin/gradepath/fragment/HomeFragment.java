package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.astuetz.PagerSlidingTabStrip;
import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;

public class HomeFragment extends BaseFragment {

    private static final int NUM_TABS = 3;
    private int mSelectedPosition;
    private FragmentHomeListener mListener;

    private TabPagerAdapter mAdapter;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.viewPagerTabs);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        final ImageButton addButton = (ImageButton) view.findViewById(R.id.btn_add_item);

        mAdapter = new TabPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(NUM_TABS - 1);

        ViewPager.SimpleOnPageChangeListener onPageChangeListener =
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        mSelectedPosition = position;
                        // Switch the floating button depending on page and set its on-click listener.
                        switch (position) {
                            case 0: {
                                addButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mListener != null) mListener.onHomeNewGrade();
                                    }
                                });
                                break;
                            }
                            case 1: {
                                addButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mListener != null) mListener.onHomeNewCourse();
                                    }
                                });
                                break;
                            }
                            case 2: {
                                addButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mListener != null) mListener.onHomeNewSemester();
                                    }
                                });
                                break;
                            }
                        }
                    }
                };

        // Set tab strip ViewPager.
        tabStrip.setViewPager(viewPager);
        tabStrip.setUnderlineColorResource(android.R.color.transparent);
        tabStrip.setDividerColorResource(android.R.color.transparent);
        tabStrip.setTextColorResource(android.R.color.white);
        tabStrip.setTabBackground(R.color.theme_primary_color);
        tabStrip.setUnderlineHeight(0);
        tabStrip.setIndicatorColorResource(R.color.theme_secondary_color);
        tabStrip.setIndicatorHeight(8);
        tabStrip.setOnPageChangeListener(onPageChangeListener);

        onPageChangeListener.onPageSelected(mSelectedPosition);
    }

//    /**
//     * Refresh reminder list.
//     */
//    public void refreshReminderList() {
//        ((ListReminderFragment) mAdapter.getTabFragment(0)).updateListItems();
//    }

    /**
     * Refresh grade list.
     */
    public void refreshGradeList() {
        ((ListGradeFragment) mAdapter.getTabFragment(0)).updateListItems();
    }

    /**
     * Refresh course list.
     */
    public void refreshCourseList() {
        ((ListCourseFragment) mAdapter.getTabFragment(1)).updateListItems();
    }

    /**
     * Refresh semester list.
     */
    public void refreshSemesterList() {
        ((ListSemesterFragment) mAdapter.getTabFragment(2)).updateListItems();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentHomeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentHomeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class TabPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> mTabs = new SparseArray<Fragment>();

        public TabPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0: {
                    fragment = ListGradeFragment.newInstance();
                    break;
                }
                case 1: {
                    fragment = ListCourseFragment.newInstance();
                    break;
                }
                case 2: {
                    fragment = ListSemesterFragment.newInstance();
                    break;
                }
                default: {
                    fragment = new Fragment();
                }
            }

            mTabs.put(position, fragment);
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
                    return getString(R.string.title_fragment_list_courses);
                case 2:
                    return getString(R.string.title_fragment_list_semesters);
                default:
                    return null;
            }
        }

        public Fragment getTabFragment(int position) {
            Fragment fragment = mTabs.get(position);
            if (fragment != null) {
                return fragment;
            } else {
                return getItem(position);
            }
        }
    }

    public interface FragmentHomeListener {

        /* Calls new grade dialog. */
        public void onHomeNewGrade();

        /* Calls new reminder dialog. */
        public void onHomeNewReminder();

        /* Calls new course activity. */
        public void onHomeNewCourse();

        /* Calls new semester dialog. */
        public void onHomeNewSemester();

        /* Displays course detail fragment. */
        public void onHomeViewCourse(Course course);
    }
}
