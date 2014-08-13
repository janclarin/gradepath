package com.janclarin.gradepath.fragment;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.DatabaseItem;

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
     * Updates the list view within the fragment.
     */
    abstract public void updateListItems();

    /**
     * Edit select item under contextual action bar.
     */
    abstract protected void editSelectedItem(int selectedPosition);

    /**
     * Delete selected items under contextual action bar.
     * Deletes items starting with right side of the list.
     *
     * @param possibleSelectedPositions
     */
    abstract protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions);

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
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private int mNumSelected;
            private int mCurrentSelectedPosition;
            private SparseBooleanArray selectedPositions;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                                  boolean checked) {
                Menu menu = mode.getMenu();
                MenuInflater inflater = mode.getMenuInflater();

                // When an item is checked keep track of how many are selected.
                if (checked) {
                    mNumSelected++;
                    selectedPositions.put(position, true);
                    // Change menu when more than one item is selected.
                    if (mNumSelected > 1) {
                        if (menu.getItem(0).getItemId() == R.id.menu_contextual_edit) {
                            menu.clear();
                            inflater.inflate(R.menu.list_item_selected, menu);
                        }
                    }
                } else {
                    if (mNumSelected > 0) {
                        mNumSelected--;
                        selectedPositions.put(position, false);
                    }

                    // Change menu when only one is left selected.
                    if (mNumSelected == 1) {
                        menu.clear();
                        inflater.inflate(R.menu.list_item_one_selected, menu);
                    }
                }

                mCurrentSelectedPosition = position;
                mode.setTitle(Integer.toString(mNumSelected));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mNumSelected = 0;
                mCurrentSelectedPosition = 0;
                selectedPositions = new SparseBooleanArray();

                // Vibrate on open.
                Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(300);

                // Inflate the menu for the contextual action bar.
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.list_item_one_selected, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_contextual_edit:
                        editSelectedItem(mCurrentSelectedPosition);
                        mode.finish();
                        return true;
                    case R.id.menu_contextual_delete:
                        deleteSelectedItems(selectedPositions);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mListView.getCheckedItemPositions().clear();
            }
        });
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

    abstract protected class BaseListAdapter extends BaseAdapter {

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
                colorDrawable.getPaint().setColor(getResources().getColor(color));
            }

            return colorDrawable;
        }

        protected class ViewHolder {
            TextView tvTitle;
            TextView tvSubtitle;
            TextView tvSubtitle2;
            ImageView ivDetail;
            View btnSecondary;
            View divider;
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

    /**
     * Header for tasks.
     */
    protected class Header extends DatabaseItem {
        private final String name;

        public Header(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
