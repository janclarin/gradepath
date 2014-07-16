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
            long dayDifference = TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS);

            // Determine if the day is actually the same day, yesterday or tomorrow since
            // TimeUnit.DAYS.convert rounds the day difference.
            // 0 indicates today, -1 indicates yesterday, 1 indicates tomorrow.
            if (dayDifference == 0) {
                int dateCheck = one.compareTo(two);
                if (dateCheck == -1) {
                    return -1;
                } else if (dateCheck == 1) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return dayDifference;
            }
        }
    }
}
