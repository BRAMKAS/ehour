/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.ui.timesheet.dto;

import com.google.common.collect.Lists;
import net.rrm.ehour.config.EhourConfig;
import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.domain.Activity;
import net.rrm.ehour.domain.Project;
import net.rrm.ehour.domain.TimesheetEntry;
import net.rrm.ehour.timesheet.dto.WeekOverview;
import net.rrm.ehour.ui.timesheet.util.TimesheetRowComparator;
import net.rrm.ehour.util.DateUtil;

import java.io.Serializable;
import java.util.*;

/**
 * Generates the timesheet backing object
 */

public class TimesheetFactory {
    private final EhourConfig config;
    private final WeekOverview weekOverview;

    public TimesheetFactory(EhourConfig config, WeekOverview weekOverview) {
        this.config = config;
        this.weekOverview = weekOverview;
    }

    /**
     * Create timesheet form
     */
    public Timesheet createTimesheet() {
        List<Date> dateSequence = DateUtil.createDateSequence(weekOverview.getWeekRange(), config);
        List<TimesheetDate> timesheetDates = createTimesheetDates(dateSequence, weekOverview.getLockedDays());

        Timesheet timesheet = new Timesheet();
        timesheet.setMaxHoursPerDay(config.getCompleteDayHours());
        List<TimesheetRow> timesheetRows = createTimesheetRows(weekOverview.getActivityMap(), timesheetDates, weekOverview.getActivities(), timesheet);

        timesheet.setCustomers(structureRowsPerProject(timesheetRows));
        timesheet.setDateSequence(dateSequence.toArray(new Date[7]));
        timesheet.setWeekStart(weekOverview.getWeekRange().getDateStart());
        timesheet.setWeekEnd(weekOverview.getWeekRange().getDateEnd());

        timesheet.setComment(weekOverview.getComment());
        timesheet.setUser(weekOverview.getUser());

        timesheet.setLockedDays(weekOverview.getLockedDays());

        return timesheet;
    }

    private List<TimesheetDate> createTimesheetDates(List<Date> dateSequence, Collection<Date> lockedDays) {
        List<String> formattedLockedDays = formatLockedDays(lockedDays);

        List<TimesheetDate> dates = new ArrayList<TimesheetDate>();

        for (Date date : dateSequence) {
            Calendar calendar = DateUtil.getCalendar(config);
            calendar.setTime(date);
            String formattedDate = weekOverview.formatter.format(date);
            boolean locked = formattedLockedDays.contains(formattedDate);

            dates.add(new TimesheetDate(date, calendar.get(Calendar.DAY_OF_WEEK) - 1, formattedDate, locked));
        }

        return dates;
    }

    private List<String> formatLockedDays(Collection<Date> lockedDays) {
        List<String> formattedLockedDays = Lists.newArrayList();

        for (Date lockedDay : lockedDays) {
            formattedLockedDays.add(weekOverview.formatter.format(lockedDay));
        }

        return formattedLockedDays;
    }

    private SortedMap<Project, List<TimesheetRow>> structureRowsPerProject(List<TimesheetRow> rows) {
        SortedMap<Project, List<TimesheetRow>> projectMap = new TreeMap<Project, List<TimesheetRow>>();

        for (TimesheetRow timesheetRow : rows) {
            Project customer = timesheetRow.getActivity().getProject();

            List<TimesheetRow> timesheetRows = projectMap.containsKey(customer) ? projectMap.get(customer) : new ArrayList<TimesheetRow>();
            timesheetRows.add(timesheetRow);

            projectMap.put(customer, timesheetRows);
        }

        sortTimesheetRows(projectMap);

        return projectMap;
    }

    private void sortTimesheetRows(SortedMap<Project, List<TimesheetRow>> rows) {
        Set<Map.Entry<Project, List<TimesheetRow>>> entries = rows.entrySet();

        for (Map.Entry<Project, List<TimesheetRow>> entry : entries) {
            Collections.sort(entry.getValue(), TimesheetRowComparator.INSTANCE);
        }
    }

    private List<TimesheetRow> createTimesheetRows(Map<Activity, Map<String, TimesheetEntry>> activityMap,
                                                   List<TimesheetDate> timesheetDates,
                                                   List<Activity> validActivities,
                                                   Timesheet timesheet) {
        List<TimesheetRow> timesheetRows = new ArrayList<TimesheetRow>();
        Calendar firstDate = DateUtil.getCalendar(config);

        if (timesheetDates.size() > 0) {
            firstDate.setTime(timesheetDates.get(0).date);
        }

        for (Map.Entry<Activity, Map<String, TimesheetEntry>> activityEntry : activityMap.entrySet()) {
            Activity activity = activityEntry.getKey();

            TimesheetRow timesheetRow = new TimesheetRow(config);
            timesheetRow.setTimesheet(timesheet);
            timesheetRow.setActivity(activity);
            timesheetRow.setFirstDayOfWeekDate(firstDate);

            // create a cell for every requested timesheetDate
            for (TimesheetDate timesheetDate : timesheetDates) {
                TimesheetEntry entry = activityEntry.getValue().get(timesheetDate.formatted);

                timesheetRow.addTimesheetCell(timesheetDate.dayInWeek,
                        createTimesheetCell(activity, entry, timesheetDate.date, timesheetDate.locked, validActivities));
            }

            timesheetRows.add(timesheetRow);
        }

        return timesheetRows;
    }

    /**
     * Create timesheet cell, a cell is valid when the timesheetDate is within the assignment valid range
     */
    private TimesheetCell createTimesheetCell(Activity activity,
                                              TimesheetEntry entry,
                                              Date date,
                                              Boolean locked,
                                              List<Activity> validActivities) {
        TimesheetCell cell = new TimesheetCell();

        cell.setTimesheetEntry(entry);
        cell.setValid(isCellValid(activity, validActivities, date));

        cell.setLocked(locked);
        cell.setDate(date);

        return cell;
    }

    /**
     * Check if the cell is still valid. Even if they're in the timesheet entries it can be that time allotted
     * assignments are over their budget or default assignments are de-activated
     */
    private boolean isCellValid(Activity activity,
                                List<Activity> validActivities,
                                Date date) {
        // first check if it's in valid project assignments (time allotted can have values
        // but not be valid anymore)
        boolean isValid = validActivities.contains(activity);

        DateRange dateRange = new DateRange(activity.getDateStart(), activity.getDateEnd());

        isValid = isValid && DateUtil.isDateWithinRange(date, dateRange);

        return isValid;
    }

    private static class TimesheetDate implements Serializable {
        final Date date;
        final int dayInWeek;
        final String formatted;
        final boolean locked;

        private TimesheetDate(Date date, int dayInWeek, String formatted, boolean locked) {
            this.date = date;
            this.dayInWeek = dayInWeek;
            this.formatted = formatted;
            this.locked = locked;
        }
    }
}
