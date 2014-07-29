package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.GradeComponent;

import java.util.List;

public class CourseListGradeFragment extends BaseListFragment {

    private FragmentListCourseGradeListener mListener;

    // Selected Course object.
    private Course mCourse;

    public CourseListGradeFragment() {
        // Required empty public constructor.
    }

    /**
     * Creates a new instance of this fragment.
     *
     * @return A new instance of fragment CourseDetailsFragment.
     */
    public static CourseListGradeFragment newInstance(Course course) {
        CourseListGradeFragment fragment = new CourseListGradeFragment();
        Bundle args = new Bundle();
        args.putSerializable(MainActivity.COURSE_KEY, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourse = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_course_general, container, false);

        mListView = (ListView) rootView.findViewById(R.id.lv_list_items);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_empty);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_grade_empty);

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get list of grade components for a course.
        List<GradeComponent> components = mDatabase.getGradeComponents(mCourse.getId());

        for (GradeComponent component : components) {
            List<Grade> componentGrades = mDatabase.getComponentGrades(component.getId());

            if (componentGrades.size() > 0) {
                // Calculate current grade for component and set the value.
                double componentAverage = 0;
                double gradeWeight = component.getWeight() / component.getNumberOfItems();
                for (Grade grade : componentGrades) {
                    componentAverage +=
                            (grade.getPointsReceived() / grade.getPointsPossible()) * gradeWeight;
                }
                component.setComponentAverage(componentAverage);
                mListItems.add(component);
                mListItems.addAll(componentGrades);
            }
        }

        notifyAdapter();

        // Determine list view state.
        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null)
            mListener.onListCourseGradeEdit((Grade) mAdapter.getItem(selectedPosition));
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {
        int numItems = mListItems.size();
        for (int i = numItems - 1; i >= 0; i--) {
            if (possibleSelectedPositions.get(i, false)) {
                Grade selectedGrade = (Grade) mAdapter.getItem(i);
                mDatabase.deleteGrade(selectedGrade);
                mListItems.remove(selectedGrade);
            }
        }
        updateListItems();
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

    private class ListAdapter extends BaseListAdapter {
        @Override
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof GradeComponent) ?
                    ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_DATABASE_ITEM;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final DatabaseItem listItem = mListItems.get(position);
            final int type = getItemViewType(position);

            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                if (type == ITEM_VIEW_TYPE_HEADER) {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_header_general_two, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name_header);
                    viewHolder.tvGrade = (TextView) convertView.findViewById(R.id.tv_name_sub_header);
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_item_grade, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                    viewHolder.tvGrade = (TextView) convertView.findViewById(R.id.tv_information);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                GradeComponent gradeComponent = (GradeComponent) listItem;
                viewHolder.tvName.setText(gradeComponent.getName()
                        + " (" + Integer.toString(gradeComponent.getNumberOfItems()) + ")");
                viewHolder.tvGrade.setText(gradeComponent.getComponentAverageString());
            } else {
                Grade grade = (Grade) listItem;
                viewHolder.tvName.setText(grade.getName());
                viewHolder.tvSubtitle.setText(grade.toString());
                viewHolder.tvGrade.setText(grade.getGradePercentage());
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvSubtitle;
            TextView tvGrade;
        }
    }

    /**
     * Listeners.
     */
    public static interface FragmentListCourseGradeListener {
        /**
         * Called when a grade is going to be updated.
         */
        public void onListCourseGradeEdit(Grade grade);
    }
}
