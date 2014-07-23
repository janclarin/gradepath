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
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Reminder;

import java.util.ArrayList;
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
        List<Reminder> allReminders = mDatabase.getReminders(courseId);
        List<Reminder> currentReminders = new ArrayList<Reminder>();
        List<Reminder> pastReminders = new ArrayList<Reminder>();

        // Add incomplete task header and tasks.
        if (pastReminders.size() > 0) {
            mListItems.add(new Header(mContext.getString(R.string.list_reminders_past)));
            mListItems.addAll(pastReminders);
        }

        // Add complete task header and tasks.
        if (currentReminders.size() > 0) {
            mListItems.add(new Header(mContext.getString(R.string.list_reminders_current)));
            mListItems.addAll(currentReminders);
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
                mDatabase.deleteReminder(selectedReminder);
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
            return getItem(position) instanceof Header ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_DATABASE_ITEM;
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
                            .inflate(R.layout.fragment_list_item_reminder, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_reminder_name);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_reminder_subtitle);
                    viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tv_reminder_date);
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
                viewHolder.tvSubtitle.setText(reminder.isGraded() ? R.string.graded : R.string.not_graded);
                viewHolder.tvDate.setText(reminder.getDueDate(mContext));
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvSubtitle;
            TextView tvDate;
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
