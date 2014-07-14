package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Task;

import java.util.ArrayList;
import java.util.List;

public class ListTaskFragment extends BaseListFragment<DatabaseItem> {

    private static final int TITLE_ID = R.string.title_fragment_list_tasks;
    private static final int ITEM_VIEW_TYPE_COURSE = 0;
    private static final int ITEM_VIEW_TYPE_TASK = 1;
    private static final int NUM_ITEM_VIEW_TYPES = 2;

    private FragmentListTaskListener mListener;

    public static ListTaskFragment newInstance() {
        return new ListTaskFragment();
    }

    public ListTaskFragment() {
        // Required empty public constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View convertView = inflater.inflate(R.layout.fragment_list_task, container, false);

        // Find views.
        mEmptyTextView = (TextView) convertView.findViewById(R.id.tv_list_task_empty);
        mListView = (ListView) convertView.findViewById(R.id.lv_list_task);

        return convertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateListItems();
        initAdapter();
        setUpListView();

    }

    @Override
    protected void initAdapter() {
        mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mListItems.size();
            }

            @Override
            public Object getItem(int position) {
                return mListItems.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public int getViewTypeCount() {
                return NUM_ITEM_VIEW_TYPES;
            }

            @Override
            public int getItemViewType(int position) {
                return (mListItems.get(position) instanceof Course) ?
                        ITEM_VIEW_TYPE_COURSE : ITEM_VIEW_TYPE_TASK;
            }

            @Override
            public boolean isEnabled(int position) {
                // Only enable tasks.
                return getItemViewType(position) == ITEM_VIEW_TYPE_TASK;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                final int type = getItemViewType(position);

                ViewHolder viewHolder;

                if (convertView == null) {
                    viewHolder = new ViewHolder();

                    if (type == ITEM_VIEW_TYPE_COURSE) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_list_header_course, null);
                        viewHolder.tvName = (TextView) convertView;
                    } else {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_list_task_item, null);
                        viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_task_name);
                        viewHolder.tvDueDate = (TextView) convertView.findViewById(R.id.tv_task_due_date);
                        viewHolder.cbCompleted = (CheckBox) convertView.findViewById(R.id.cb_task_completed);
                    }

                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                if (type == ITEM_VIEW_TYPE_COURSE) {
                    viewHolder.tvName.setText(((Course) mListItems.get(position)).getName());
                } else {
                    Task task = (Task) mListItems.get(position);
                    viewHolder.tvName.setText(task.getName());
                    viewHolder.tvDueDate.setText(task.getDueDate(mContext));
                    viewHolder.tvDueDate.setTextColor(task.getUrgencyColor(mContext));
                    viewHolder.cbCompleted.setChecked(task.isCompleted());
                    viewHolder.cbCompleted.setOnCheckedChangeListener(new OnCompletedChangeListener(task));
                }

                return convertView;
            }

            class ViewHolder {
                TextView tvName;
                TextView tvDueDate;
                CheckBox cbCompleted;
            }

            class OnCompletedChangeListener implements CompoundButton.OnCheckedChangeListener {

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
        };
    }

    @Override
    protected void updateListItems() {
        // Get list of current courses.
        List<Course> courses = mDatabase.getCurrentCourses();

        // Reset list items and populate the list.
        try {
            mListItems.clear();
        } catch (NullPointerException e) {
            // Initialize list.yy
            mListItems = new ArrayList<DatabaseItem>();
        }

        for (Course course : courses) {
            List<Task> tasks = mDatabase.getTasks(course.getId());

            if (tasks.size() > 0) {
                mListItems.add(course);
                mListItems.addAll(tasks);
            }
        }
        if (mAdapter != null) mAdapter.notifyDataSetChanged();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null) mListener.onListTaskEdit((Task) mAdapter.getItem(selectedPosition));
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {
        for (int i = 0; i < mListItems.size(); i++) {
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
            mListener = (FragmentListTaskListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListTaskListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentListTaskListener {

        /**
         * Called when a task is going to be edited.
         */
        public void onListTaskEdit(Task task);
    }
}
