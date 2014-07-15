package com.janclarin.gradepath.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.BaseActivity;
import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.model.Course;

public class DetailCourseFragment extends BaseDetailFragment {

    private Context mContext;
    private DatabaseFacade mDatabase;

    private Course mCourse;

    public static DetailCourseFragment newInstance(Course course) {
        DetailCourseFragment fragment = new DetailCourseFragment();
        Bundle args = new Bundle();
        args.putSerializable(BaseActivity.COURSE_KEY, course);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailCourseFragment() {
        // Required empty public constructor.
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
        return inflater.inflate(R.layout.fragment_detail_course, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
