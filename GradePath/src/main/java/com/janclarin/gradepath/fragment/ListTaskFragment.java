package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Task;

import java.util.List;

public class ListTaskFragment extends BaseListFragment {

    private OnFragmentListTaskListener mListener;

    public static ListTaskFragment newInstance() {
        return new ListTaskFragment();
    }

    public ListTaskFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_task_empty);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onListTaskNew();
            }
        });

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get list of current courses.
        List<Course> courses = mDatabase.getCurrentCourses();

        for (Course course : courses) {
            List<Task> tasks = mDatabase.getTasks(course.getId());

            if (tasks.size() > 0) {
                mListItems.add(course);
                mListItems.addAll(tasks);
            }
        }

        notifyAdapter();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null) mListener.onListTaskEdit((Task) mAdapter.getItem(selectedPosition));
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
            mListener = (OnFragmentListTaskListener) activity;
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

    public interface OnFragmentListTaskListener {

        /* Called when the add item button is clicked. */
        public void onListTaskNew();

        /* Called when a task is going to be edited. */
        public void onListTaskEdit(Task task);
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
                            .inflate(R.layout.fragment_list_general_header, parent, false);
                    viewHolder.tvName = (TextView) convertView;
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_item_task, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_task_name);
                    viewHolder.tvDueDate = (TextView) convertView.findViewById(R.id.tv_task_due_date);
                    viewHolder.cbCompleted = (CheckBox) convertView.findViewById(R.id.cb_task_completed);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvName.setText(((Course) listItem).getName());
            } else {
                Task task = (Task) listItem;
                viewHolder.tvName.setText(task.getName());
                if (task.isCompleted()) {
                    viewHolder.tvName.setPaintFlags(
                            viewHolder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                viewHolder.tvDueDate.setText(task.getDueDate(mContext));
                viewHolder.tvDueDate.setTextColor(getResources().getColor(task.getUrgencyColor(mContext)));
                viewHolder.cbCompleted.setChecked(task.isCompleted());
                viewHolder.cbCompleted.setOnCheckedChangeListener(new OnCompletedChangeListener(task,
                        viewHolder.tvName));
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
            private TextView textView;

            public OnCompletedChangeListener(Task task, TextView textView) {
                this.task = task;
                this.textView = textView;
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                task.setCompleted(isChecked);
                mDatabase.updateTask(task);
                textView.setPaintFlags(isChecked ? textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                        : textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }
}
