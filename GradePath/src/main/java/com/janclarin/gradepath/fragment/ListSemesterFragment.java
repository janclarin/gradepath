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
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Semester;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Fragment with list of list_course that allows for editing and sets option for a new course.
 */
public class ListSemesterFragment extends BaseListFragment {

    private OnFragmentListSemesterListener mListener;

    public static ListSemesterFragment newInstance() {
        return new ListSemesterFragment();
    }

    public ListSemesterFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_semester_empty);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onListSemesterNew();
            }
        });

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();
    }

    @Override
    public void updateListItems() {
        clearListItems();

        List<Semester> semesters = mDatabase.getSemesters();
        Semester currentSemester = mDatabase.getCurrentSemester();

        // Add current semester header and semester into list if they exist.
        if (currentSemester != null) {
            mListItems.add(new Header(getString(R.string.semester_current)));
            mListItems.add(currentSemester);

            // Remove current semester from list of all semesters.
            semesters.remove(currentSemester);
        }

        // Add all other semesters under "Past" header.
        if (semesters.size() > 0) {
            mListItems.add(new Header(getString(R.string.semester_past)));
            mListItems.addAll(semesters);
        }

        notifyAdapter();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null)
            mListener.onListSemesterEdit((Semester) mAdapter.getItem(selectedPosition));
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {
        int numItems = mListItems.size();
        for (int i = numItems - 1; i >= 0; i--) {
            if (possibleSelectedPositions.get(i, false)) {
                Semester selectedSemester = (Semester) mAdapter.getItem(i);
                if (mListener != null) {
                    mListener.onListSemesterDelete(selectedSemester);
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentListSemesterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListSemesterCallbacks");
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
        if (!menu.hasVisibleItems()) inflater.inflate(R.menu.list_semester, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Provides an interface to alert listeners.
     */
    public interface OnFragmentListSemesterListener {

        /* Called when add item button is clicked. */
        public void onListSemesterNew();

        /* Called when contextual action bar edit button is clicked. */
        public void onListSemesterEdit(Semester semester);

        /* Called when contextual action bar delete button is clicked. */
        public void onListSemesterDelete(Semester semester);
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
            final ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                if (type == ITEM_VIEW_TYPE_HEADER) {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_header_general_two, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name_header);
                    viewHolder.tvInformation = (TextView) convertView.findViewById(R.id.tv_name_sub_header);
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_item_semester, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_semester_name);
                    viewHolder.tvInformation =
                            (TextView) convertView.findViewById(R.id.tv_semester_information);
                    viewHolder.tvInformationLabel =
                            (TextView) convertView.findViewById(R.id.tv_semester_information_label);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                String header = ((Header) listItem).getName();
                viewHolder.tvName.setText(header);

                // Set cumulative gpa for past courses only.
                if (header.equals(mContext.getString(R.string.semester_past))) {
                    double gpa = mDatabase.getCumulativeGPA();
                    if (gpa > -1) {
                        viewHolder.tvInformation.setText(mContext.getString(R.string.gpa) + ": "
                                + new DecimalFormat("#.0#").format(gpa));
                    } else {
                        viewHolder.tvInformation.setText(mContext.getString(R.string.gpa) + ": "
                                + mContext.getString(R.string.not_available));
                    }
                }
            } else {
                Semester semester = (Semester) listItem;
                viewHolder.tvName.setText(semester.toString());

                // Set semester label to current or completed based on semester value.
                if (semester.isCurrent()) {
                    viewHolder.tvInformation.setText(semester.getDaysLeft());
                    viewHolder.tvInformationLabel.setText(mContext.getString(R.string.semester_days_left));
                } else {
                    // Otherwise set label to completed and information to gpa.
                    double gpa = semester.getGpa();

                    // Set gpa if it exists.
                    viewHolder.tvInformation.setText(gpa > -1 ? Double.toString(semester.getGpa()) :
                                    mContext.getString(R.string.not_available)
                    );
                    viewHolder.tvInformationLabel.setText(mContext.getString(R.string.tv_gpa));
                }
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvInformation;
            TextView tvInformationLabel;
        }
    }
}
