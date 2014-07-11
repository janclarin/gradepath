package com.janclarin.gradepath.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public abstract class DatabaseItem implements Serializable {

    protected long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Compares two calendars.
     *
     * @return number of days apart, return -1 if late.
     */
    protected long getDayDifference(Calendar one, Calendar two) {
        // If they are the same day.
        if (one.equals(two)) {
            return 0;
        } else {
            long timeDifference = one.getTimeInMillis() - two.getTimeInMillis();

            if (timeDifference < 0) {
                return -1;
            } else {
                return TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS);
            }
        }
    }
}
