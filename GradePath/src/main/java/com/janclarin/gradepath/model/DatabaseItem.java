package com.janclarin.gradepath.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public abstract class DatabaseItem implements Serializable {

    protected long id = -1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Compares two calendars.
     *
     * @return number of days apart.
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
                int dayOne = one.get(Calendar.DAY_OF_MONTH);
                int dayTwo = two.get(Calendar.DAY_OF_MONTH);

                // If the days are the same, they're on the same day.
                if (dayOne == dayTwo) {
                    return 0;
                } else {
                    int monthOne = one.get(Calendar.MONTH);
                    int monthTwo = two.get(Calendar.MONTH);

                    // Check months for edge cases like the 31st and 1st of two months.
                    if (monthOne == monthTwo) {
                        return dayOne < dayTwo ? -1 : 1;

                        // Check for new years.
                    } else if (monthOne == 11 && monthTwo == 1) {
                        return 1;
                    } else if (monthOne == 0 && monthTwo == 11) {
                        return -1;
                    } else {
                        return monthOne < monthTwo ? -1 : 1;
                    }
                }
            } else {
                return dayDifference;
            }
        }
    }
}
