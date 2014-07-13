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
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Semester;

/**
 * Fragment with list of list_course that allows for editing and sets option for a new course.
 */
public class ListSemesterFragment extends BaseListFragment<Semester>
        implements PopupMenu.OnMenuItemClickListener {

    private FragmentListSemesterListener mListener;

    public ListSemesterFragment() {
        // Required empty constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_semester, container, false);

        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_semester_empty);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_semester);

        updateList();
        initAdapter();
        setUpListView();

        showEmptyStateView(mListItems.isEmpty());

        return rootView;
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
            public View getView(int position, View convertView, ViewGroup parent) {

                Semester semester = mListItems.get(position);

                final ViewHolder viewHolder;

                if (convertView == null) {
                    viewHolder = new ViewHolder();

                    convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_list_semester_item, null);

                    viewHolder.tvSemesterName = (TextView) convertView.findViewById(R.id.tv_semester_name);
//                    viewHolder.tvSemesterLabel = (TextView) convertView.findViewById(R.id.tv_semester_label);
                    viewHolder.tvInformation =
                            (TextView) convertView.findViewById(R.id.tv_semester_information);
                    viewHolder.tvInformationLabel =
                            (TextView) convertView.findViewById(R.id.tv_semester_information_label);
                    viewHolder.btnSemesterOptions =
                            (ImageButton) convertView.findViewById(R.id.btn_semester_options);

                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.tvSemesterName.setText(semester.toString());

                // Set semester label to current or completed based on semester value.
                if (semester.isCurrent()) {
//                    viewHolder.tvSemesterLabel.setText(mContext.getString(R.string.semester_current));
                    viewHolder.tvInformation.setText(semester.getDaysLeft());
                    viewHolder.tvInformationLabel.setText(mContext.getString(R.string.semester_days_left));
                } else {
                    double gpa = semester.getGpa();
                    // Otherwise set label to completed and information to gpa.
//                    viewHolder.tvSemesterLabel.setText(mContext.getString(R.string.semester_past));

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

                return convertView;
            }

            class ViewHolder {
                TextView tvSemesterName;
                TextView tvSemesterLabel;
                TextView tvInformation;
                TextView tvInformationLabel;
                ImageButton btnSemesterOptions;
            }
        };
    }

    @Override
    protected void updateList() {
        // List of semesters.
        mListItems = mDatabase.getSemesters();
        if (mAdapter != null) mAdapter.notifyDataSetChanged();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray selectedPositions) {
        for (int i = 0; i < mListItems.size(); i++) {
            if (selectedPositions.get(i, false)) {
                Semester selectedSemester = (Semester) mAdapter.getItem(i);
                mDatabase.deleteSemester(selectedSemester);
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
}
