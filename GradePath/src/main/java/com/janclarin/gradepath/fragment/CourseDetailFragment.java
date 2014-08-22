package com.janclarin.gradepath.fragment;


import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.BaseActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.GradeComponent;

import java.text.DecimalFormat;
import java.util.List;

public class CourseDetailFragment extends BaseListFragment {

    private final GradeComponent TOTAL_GRADE_COMPONENT = new GradeComponent() {
        @Override
        public String getName() {
            return getString(R.string.total);
        }
    };
    private Callbacks mListener;
    private Course mCourse;
    private TextView mCourseCredits;
    private TextView mCourseName;
    private TextView mInstructorName;
    private TextView mInstructorEmail;
    private LinearLayout mTextFields;

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
        View rootView = inflater.inflate(R.layout.fragment_detail_course, container, false);

        mCourseCredits = (TextView) rootView.findViewById(R.id.tv_course_credits);
        mCourseName = (TextView) rootView.findViewById(R.id.tv_course_name);
        mInstructorName = (TextView) rootView.findViewById(R.id.tv_instructor_name);
        mInstructorEmail = (TextView) rootView.findViewById(R.id.tv_instructor_email);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_items);
        mTextFields = (LinearLayout) rootView.findViewById(R.id.layout_text_fields);

        rootView.findViewById(R.id.btn_edit_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) mListener.onEditCourse(mCourse);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setBackgroundColors();
        updateTextViews();
        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mListener != null)
                    mListener.onEditGrade((Grade) mListItems.get(position));
            }
        });
    }

    private void updateTextViews() {
        mCourseCredits.setText(
                Double.toString(mCourse.getCredits()) + " " + getString(R.string.course_credits));
        mCourseName.setText(mCourse.getName());

        String instructorName = mCourse.getInstructorName();
        String instructorEmail = mCourse.getInstructorEmail();

        // Set instructor name and email based on their availability.
        if (instructorName.length() > 0 && instructorEmail.length() > 0) {
            mInstructorName.setText(instructorName + " " + getString(R.string.bullet) + " ");
            mInstructorEmail.setText(instructorEmail);
            if (mInstructorName.getVisibility() == View.GONE
                    || mInstructorEmail.getVisibility() == View.GONE) {
                mInstructorName.setVisibility(View.VISIBLE);
                mInstructorEmail.setVisibility(View.VISIBLE);
            }
        } else {
            if (instructorName.isEmpty()) {
                mInstructorName.setVisibility(View.GONE);
            } else {
                if (mInstructorName.getVisibility() == View.GONE)
                    mInstructorName.setVisibility(View.VISIBLE);
                mInstructorName.setText(instructorName);
            }

            if (instructorEmail.isEmpty()) {
                mInstructorEmail.setVisibility(View.GONE);
            } else {
                if (mInstructorEmail.getVisibility() == View.GONE)
                    mInstructorEmail.setVisibility(View.VISIBLE);
                mInstructorEmail.setText(instructorEmail);
            }
        }
    }

    @Override
    public void updateListItems() {
        clearListItems();

        List<GradeComponent> gradeComponents = mDatabase.getGradeComponents(mCourse.getId());

        double totalWeight = 0;
        double totalAverage = 0;

        for (GradeComponent gradeComponent : gradeComponents) {
            mListItems.add(gradeComponent);

            // Calculate total weight for component thus far.
            double componentAverage = 0;
            double averageWeight = gradeComponent.getWeight() / gradeComponent.getNumberOfItems();
            List<Grade> grades = mDatabase.getComponentGrades(gradeComponent.getId());

            for (Grade grade : grades) {
                componentAverage +=
                        (grade.getPointsReceived() / grade.getPointsPossible()) * averageWeight;
                mListItems.add(grade);
            }

            gradeComponent.setComponentAverage(componentAverage);
            totalAverage += componentAverage;
            totalWeight += gradeComponent.getWeight();
        }
        TOTAL_GRADE_COMPONENT.setComponentAverage(totalAverage);
        TOTAL_GRADE_COMPONENT.setWeight(totalWeight);
        mListItems.add(TOTAL_GRADE_COMPONENT);

        notifyAdapter();
    }

    private void setBackgroundColors() {
        final int color = mCourse.getColor();

        // Set actionbar color to course color.
        getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(color));

        // Set background color to course color.
        mTextFields.setBackgroundColor(color);

    }

    /**
     * Gets the updated course from the database and refreshes the text views.
     */
    public void onCourseUpdated(Course course) {
        mCourse = course;
        setBackgroundColors();
        updateTextViews();
        updateListItems();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface Callbacks {

        /* Edit course. */
        public void onEditCourse(Course course);

        /* Edit grade. */
        public void onEditGrade(Grade grade);
    }

    private class ListAdapter extends BaseListAdapter {

        private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

        @Override
        public int getItemViewType(int position) {
            return getItem(position) instanceof GradeComponent ? ITEM_VIEW_TYPE_HEADER :
                    ITEM_VIEW_TYPE_MAIN_2_LINE;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final DatabaseItem item = mListItems.get(position);

            ViewHolder viewHolder;

            if (convertView == null) {

                viewHolder = new ViewHolder();

                if (item instanceof GradeComponent) {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_header_general, parent, false);
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title_header);
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_general_two_line, parent, false);

                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                    viewHolder.ivDetail = (ImageView) convertView.findViewById(R.id.iv_detail);
                    viewHolder.btnSecondary = convertView.findViewById(R.id.btn_secondary);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (item instanceof GradeComponent) {
                GradeComponent gradeComponent = (GradeComponent) item;

                viewHolder.tvTitle.setText(
                        gradeComponent.getName() + " "
                                + "("
                                + gradeComponent.getComponentAverageString()
                                + ")"
                );

            } else {
                Grade grade = (Grade) item;
                viewHolder.tvTitle.setText(grade.getName());
                viewHolder.tvSubtitle.setText(
                        grade.getGradePercentage() + " "
                                + getString(R.string.bullet) + " "
                                + grade.toString()
                );
                viewHolder.ivDetail.setImageResource(R.drawable.grade);

                // Set circle background based on course's color.
                viewHolder.ivDetail.setBackground(getColorCircle(R.color.theme_primary));

                // Set button to open popup menu.
                viewHolder.btnSecondary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopupMenu(view, R.menu.list_general, position);
                    }
                });
            }

            return convertView;
        }
    }
}
