package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Reminder;

import java.util.Collections;
import java.util.List;

public class ListReminderFragment extends BaseListFragment {

    private OnFragmentListTaskListener mListener;
    private LongSparseArray<Course> mCoursesById;

    public static ListReminderFragment newInstance() {
        return new ListReminderFragment();
    }

    public ListReminderFragment() {
        // Required empty public constructor.
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

        mCoursesById = new LongSparseArray<Course>();

        for (Course course : mDatabase.getCurrentCourses()) {
            mCoursesById.put(course.getId(), course);
        }

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mListener != null)
                    mListener.onListReminderEdit((Reminder) mListItems.get(position));
            }
        });
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get and sort reminder from database. Add upcoming reminder.
        List<Reminder> reminders = mDatabase.getUpcomingReminders();
        Collections.sort(reminders);
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

        /* Called when a task is going to be edited. */
        public void onListReminderEdit(Reminder reminder);
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        public int getItemViewType(int position) {
            return mListItems.get(position) instanceof Header ?
                    ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_MAIN_3_LINE;
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
                            .inflate(R.layout.list_header_general, parent, false);
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title_header);
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_general_three_line, parent, false);
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                    viewHolder.tvSubtitle2 = (TextView) convertView.findViewById(R.id.tv_subtitle_2);
                    viewHolder.ivDetail = (ImageView) convertView.findViewById(R.id.iv_detail);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvTitle.setText(((Header) listItem).getName());
            } else {
                Reminder reminder = (Reminder) listItem;
                viewHolder.tvTitle.setText(reminder.getName());
                viewHolder.tvSubtitle.setText(mCoursesById.get(reminder.getCourseId()).getName());
                viewHolder.tvSubtitle2.setText(
                        reminder.getDateString(mContext) + " "
                                + getString(R.string.bullet) + " "
                                + reminder.getTimeString());
                viewHolder.ivDetail.setBackground(getColorCircle(R.color.theme_primary));
                viewHolder.ivDetail.setImageResource(R.drawable.reminder);
            }

            return convertView;
        }
    }
}
