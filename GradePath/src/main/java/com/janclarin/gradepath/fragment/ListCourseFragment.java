package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Semester;

import java.util.Collections;
import java.util.List;

/**
 * Course list fragment.
 */
public class ListCourseFragment extends BaseListFragment {

    private OnFragmentListCourseListener mListener;

    public static ListCourseFragment newInstance() {
        return new ListCourseFragment();
    }

    public ListCourseFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_course_empty);

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        // Set on item click listener to display course info.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course course = (Course) mAdapter.getItem(position);
                if (mListener != null) mListener.onListCourseViewDetails(course);
            }
        });
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get list of Semesters.
        List<Semester> semesters = mDatabase.getSemesters();
        Semester currentSemester = mDatabase.getCurrentSemester();

        // For all semesters that contain Courses, add the Semester and Courses to the list.
        for (Semester semester : semesters) {
            List<Course> courses = mDatabase.getCourses(semester.getId());
            if (courses.size() > 0) {
                // Only add title if it's a past semester.
                if (!semester.equals(currentSemester)) mListItems.add(semester);
                Collections.sort(courses);
                mListItems.addAll(courses);
            }
        }

        notifyAdapter();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null) {
            mListener.onListCourseEdit((Course) mAdapter.getItem(selectedPosition));
        }
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray selectedPositions) {
        int numItems = mListItems.size();
        for (int i = numItems - 1; i >= 0; i--) {
            if (selectedPositions.get(i, false)) {
                Course selectedCourse = (Course) mAdapter.getItem(i);
                if (mListener != null) mListener.onListCourseDelete(selectedCourse);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentListCourseListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListCourseCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Interface to be implemented by all listener classes.
     */
    public interface OnFragmentListCourseListener {

        /* Called when the course item is clicked. Opens course detail fragment. */
        public void onListCourseViewDetails(Course course);

        /* Called when the contextual action bar edit button is clicked. */
        public void onListCourseEdit(Course course);

        /* Called when the contextual action bar delete button is clicked. */
        public void onListCourseDelete(Course course);
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof Semester) ?
                    ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_MAIN;
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
                            .inflate(R.layout.fragment_list_header_general, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name_header);
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_course, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvName.setText(((Semester) listItem).toString());
            } else {
                Course course = (Course) listItem;
                viewHolder.tvName.setText(course.getName());
                viewHolder.tvSubtitle.setText(course.getInstructorName());
            }

            return convertView;
        }

        /**
         * View holder class for course item layout.
         */
        private class ViewHolder {
            TextView tvName;
            TextView tvSubtitle;
        }
    }
}

