package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Task;

import java.util.List;

public class ListCourseTaskFragment extends BaseListFragment {

    private FragmentListCourseTaskListener mListener;

    // Selected Course object.
    private Course mCourse;

    public ListCourseTaskFragment() {
        // Required empty public constructor.
    }

    /**
     * Creates a new instance of this fragment.
     *
     * @return A new instance of fragment CourseDetailsFragment.
     */
    public static ListCourseTaskFragment newInstance(Course course) {
        ListCourseTaskFragment fragment = new ListCourseTaskFragment();
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail_course_list_task, container, false);

        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_course_task_empty);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_course_task);

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
    public void updateListItems() {
        clearListItems();

        long courseId = mCourse.getId();
        List<Task> incompleteTasks = mDatabase.getIncompleteTasks(courseId);
        List<Task> completedTasks = mDatabase.getCompletedTasks(courseId);

        // Add incomplete task header and tasks.
        if (incompleteTasks.size() > 0) {
            mListItems.add(new Header(mContext.getString(R.string.list_task_incomplete)));
            mListItems.addAll(incompleteTasks);
        }

        // Add complete task header and tasks.
        if (completedTasks.size() > 0) {
            mListItems.add(new Header(mContext.getString(R.string.list_task_complete)));
            mListItems.addAll(completedTasks);
        }

        notifyAdapter();

        // Determine list view state.
        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null)
            mListener.onListCourseTaskEdit((Task) mAdapter.getItem(selectedPosition));
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {
        int numItems = mListItems.size();
        for (int i = numItems - 1; i >= 0; i--) {
            if (possibleSelectedPositions.get(i, false)) {
                Task selectedTask = (Task) mAdapter.getItem(i);
                mDatabase.deleteTask(selectedTask);
                mListItems.remove(selectedTask);
            }
        }
        updateListItems();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentListCourseTaskListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListCourseTaskListener");
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
            return (mListItems.get(position) instanceof Header) ?
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
                            .inflate(R.layout.fragment_list_task_item, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_task_name);
                    viewHolder.tvDueDate = (TextView) convertView.findViewById(R.id.tv_task_due_date);
                    viewHolder.cbCompleted = (CheckBox) convertView.findViewById(R.id.cb_task_completed);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvName.setText(((Header) listItem).getName());
            } else {
                Task task = (Task) listItem;
                viewHolder.tvName.setText(task.getName());
                if (task.isCompleted()) {
                    viewHolder.tvName.setPaintFlags(
                            viewHolder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                viewHolder.tvDueDate.setText(task.getDueDate(mContext));
                viewHolder.cbCompleted.setChecked(task.isCompleted());
                viewHolder.cbCompleted.setOnCheckedChangeListener(new OnCompletedChangeListener(task));
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvDueDate;
            CheckBox cbCompleted;
        }

        private class OnCompletedChangeListener implements CompoundButton.OnCheckedChangeListener {

            private Task task;

            public OnCompletedChangeListener(Task task) {
                this.task = task;
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                task.setCompleted(isChecked);
                mDatabase.updateTask(task);
            }
        }
    }

    /**
     * Listeners.
     */
    public static interface FragmentListCourseTaskListener {

        /**
         * Called when a task is going to be added.
         */
        public void onListCourseTaskAdd(Course course);

        /**
         * Called when a task is going to be updated.
         */
        public void onListCourseTaskEdit(Task task);
    }
}
