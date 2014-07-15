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
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Grade;

import java.util.ArrayList;
import java.util.List;

public class ListGradeFragment extends BaseListFragment<DatabaseItem> {

    private static final int ITEM_VIEW_TYPE_COURSE = 0;
    private static final int ITEM_VIEW_TYPE_GRADE = 1;
    private static final int NUM_ITEM_VIEW_TYPES = 2;

    private FragmentListGradeListener mListener;

    public static ListGradeFragment newInstance() {
        return new ListGradeFragment();
    }

    public ListGradeFragment() {
        // Required empty public constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View convertView = inflater.inflate(R.layout.fragment_list_grade, container, false);

        mEmptyTextView = (TextView) convertView.findViewById(R.id.tv_list_grade_empty);
        mListView = (ListView) convertView.findViewById(R.id.lv_list_grade);

        return convertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateListItems();

        mAdapter = new ListAdapter();
        setUpListView();

        showEmptyStateView(mListItems.isEmpty());

    }

    @Override
    public void updateListItems() {
        // Get list of current courses.
        List<Course> courses = mDatabase.getCurrentCourses();

        // Reset list items and populate the list.
        try {
            mListItems.clear();
        } catch (NullPointerException e) {
            mListItems = new ArrayList<DatabaseItem>();
        }

        for (Course course : courses) {
            List<Grade> grades = mDatabase.getGrades(course.getId());

            if (grades.size() > 0) {
                mListItems.add(course);
                mListItems.addAll(grades);
            }
        }
        if (mAdapter != null) mAdapter.notifyDataSetChanged();

        // Determine list view state.
        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null)
            mListener.onListGradeEdit((Grade) mAdapter.getItem(selectedPosition));
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {
        for (int i = 0; i < mListItems.size(); i++) {
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
            mListener = (FragmentListGradeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListGradeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        public int getViewTypeCount() {
            return NUM_ITEM_VIEW_TYPES;
        }

        @Override
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof Course) ?
                    ITEM_VIEW_TYPE_COURSE : ITEM_VIEW_TYPE_GRADE;
        }

        @Override
        public boolean isEnabled(int position) {
            // Only enable grades.
            return getItemViewType(position) == ITEM_VIEW_TYPE_GRADE;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final int type = getItemViewType(position);

            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                if (type == ITEM_VIEW_TYPE_COURSE) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_list_header, null);
                    viewHolder.tvName = (TextView) convertView;
                } else {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_list_grade_item, null);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_grade_name);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_grade_subtitle);
                    viewHolder.tvGrade = (TextView) convertView.findViewById(R.id.tv_grade);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_COURSE) {
                viewHolder.tvName.setText(((Course) mListItems.get(position)).getName());
            } else {
                Grade grade = (Grade) mListItems.get(position);
                viewHolder.tvName.setText(grade.getName());
                viewHolder.tvSubtitle.setText(
                        mDatabase.getGradeComponent(grade.getComponentId()).getName() + " "
                                + getString(R.string.bullet) + " "
                                + grade.getAddDate(mContext)
                );
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

    public interface FragmentListGradeListener {

        /**
         * Called when a grade is going to be updated.
         */
        public void onListGradeEdit(Grade grade);
    }
}