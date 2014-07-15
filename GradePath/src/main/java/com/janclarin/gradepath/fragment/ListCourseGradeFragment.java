package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Course fragment to display course information, grades, etc.
 */
public class ListCourseGradeFragment extends BaseListFragment<DatabaseItem> {

    private static final int ITEM_VIEW_TYPE_COMPONENT = 0;
    private static final int ITEM_VIEW_TYPE_GRADE = 1;
    private static final int NUM_ITEM_VIEW_TYPES = 2;

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

    // TODO: Show average for each component.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourse = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_course_grades, container, false);

        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_course_grade_empty);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_course_grade);

        return rootView;
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
    protected void updateListItems() {
        // Get list of grade components for a course.
        List<GradeComponent> components = mDatabase.getGradeComponents(mCourse.getId());

        // Reset list items and populate the list.
        try {
            mListItems.clear();
        } catch (NullPointerException e) {
            mListItems = new ArrayList<DatabaseItem>();
        }

        for (GradeComponent component : components) {
            List<Grade> componentGrades = mDatabase.getComponentGrades(component.getId());

            if (componentGrades.size() > 0) {
                mListItems.add(component);
                mListItems.addAll(componentGrades);
            }
        }
        if (mAdapter != null) mAdapter.notifyDataSetChanged();

        // Determine list view state.
        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {

    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {

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
        public int getViewTypeCount() {
            return NUM_ITEM_VIEW_TYPES;
        }

        @Override
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof GradeComponent) ?
                    ITEM_VIEW_TYPE_COMPONENT : ITEM_VIEW_TYPE_GRADE;
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

                if (type == ITEM_VIEW_TYPE_COMPONENT) {
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

            if (type == ITEM_VIEW_TYPE_COMPONENT) {
                viewHolder.tvName.setText(((GradeComponent) mListItems.get(position)).getName());
            } else {
                Grade grade = (Grade) mListItems.get(position);
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

        public void onListCourseGradeAdd(Course course);
    }
}
