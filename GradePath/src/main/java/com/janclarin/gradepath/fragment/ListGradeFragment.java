package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Grade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListGradeFragment extends BaseListFragment {

    private OnFragmentListGradeListener mListener;
    private LongSparseArray<Course> mCoursesById;

    public static ListGradeFragment newInstance() {
        return new ListGradeFragment();
    }

    public ListGradeFragment() {
        // Required empty public constructor.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_grade_empty);

        mCoursesById = new LongSparseArray<Course>();
        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        // Set on item click listener to notify listener to open course detail activity into grades.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Grade grade = (Grade) mAdapter.getItem(position);
                if (mListener != null) {
                    mListener.onListGradeClick(grade, mCoursesById.get(grade.getCourseId()));
                }
            }
        });
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get list of current courses.
        List<Course> courses = mDatabase.getCurrentCourses();
        List<Grade> grades = new ArrayList<Grade>();

        for (Course course : courses) {
            grades.addAll(mDatabase.getGrades(course.getId()));
            mCoursesById.put(course.getId(), course);
        }

        Collections.sort(grades);

        mListItems.addAll(grades);

        notifyAdapter();

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
            mListener = (OnFragmentListGradeListener) activity;
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
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final DatabaseItem listItem = mListItems.get(position);
            final int type = getItemViewType(position);

            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.fragment_list_item_general, parent, false);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                viewHolder.tvGrade = (TextView) convertView.findViewById(R.id.tv_information);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Grade grade = (Grade) listItem;
            viewHolder.tvName.setText(grade.getName());
            viewHolder.tvSubtitle.setText(mCoursesById.get(grade.getCourseId()).getName());
            viewHolder.tvGrade.setText(grade.getGradePercentage());

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvSubtitle;
            TextView tvGrade;
        }
    }

    public interface OnFragmentListGradeListener {

        /* Called when a grade is selected for edit in contextual action bar */
        public void onListGradeEdit(Grade grade);

        /* Called when a grade is clicked in list. Opens course detail fragment. */
        public void onListGradeClick(Grade grade, Course course);
    }
}