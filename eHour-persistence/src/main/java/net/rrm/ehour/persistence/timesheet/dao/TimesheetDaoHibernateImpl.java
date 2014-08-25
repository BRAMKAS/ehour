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

package net.rrm.ehour.persistence.timesheet.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.domain.Activity;
import net.rrm.ehour.domain.TimesheetEntry;
import net.rrm.ehour.domain.TimesheetEntryId;
import net.rrm.ehour.persistence.dao.AbstractGenericDaoHibernateImpl;
import net.rrm.ehour.timesheet.dto.BookedDay;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

@Repository("timesheetDAO")
public class TimesheetDaoHibernateImpl 
		extends AbstractGenericDaoHibernateImpl<TimesheetEntry, TimesheetEntryId>
		implements TimesheetDao
{
	/**
	 * @todo fix this a bit better
	 */
	public TimesheetDaoHibernateImpl()
	{
		super(TimesheetEntry.class);
	}	
	
	/**
	 * Get timesheet entries within date range for a user
	 * @param userId
	 * @param dateStart
	 * @param dateEnd
	 * @return List with TimesheetEntry domain objects
	 */
	public List<TimesheetEntry> getTimesheetEntriesInRange(Integer userId, DateRange dateRange)
	{
		return getSheetOnUserIdAndRange(userId, dateRange, "Timesheet.getEntriesBetweenDateForUserId");
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.rrm.ehour.persistence.persistence.timesheet.dao.TimesheetDAO#getTimesheetEntriesInRange(net.rrm.ehour.persistence.persistence.domain.ProjectAssignment, net.rrm.ehour.persistence.persistence.data.DateRange)
	 */
	@SuppressWarnings("unchecked")
	public List<TimesheetEntry> getTimesheetEntriesInRange(Activity activity, DateRange dateRange)
	{
		String[] keys = new String[]{"dateStart", "dateEnd", "activity"};
		Object[] params = new Object[]{dateRange.getDateStart(),dateRange.getDateEnd(), activity};
		String hql= "Timesheet.getEntriesBetweenDateForActivity";
		
		return getHibernateTemplate().findByNamedQueryAndNamedParam(hql, keys, params);
	}	
	
	/**
	 * Get  hours per day for a date range
	 * @param userId
	 * @param dateRange
	 * @return List with key values -> key = date, value = hours booked
	 */	
	public List<BookedDay> getBookedHoursperDayInRange(Integer userId, DateRange dateRange)
	{
		return getSheetOnUserIdAndRange(userId, dateRange, "Timesheet.getBookedDaysInRangeForUserId");
	}
	 	
	/**
	 * 
	 * @param userId
	 * @param range
	 * @param hql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> getSheetOnUserIdAndRange(Integer userId, DateRange dateRange, String hql)
	{
		String[] keys = new String[]{"dateStart", "dateEnd", "userId"};
		Object[] params = new Object[]{dateRange.getDateStart(),dateRange.getDateEnd(), userId};

		return getHibernateTemplate().findByNamedQueryAndNamedParam(hql, keys, params);
	}

	/*
	 * (non-Javadoc)
	 * @see net.rrm.ehour.persistence.persistence.timesheet.dao.TimesheetDAO#getLatestTimesheetEntryForAssignment(java.lang.Integer)
	 */
	@SuppressWarnings({ "unchecked"})
	public TimesheetEntry getLatestTimesheetEntryForActivity(final Integer activityId)
	{
		return (TimesheetEntry) getHibernateTemplate().executeWithNativeSession(
				new HibernateCallback()
				{
					public Object doInHibernate(Session session) throws HibernateException
					{
						List<TimesheetEntry> results;
						
						Query queryObject = session.getNamedQuery("Timesheet.getLatestEntryForActivityId");
						
						queryObject.setInteger("activityId", activityId);
						queryObject.setMaxResults(1);
						results = (List<TimesheetEntry>)queryObject.list();
						
						return ((results != null && results.size() > 0) ? results.get(0) : null);
					}
			});		
	}

	/*
	 * (non-Javadoc)
	 * @see net.rrm.ehour.persistence.persistence.timesheet.dao.TimesheetDAO#deleteTimesheetEntries(java.util.List)
	 */
	public int deleteTimesheetEntries(List<? extends Serializable> activityIds)
	{
		Session session = getSession();
		Query	query = session.getNamedQuery("Timesheet.deleteOnActivityIds");
		query.setParameterList("activityIds", activityIds);
		
		return query.executeUpdate();
	}

	/*
	 * (non-Javadoc)
	 * @see net.rrm.ehour.persistence.persistence.timesheet.dao.TimesheetDAO#getTimesheetEntriesAfter(java.lang.Integer, java.util.Date)
	 */
	@SuppressWarnings("unchecked")
	public List<TimesheetEntry> getTimesheetEntriesAfter(Activity activity, Date date)
	{
		String[]	keys = new String[]{"date", "activity"};
		Object[]	params = new Object[]{date, activity};
		
		return getHibernateTemplate().findByNamedQueryAndNamedParam("Timesheet.getEntriesAfterDateForActivity", keys, params);
	}

	/*
	 * (non-Javadoc)
	 * @see net.rrm.ehour.persistence.persistence.timesheet.dao.TimesheetDAO#getTimesheetEntriesBefore(java.lang.Integer, java.util.Date)
	 */
	@SuppressWarnings("unchecked")
	public List<TimesheetEntry> getTimesheetEntriesBefore(Activity activity, Date date)
	{
		String[]	keys = new String[]{"date", "activity"};
		Object[]	params = new Object[]{date, activity};
		
		return getHibernateTemplate().findByNamedQueryAndNamedParam("Timesheet.getEntriesBeforeDateForActivity", keys, params);
	}
}
