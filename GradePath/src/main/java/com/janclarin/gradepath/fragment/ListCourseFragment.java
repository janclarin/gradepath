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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Semester;
import com.janclarin.gradepath.model.Task;

import java.util.List;

/**
 * Course list fragment.
 */
public class ListCourseFragment extends BaseListFragment {

    private OnFragmentListCourseListener mListener;
    private TextView mSemesterTextView;

    public static ListCourseFragment newInstance() {
        return new ListCourseFragment();
    }

    public ListCourseFragment() {
        // Required empty public constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_course, container, false);

        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_course_empty);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_course);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        // Set on item click listener to display course info.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course course = (Course) parent.getAdapter().getItem(position);
                if (mListener != null) mListener.onListCourseViewDetails(course);
            }
        });
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get current semester.
        Semester currentSemester = mDatabase.getCurrentSemester();

        // If there is a current semester.
        if (currentSemester != null) {
            List<Course> courses = mDatabase.getCourses(currentSemester.getId());
            if (courses.size() > 0) {
                // Add all current courses.
                mListItems.add(currentSemester);
                mListItems.addAll(mDatabase.getCourses(currentSemester.getId()));
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
                mListItems.remove(selectedCourse);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate options menu if it hasn't been already.
        if (!menu.hasVisibleItems()) inflater.inflate(R.menu.list_course, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: Handle menu options.
        switch (item.getItemId()) {
            case R.id.action_list_course_sort:
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Interface to be implemented by all listener classes.
     */
    public interface OnFragmentListCourseListener {

        /**
         * Called when the add grade button is clicked under a course.
         */
        void onListCourseAddGrade(Course course);

        /**
         * Called when the add task button is clicked under a course.
         */
        void onListCourseAddTask(Course course);

        /**
         * Called when the course item is clicked. Opens course detail fragment.
         */
        void onListCourseViewDetails(Course course);

        /**
         * Called when the contextual action bar edit button is clicked.
         */
        void onListCourseEdit(Course course);

        /**
         * Called when the contextual action bar delete button is clicked.
         */
        void onListCourseDelete(Course course);
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof Semester) ?
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
                            .inflate(R.layout.fragment_list_header_semester, parent, false);
                    viewHolder.tvName = (TextView) convertView;
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_course_item, parent, false);
                    viewHolder.vCourseColor = convertView.findViewById(R.id.view_course_color);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_course_name);
                    viewHolder.tvNextDueDate = (TextView) convertView.findViewById(R.id.tv_next_due_date);
                    viewHolder.vHorizontalDivider = convertView.findViewById(R.id.view_course_item_divider_horizontal);
                    viewHolder.btnShowButtonBar = (ImageButton) convertView.findViewById(R.id.btn_course_show_button_bar);
                    viewHolder.llButtonBar = (LinearLayout) convertView.findViewById(R.id.ll_button_bar);
                    viewHolder.btnAddGrade = (Button) convertView.findViewById(R.id.btn_bar_add_grade);
                    viewHolder.btnAddTask = (Button) convertView.findViewById(R.id.btn_bar_add_task);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvName.setText(((Semester) listItem).toString());
            } else {
                Course course = (Course) listItem;
                Task upcomingTask = mDatabase.getUpcomingTask(course.getId());

                String upcomingTaskText;
                int urgencyColorId;
                if (upcomingTask == null) {
                    upcomingTaskText = mContext.getString(R.string.task_due_date_none);

                    urgencyColorId = R.color.course_urgency_0;
                } else {
                    upcomingTaskText = upcomingTask.getDueDate(mContext) + " "
                            + mContext.getString(R.string.bullet) + " "
                            + upcomingTask.getName();

                    urgencyColorId = upcomingTask.getUrgencyColor(mContext);
                }

                viewHolder.vCourseColor.setBackgroundResource(urgencyColorId);
                viewHolder.tvName.setText(course.getName());
                viewHolder.tvNextDueDate.setText(upcomingTaskText);
                viewHolder.tvNextDueDate.setTextColor(getResources().getColor(urgencyColorId));

                // Set on click listeners.
                viewHolder.btnShowButtonBar
                        .setOnClickListener(
                                new OnShowButtonBarClickListener(viewHolder.llButtonBar,
                                        viewHolder.vHorizontalDivider)
                        );
                viewHolder.btnAddGrade.setOnClickListener(new OnAddGradeClickListener(course));
                viewHolder.btnAddTask.setOnClickListener(new OnAddTaskClickListener(course));
            }

            return convertView;
        }

        /**
         * View holder class for course item layout.
         */
        private class ViewHolder {
            View vCourseColor;
            TextView tvName;
            TextView tvNextDueDate;
            ImageButton btnShowButtonBar;
            View vHorizontalDivider;
            LinearLayout llButtonBar;
            Button btnAddGrade;
            Button btnAddTask;
        }

        /**
         * Implements OnClickListener for course color button. Notifies listener to open
         * Course detail activity.
         */
        private class OnShowButtonBarClickListener implements View.OnClickListener {

            LinearLayout buttonBarLayout;
            View viewDivider;

            public OnShowButtonBarClickListener(LinearLayout buttonBarLayout, View viewDivider) {
                this.buttonBarLayout = buttonBarLayout;
                this.viewDivider = viewDivider;
            }

            public void onClick(View view) {
                if (this.buttonBarLayout.getVisibility() == View.GONE) {
                    this.buttonBarLayout.setVisibility(View.VISIBLE);
                    this.viewDivider.setVisibility(View.VISIBLE);
                } else {
                    this.buttonBarLayout.setVisibility(View.GONE);
                    this.viewDivider.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * Implements OnClickListener for add grade button. Notifies listener to add grade.
     */
    private class OnAddGradeClickListener implements View.OnClickListener {

        private final Course course;

        public OnAddGradeClickListener(Course course) {
            this.course = course;
        }

        public void onClick(View view) {
            if (mListener != null) mListener.onListCourseAddGrade(course);
        }

    }

    /**
     * Implements OnClickListener for add task button. Notifies listener to add task.
     */
    private class OnAddTaskClickListener implements View.OnClickListener {

        private final Course course;

        public OnAddTaskClickListener(Course course) {
            this.course = course;
        }

        public void onClick(View view) {
            if (mListener != null) mListener.onListCourseAddTask(course);
        }
    }
}

