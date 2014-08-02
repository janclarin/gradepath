package com.janclarin.gradepath.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.BaseActivity;
import com.janclarin.gradepath.model.Course;

public class CourseDetailFragment extends BaseFragment {

    private Course mCourse;

    private TextView mInstructorName;
    private TextView mInstructorEmail;

    public CourseDetailFragment() {
        // Required empty public constructor
    }

    public static CourseDetailFragment newInstance(Course course) {
        CourseDetailFragment fragment = new CourseDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(BaseActivity.COURSE_KEY, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourse = (Course) getArguments().getSerializable(BaseActivity.COURSE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View rootView = inflater.inflate(R.layout.fragment_course_detail, container, false);

        mInstructorName = (TextView) rootView.findViewById(R.id.tv_subtitle);
        mInstructorEmail = (TextView) rootView.findViewById(R.id.tv_instructor_email);

        mInstructorName.setText(mCourse.getInstructorName());
        mInstructorEmail.setText(mCourse.getInstructorEmail());

        return rootView;
    }

    /**
     * Gets the updated course from the database and refreshes the text views.
     */
    public void onCourseUpdated(Course course) {
        mCourse = course;
        mInstructorName.setText(mCourse.getInstructorName());
        mInstructorEmail.setText(mCourse.getInstructorEmail());
    }
}
