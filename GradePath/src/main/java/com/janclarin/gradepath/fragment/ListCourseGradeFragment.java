package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.GradeComponent;

import java.util.List;

public class ListCourseGradeFragment extends BaseListFragment {

    private FragmentListCourseGradeListener mListener;

    // Selected Course object.
    private Course mCourse;

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
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourse = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_grade_empty);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onListCourseGradeAdd(mCourse);
            }
        });

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
                double componentWeight = component.getWeight() / component.getNumberOfItems();
                for (Grade grade : componentGrades) {
                    componentAverage +=
                            (grade.getPointsPossible() / grade.getPointsReceived()) * componentWeight;
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
                            .inflate(R.layout.fragment_list_header_course, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name_header);
                    viewHolder.tvGrade = (TextView) convertView.findViewById(R.id.tv_name_sub_header);
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_item_grade, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_grade_name);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_grade_subtitle);
                    viewHolder.tvGrade = (TextView) convertView.findViewById(R.id.tv_grade);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                GradeComponent gradeComponent = (GradeComponent) listItem;
                viewHolder.tvName.setText(gradeComponent.getName());
                viewHolder.tvGrade.setText(gradeComponent.getComponentAverageString());
            } else {
                Grade grade = (Grade) listItem;
                viewHolder.tvName.setText(grade.getName());
                viewHolder.tvSubtitle.setText(grade.getAddDate(mContext));
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
         * Called when a grade is going to be added.
         */
        public void onListCourseGradeAdd(Course course);

        /**
         * Called when a grade is going to be updated.
         */
        public void onListCourseGradeEdit(Grade grade);
    }
}
