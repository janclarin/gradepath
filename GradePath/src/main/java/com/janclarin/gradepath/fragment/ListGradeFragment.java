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

import java.util.List;

public class ListGradeFragment extends BaseListFragment {

    private OnFragmentListGradeListener mListener;

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
        clearListItems();

        // Get list of current courses.
        List<Course> courses = mDatabase.getCurrentCourses();

        for (Course course : courses) {
            List<Grade> grades = mDatabase.getGrades(course.getId());

            if (grades.size() > 0) {
                mListItems.add(course);
                mListItems.addAll(grades);
            }
        }

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
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof Course) ?
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
                            .inflate(R.layout.fragment_list_header, parent, false);
                    viewHolder.tvName = (TextView) convertView;
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_grade_item, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_grade_name);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_grade_subtitle);
                    viewHolder.tvGrade = (TextView) convertView.findViewById(R.id.tv_grade);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvName.setText(((Course) listItem).getName());
            } else {
                Grade grade = (Grade) listItem;
                viewHolder.tvName.setText(grade.getName());
                viewHolder.tvSubtitle.setText(
                        grade.getAddDate(mContext) + " "
                                + getString(R.string.bullet) + " "
                                + mDatabase.getGradeComponent(grade.getComponentId()).getName()
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

    public interface OnFragmentListGradeListener {

        /**
         * Called when a grade is going to be updated.
         */
        public void onListGradeEdit(Grade grade);
    }
}