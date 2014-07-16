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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Semester;

import java.util.List;

/**
 * Fragment with list of list_course that allows for editing and sets option for a new course.
 */
public class ListSemesterFragment extends BaseListFragment
        implements PopupMenu.OnMenuItemClickListener {

    private FragmentListSemesterListener mListener;

    public static ListSemesterFragment newInstance() {
        return new ListSemesterFragment();
    }

    public ListSemesterFragment() {
        // Required empty public constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_semester, container, false);

        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_semester_empty);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_semester);

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
                mListItems.remove(selectedSemester);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void showPopupMenu(View view) {
        // TODO: Change to PopupWindow.
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.list_semester_options, popup.getMenu());
        popup.show();

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_semester_to_current:
                // TODO: Display dialog to set end date.
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentListSemesterListener) activity;
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
    public interface FragmentListSemesterListener {

        /**
         * Called when contextual action bar edit button is clicked.
         */
        void onListSemesterEdit(Semester semester);

        /**
         * Called when contextual action bar delete button is clicked.
         */
        void onListSemesterDelete(Semester semester);
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
                            .inflate(R.layout.fragment_list_header_semester, parent, false);
                    viewHolder.tvName = (TextView) convertView;
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_semester_item, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_semester_name);
                    viewHolder.tvInformation =
                            (TextView) convertView.findViewById(R.id.tv_semester_information);
                    viewHolder.tvInformationLabel =
                            (TextView) convertView.findViewById(R.id.tv_semester_information_label);
                    viewHolder.btnSemesterOptions =
                            (ImageButton) convertView.findViewById(R.id.btn_semester_options);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvName.setText(((Header) listItem).getName());
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
                                    mContext.getString(R.string.tv_not_set)
                    );
                    viewHolder.tvInformationLabel.setText(mContext.getString(R.string.tv_gpa));
                }

                viewHolder.btnSemesterOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupMenu(viewHolder.btnSemesterOptions);
                    }
                });
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvInformation;
            TextView tvInformationLabel;
            ImageButton btnSemesterOptions;
        }
    }
}
