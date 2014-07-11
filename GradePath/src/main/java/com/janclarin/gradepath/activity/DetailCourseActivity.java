package com.janclarin.gradepath.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.model.Course;

public class DetailCourseActivity extends Activity {

    static final String LOG_TAG = DetailCourseActivity.class.getSimpleName();

    private DatabaseFacade mDatabase;
    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_course);

        mDatabase = DatabaseFacade.getInstance(getApplicationContext());
        mDatabase.open();

        mCourse = (Course) getIntent().getSerializableExtra(MainActivity.COURSE_KEY);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}
