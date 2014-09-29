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

package net.rrm.ehour.report.service;

import com.google.common.collect.Lists;
import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.domain.Activity;
import net.rrm.ehour.domain.Project;
import net.rrm.ehour.domain.User;
import net.rrm.ehour.persistence.project.dao.ProjectDao;
import net.rrm.ehour.persistence.report.dao.ReportAggregatedDao;
import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.report.criteria.UserSelectedCriteria;
import net.rrm.ehour.report.reports.ReportData;
import net.rrm.ehour.report.reports.element.ProjectStructuredReportElement;
import net.rrm.ehour.timesheet.service.TimesheetLockService;
import net.rrm.ehour.timesheet.service.TimesheetLockService$;
import org.joda.time.Interval;
import scala.collection.Seq;

import java.util.Date;
import java.util.List;

/**
 * Abstract report service provides utility methods for dealing
 * with the usercriteria obj
 */

public abstract class AbstractReportServiceImpl<RE extends ProjectStructuredReportElement> {
    private ProjectDao projectDAO;

    private TimesheetLockService lockService;
    protected ReportAggregatedDao reportAggregatedDao;

    AbstractReportServiceImpl() {
    }

    protected AbstractReportServiceImpl(ProjectDao projectDAO, TimesheetLockService lockService, ReportAggregatedDao reportAggregatedDao) {
        this.projectDAO = projectDAO;
        this.lockService = lockService;
        this.reportAggregatedDao = reportAggregatedDao;
    }

    ReportData getReportData(ReportCriteria reportCriteria) {
        UserSelectedCriteria userSelectedCriteria = reportCriteria.getUserSelectedCriteria();

        DateRange reportRange = reportCriteria.getReportRange();

        Seq<Interval> lockedDatesAsIntervals = lockService.findLockedDatesInRange(reportRange.getDateStart(), reportRange.getDateEnd());
        List<Date> lockedDates = TimesheetLockService$.MODULE$.intervalToJavaList(lockedDatesAsIntervals);

        List<RE> allReportElements = generateReport(userSelectedCriteria, lockedDates, reportRange);

        if (userSelectedCriteria.isForPm()) {
            List<ProjectStructuredReportElement> elem = evictNonPmReportElements(userSelectedCriteria, allReportElements);
            return new ReportData(lockedDates, elem, reportRange, userSelectedCriteria);
        } else {
            return new ReportData(lockedDates, allReportElements, reportRange, userSelectedCriteria);
        }
    }

    private List<ProjectStructuredReportElement> evictNonPmReportElements(UserSelectedCriteria userSelectedCriteria, List<RE> allReportElements) {
        List<Integer> projectIds = fetchAllowedProjectIds(userSelectedCriteria);

        List<ProjectStructuredReportElement> allowedElements = Lists.newArrayList();

        for (ProjectStructuredReportElement reportElement : allReportElements) {
            if (projectIds.contains(reportElement.getProjectId())) {
                allowedElements.add(reportElement);
            }
        }
        return allowedElements;
    }

    private List<Integer> fetchAllowedProjectIds(UserSelectedCriteria userSelectedCriteria) {
        List<Project> allowedProjects = projectDAO.findActiveProjectsWhereUserIsPM(userSelectedCriteria.getPm());

        List<Integer> projectIds = Lists.newArrayList();

        for (Project allowedProject : allowedProjects) {
            projectIds.add(allowedProject.getProjectId());
        }
        return projectIds;
    }


    private List<RE> generateReport(UserSelectedCriteria userSelectedCriteria, List<Date> lockedDates, DateRange reportRange) {
        boolean noUserRestrictionProvided = userSelectedCriteria.isEmptyUsers();
        boolean noProjectRestrictionProvided = userSelectedCriteria.isEmptyCustomers() && userSelectedCriteria.isEmptyProjects();

        List<Project> projects = null;
        List<User> users = null;

        if (!noProjectRestrictionProvided || !noUserRestrictionProvided) {
            if (noProjectRestrictionProvided) {
                users = userSelectedCriteria.getUsers();
            } else if (noUserRestrictionProvided) {
                projects = getProjects(userSelectedCriteria);
            } else {
                users = userSelectedCriteria.getUsers();
                projects = getProjects(userSelectedCriteria);
            }
        }

        if (userSelectedCriteria.isOnlyBillableProjects() && projects == null) {
            projects = getBillableProjects(userSelectedCriteria);
        }

        return getReportElements(users, projects, lockedDates, reportRange, userSelectedCriteria.isShowZeroBookings());
    }

    /**
     * Get the actual data
     */
    protected abstract List<RE> getReportElements(List<User> users,
                                                  List<Project> projects,
                                                  List<Date> lockedDates,
                                                  DateRange reportRange,
                                                  boolean showZeroBookings);

    /**
     * Get project id's based on selected customers
     */
    private List<Project> getProjects(UserSelectedCriteria userSelectedCriteria) {
        List<Project> projects;

        // No projects selected by the user, use any given customer limitation
        if (userSelectedCriteria.isEmptyProjects()) {
            if (!userSelectedCriteria.isEmptyCustomers()) {
                projects = projectDAO.findProjectForCustomers(userSelectedCriteria.getCustomers(),
                        userSelectedCriteria.isOnlyActiveProjects());
            } else {
                projects = null;
            }
        } else {
            projects = userSelectedCriteria.getProjects();
        }

        return projects;
    }

    private List<Project> getBillableProjects(UserSelectedCriteria criteria) {
        List<Project> projects = criteria.isOnlyActiveProjects() ? projectDAO.findAllActive() : projectDAO.findAll();

        List<Project> filteredProjects = Lists.newArrayList();

        for (Project project : projects) {
            boolean billableFilter = project.isBillable();
            boolean customerFilter = !criteria.isOnlyActiveCustomers() || project.getCustomer().isActive();

            if (billableFilter && customerFilter) {
                filteredProjects.add(project);
            }
        }

        return filteredProjects;
    }

    protected List<Activity> getActivitiesWithoutBookings(DateRange reportRange, List<Integer> userIds, List<Integer> projectIds) {
        List<Activity> activitiesWithoutBookings = reportAggregatedDao.getActivitiesWithoutBookings(reportRange);

        List<Activity> filteredAssignmentsWithoutBookings = Lists.newArrayList();

        for (Activity activitiesWithoutBooking : activitiesWithoutBookings) {
            boolean passedUserFilter = userIds == null || userIds.contains(activitiesWithoutBooking.getAssignedUser().getUserId());
            boolean passedProjectFilter = projectIds == null || projectIds.contains(activitiesWithoutBooking.getProject().getProjectId());

            if (passedUserFilter && passedProjectFilter) {
                filteredAssignmentsWithoutBookings.add(activitiesWithoutBooking);
            }
        }
        return filteredAssignmentsWithoutBookings;
    }
}
