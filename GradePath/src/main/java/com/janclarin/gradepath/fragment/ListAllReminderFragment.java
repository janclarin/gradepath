package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ListAllReminderFragment extends BaseListFragment {

    private OnFragmentListTaskListener mListener;
    private LongSparseArray<Course> mCoursesById;

    public static ListAllReminderFragment newInstance() {
        return new ListAllReminderFragment();
    }

    public ListAllReminderFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
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
        mEmptyTextView.setText(R.string.tv_list_task_empty);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onListReminderNew();
            }
        });

        mCoursesById = new LongSparseArray<Course>();
        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get list of current courses.
        List<Course> courses = mDatabase.getCurrentCourses();
        List<Reminder> reminders = new ArrayList<Reminder>();

        for (Course course : courses) {
            reminders.addAll(mDatabase.getReminders(course.getId()));
            mCoursesById.put(course.getId(), course);
        }

        Collections.sort(reminders);

        Calendar today = Calendar.getInstance();

        mListItems.addAll(reminders);

        notifyAdapter();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null)
            mListener.onListReminderEdit((Reminder) mAdapter.getItem(selectedPosition));
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
        public void onListReminderNew();

        /* Called when a task is going to be edited. */
        public void onListReminderEdit(Reminder reminder);
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        public int getItemViewType(int position) {
            return mListItems.get(position) instanceof Header ?
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
                            .inflate(R.layout.fragment_list_item_reminder, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_reminder_name);
                    viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tv_reminder_date);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_reminder_subtitle);
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
                viewHolder.tvDate.setText(reminder.getDueDate(mContext));
                viewHolder.tvSubtitle.setText(mDatabase.getCourse(reminder.getCourseId()).getName());
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvSubtitle;
            TextView tvDate;
        }
    }
}
