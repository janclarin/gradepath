package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Course;

/**
 * Course fragment to display course information, grades, etc.
 */
public class ListCourseGradeFragment extends Fragment {

    private FragmentListCourseGradeListener mListener;

    // Selected Course object.
    private Course mCourse;

    private Context mContext;

    private ExpandableListView mGradeListView;
    private TextView mGradesTextView;
    private Button mAddFirstGradeButton;

    public ListCourseGradeFragment() {
        // Required empty public constructor.
    }

    /**
     * Creates a new instance of this fragment.
     *
     * @return A new instance of fragment CourseDetailsFragment.
     */
    public static ListCourseGradeFragment newInstance(Course course) {
        ListCourseGradeFragment fragment = new ListCourseGradeFragment();
        Bundle args = new Bundle();
        args.putSerializable(MainActivity.COURSE_KEY, course);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourse = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);
        }

        mContext = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_course_grades, container, false);
        mGradeListView = (ExpandableListView) rootView.findViewById(R.id.elv_course_fragment_grades);
        mGradesTextView = (TextView) rootView.findViewById(R.id.tv_grades_header);
        mAddFirstGradeButton = (Button) rootView.findViewById(R.id.btn_add_first_grade);

        // Get data from mDatabase and initialize mAdapter.
        fillData();

        return rootView;
    }

    /**
     * Updates information for expandable list view mAdapter with data from mDatabase.
     */
    public void fillData() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentListCourseGradeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListCourseGradeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate options menu.
        inflater.inflate(R.menu.list_course, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    /**
     * Listeners.
     */
    public static interface FragmentListCourseGradeListener {

        public void onListCourseGradeAdd(Course course);
    }
}
