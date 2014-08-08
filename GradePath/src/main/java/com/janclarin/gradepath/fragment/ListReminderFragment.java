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
        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get and sort reminders from database. Add upcoming reminders.
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
                            .inflate(R.layout.list_item_general, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                    viewHolder.tvInfo = (TextView) convertView.findViewById(R.id.tv_information);
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
                viewHolder.tvSubtitle.setText(
                        reminder.getDateString(mContext) + ", "
                                + reminder.getTimeString() + " "
                                + getString(R.string.bullet) + " "
                                + mCoursesById.get(reminder.getCourseId()).getName());
                viewHolder.tvInfo.setText(reminder.getTypeString(mContext));
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvSubtitle;
            TextView tvInfo;
        }
    }
}
