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
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Reminder;

import java.util.List;

public class ListCourseReminderFragment extends BaseListFragment {

    private FragmentListCourseTaskListener mListener;

    // Selected Course object.
    private Course mCourse;

    public ListCourseReminderFragment() {
        // Required empty public constructor.
    }

    /**
     * Creates a new instance of this fragment.
     *
     * @return A new instance of fragment CourseDetailsFragment.
     */
    public static ListCourseReminderFragment newInstance(Course course) {
        ListCourseReminderFragment fragment = new ListCourseReminderFragment();
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
        mEmptyTextView.setText(R.string.tv_list_task_empty);

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    public void updateListItems() {
        clearListItems();

        long courseId = mCourse.getId();
        List<Reminder> incompleteReminders = mDatabase.getIncompleteReminders(courseId);
        List<Reminder> completedReminders = mDatabase.getCompletedReminders(courseId);

        // Add incomplete task header and tasks.
        if (incompleteReminders.size() > 0) {
            mListItems.add(new Header(mContext.getString(R.string.list_task_incomplete)));
            mListItems.addAll(incompleteReminders);
        }

        // Add complete task header and tasks.
        if (completedReminders.size() > 0) {
            mListItems.add(new Header(mContext.getString(R.string.list_task_complete)));
            mListItems.addAll(completedReminders);
        }

        notifyAdapter();

        // Determine list view state.
        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null)
            mListener.onListCourseReminderEdit((Reminder) mAdapter.getItem(selectedPosition));
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {
        int numItems = mListItems.size();
        for (int i = numItems - 1; i >= 0; i--) {
            if (possibleSelectedPositions.get(i, false)) {
                Reminder selectedReminder = (Reminder) mAdapter.getItem(i);
                mDatabase.deleteTask(selectedReminder);
                mListItems.remove(selectedReminder);
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
                            .inflate(R.layout.fragment_list_header_general, parent, false);
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
                viewHolder.tvName.setText(((Header) listItem).getName());
            } else {
                Reminder reminder = (Reminder) listItem;
                viewHolder.tvName.setText(reminder.getName());
                if (reminder.isCompleted()) {
                    viewHolder.tvName.setPaintFlags(
                            viewHolder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                viewHolder.tvDueDate.setText(reminder.getDueDate(mContext));
                viewHolder.cbCompleted.setChecked(reminder.isCompleted());
                viewHolder.cbCompleted.setOnCheckedChangeListener(new OnCompletedChangeListener(reminder));
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvDueDate;
            CheckBox cbCompleted;
        }

        private class OnCompletedChangeListener implements CompoundButton.OnCheckedChangeListener {

            private Reminder reminder;

            public OnCompletedChangeListener(Reminder reminder) {
                this.reminder = reminder;
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminder.setCompleted(isChecked);
                mDatabase.updateTask(reminder);
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
        public void onListCourseReminderAdd(Course course);

        /**
         * Called when a task is going to be updated.
         */
        public void onListCourseReminderEdit(Reminder reminder);
    }
}