package com.janclarin.gradepath.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Semester;

import java.util.ArrayList;
import java.util.List;

abstract public class BaseListFragment extends BaseFragment {

    protected static final int ITEM_VIEW_TYPE_HEADER = 0;
    protected static final int ITEM_VIEW_TYPE_MAIN_2_LINE = 1;
    protected static final int ITEM_VIEW_TYPE_MAIN_3_LINE = 2;
    protected static final int NUM_ITEM_VIEW_TYPES = 3;

    protected List<DatabaseItem> mListItems;
    protected TextView mEmptyTextView;
    protected ListView mListView;
    protected BaseListAdapter mAdapter;

    /**
     * Updates the list view within the fragment.
     */
    abstract public void updateListItems();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_general_list, container, false);

        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_empty);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_items);

        return rootView;
    }

    /**
     * Show popup menu on overflow button click.
     */
    public void showPopupMenu(View view, int menuId, final int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, view);

        popupMenu.getMenuInflater().inflate(menuId, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_delete:
                        deleteSelectedItem(position);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    /**
     * Open grade calculator activity.
     */
    protected void showGradeCalculator(Course course) {

    }

    /**
     * Delete selected item. Display confirmation message for courses.
     */
    protected void deleteSelectedItem(int position) {
        final DatabaseItem selectedItem = (DatabaseItem) mAdapter.getItem(position);

        if (selectedItem instanceof Grade) {
            mDatabase.deleteGrade(selectedItem.getId());
            updateListItems();
            notifyAdapter();

        } else if (selectedItem instanceof Course) {
            final Course course = (Course) selectedItem;
            final String title = String.format(getString(R.string.title_delete_dialog), course.getName());
            final String positiveMessage =
                    String.format(getString(R.string.toast_alert_delete_confirmation), course.getName());
            new AlertDialog.Builder(mContext)
                    .setTitle(title)
                    .setMessage(R.string.message_delete_course_dialog)
                    .setIcon(R.drawable.remove)
                    .setPositiveButton(R.string.btn_alert_delete_positive,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Delete course, display toast, and refresh list.
                                    mDatabase.deleteCourse(course.getId());
                                    Toast.makeText(mContext, positiveMessage, Toast.LENGTH_SHORT)
                                            .show();
                                    // Update list.
                                    updateListItems();
                                    notifyAdapter();
                                }
                            })
                    .setNegativeButton(R.string.btn_alert_delete_negative,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(mContext, mContext.getString(R.string.cancelled),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })
                    .show();

        } else if (selectedItem instanceof Semester) {
            final Semester semester = (Semester) selectedItem;
            final String title = String.format(getString(R.string.title_delete_dialog), semester.toString());
            final String positiveMessage =
                    String.format(getString(R.string.toast_alert_delete_confirmation), semester.toString());

            new AlertDialog.Builder(mContext)
                    .setTitle(title)
                    .setMessage(R.string.message_delete_semester_dialog)
                    .setIcon(R.drawable.remove)
                    .setPositiveButton(R.string.btn_alert_delete_positive,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Delete semester, display text, and refresh list.
                                    mDatabase.deleteSemester(semester);
                                    Toast.makeText(mContext, positiveMessage, Toast.LENGTH_SHORT)
                                            .show();
                                    // Refresh lists.
                                    updateListItems();
                                    notifyAdapter();
                                }
                            })
                    .setNegativeButton(R.string.btn_alert_delete_negative,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(mContext, mContext.getString(R.string.cancelled),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })
                    .show();
        }
    }

    /**
     * Clear items from list.
     */
    protected void clearListItems() {
        try {
            mListItems.clear();
        } catch (NullPointerException e) {
            mListItems = new ArrayList<DatabaseItem>();
        }
    }

    /**
     * Notify adapter of database set changed.
     */
    protected void notifyAdapter() {
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    /**
     * Sets up the list view.
     */
    protected void setUpListView() {
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    }

    /**
     * Changes list state based on whether or not the list of items is empty.
     */
    protected void showEmptyStateView(boolean isEmpty) {
        if (isEmpty) {
            mEmptyTextView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    abstract public class BaseListAdapter extends BaseAdapter {

        private static final int COLOR_CIRCLE_DIAMETER = 40;

        // Maps color integer value to color circle drawable.
        final SparseArray<ShapeDrawable> mColorDrawables = new SparseArray<ShapeDrawable>();

        // Creates a circle if it hasn't been already for this color.
        public ShapeDrawable getColorCircle(int color) {
            ShapeDrawable colorDrawable = mColorDrawables.get(color);

            if (colorDrawable == null) {
                colorDrawable = new ShapeDrawable(new OvalShape());
                colorDrawable.setIntrinsicWidth(COLOR_CIRCLE_DIAMETER);
                colorDrawable.setIntrinsicHeight(COLOR_CIRCLE_DIAMETER);
                colorDrawable.getPaint().setStyle(Paint.Style.FILL);
                colorDrawable.getPaint().setColor(color);
            }

            return colorDrawable;
        }

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
        public boolean isEnabled(int position) {
            final int itemViewType = getItemViewType(position);

            return itemViewType == ITEM_VIEW_TYPE_MAIN_2_LINE
                    || itemViewType == ITEM_VIEW_TYPE_MAIN_3_LINE;
        }

        @Override
        abstract public int getItemViewType(int position);

        @Override
        abstract public View getView(int position, View convertView, ViewGroup parent);
    }

    public class ViewHolder {
        public TextView tvTitle;
        public TextView tvSubtitle;
        public TextView tvSubtitle2;
        public ImageView ivDetail;
        public View btnSecondary;
    }

    /**
     * Header for tasks.
     */
    public class Header extends DatabaseItem {
        private final String name;

        public Header(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
