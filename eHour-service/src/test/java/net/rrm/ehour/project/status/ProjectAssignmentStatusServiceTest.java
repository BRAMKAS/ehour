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

package net.rrm.ehour.project.status;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.rrm.ehour.domain.ProjectAssignment;
import net.rrm.ehour.domain.ProjectAssignmentType;
import net.rrm.ehour.domain.TimesheetEntry;
import net.rrm.ehour.domain.User;
import net.rrm.ehour.report.dao.ReportAggregatedDAO;
import net.rrm.ehour.report.reports.element.AssignmentAggregateReportElement;
import net.rrm.ehour.timesheet.dao.TimesheetDAO;
import net.rrm.ehour.util.EhourConstants;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 **/

public class ProjectAssignmentStatusServiceTest
{
	private	ProjectAssignmentStatusServiceImpl util;
	private	ReportAggregatedDAO	raDAO;
	private TimesheetDAO timesheetDAO;
	
	@Before
	public void setUp() throws Exception
	{
		util = new ProjectAssignmentStatusServiceImpl();

		raDAO = createMock(ReportAggregatedDAO.class);
		timesheetDAO = createMock(TimesheetDAO.class);
		util.setTimesheetDAO(timesheetDAO);
		util.setReportAggregatedDAO(raDAO);
	}

	@Test
	public final void testGetAssignmentStatusDateIn()
	{
		ProjectAssignment assignment = new ProjectAssignment();
		assignment.setAssignmentId(1);
		assignment.setUser(new User(1));
		ProjectAssignmentType type = new ProjectAssignmentType();
		type.setAssignmentTypeId(EhourConstants.ASSIGNMENT_DATE);
		assignment.setAssignmentType(type);
		
		Calendar startCal = new GregorianCalendar();
		startCal.add(Calendar.DAY_OF_YEAR, -5);
		Date start = startCal.getTime();
		
		assignment.setDateStart(startCal.getTime());

		Calendar endCal = new GregorianCalendar();
		endCal.add(Calendar.DAY_OF_YEAR, 2);
		Date end = endCal.getTime();
		assignment.setDateEnd(endCal.getTime());

		expect(timesheetDAO.getTimesheetEntriesBefore(new ProjectAssignment(1), start))
			.andReturn(new ArrayList<TimesheetEntry>());

		expect(timesheetDAO.getTimesheetEntriesAfter(new ProjectAssignment(1), end))
		.andReturn(new ArrayList<TimesheetEntry>());
		
		replay(timesheetDAO);
		ProjectAssignmentStatus status = util.getAssignmentStatus(assignment);
		verify(timesheetDAO);

		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.RUNNING));
		assertEquals(1, status.getStatusses().size());
	}	
	
	@Test
	public final void testGetAssignmentStatusDateOut()
	{
		ProjectAssignment assignment = new ProjectAssignment(1);
		assignment.setUser(new User(1));
		ProjectAssignmentType type = new ProjectAssignmentType();
		type.setAssignmentTypeId(EhourConstants.ASSIGNMENT_DATE);
		assignment.setAssignmentType(type);
		
		Calendar startCal = new GregorianCalendar();
		startCal.add(Calendar.DAY_OF_YEAR, -5);
		assignment.setDateStart(startCal.getTime());
		Date start = startCal.getTime();

		Calendar endCal = new GregorianCalendar();
		endCal.add(Calendar.DAY_OF_YEAR, -2);
		assignment.setDateEnd(endCal.getTime());
		Date end = endCal.getTime();
		
		expect(timesheetDAO.getTimesheetEntriesBefore(new ProjectAssignment(1), start))
		.andReturn(new ArrayList<TimesheetEntry>());

		List<TimesheetEntry> entry = new ArrayList<TimesheetEntry>();
		entry.add(new TimesheetEntry());
		expect(timesheetDAO.getTimesheetEntriesAfter(new ProjectAssignment(1), end))
			.andReturn(entry);

		replay(timesheetDAO);
		ProjectAssignmentStatus status = util.getAssignmentStatus(assignment);
		verify(timesheetDAO);


		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.AFTER_DEADLINE));
		assertEquals(1, status.getStatusses().size());
	}	
	
	@Test
	public final void testGetAssignmentStatusFixed()
	{
		AssignmentAggregateReportElement pag = new AssignmentAggregateReportElement();
		pag.setHours(new Double(25));
		
		ProjectAssignment assignment = new ProjectAssignment();
		ProjectAssignmentType type = new ProjectAssignmentType();
		type.setAssignmentTypeId(EhourConstants.ASSIGNMENT_TIME_ALLOTTED_FIXED);
		assignment.setAssignmentType(type);
		assignment.setAllottedHours(new Float(250f));
		
		expect(raDAO.getCumulatedHoursForAssignment(assignment))
				.andReturn(pag);
		
		replay(raDAO);
		
		ProjectAssignmentStatus status = util.getAssignmentStatus(assignment);
		
		verify(raDAO);

		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.IN_ALLOTTED));
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.RUNNING));
		assertEquals(2, status.getStatusses().size());
	}
	
	@Test
	public final void testGetAssignmentStatusFixedOverAllotted()
	{
		AssignmentAggregateReportElement pag = new AssignmentAggregateReportElement();
		pag.setHours(new Double(260));
		
		ProjectAssignment assignment = new ProjectAssignment();
		ProjectAssignmentType type = new ProjectAssignmentType();
		type.setAssignmentTypeId(EhourConstants.ASSIGNMENT_TIME_ALLOTTED_FIXED);
		assignment.setAssignmentType(type);
		assignment.setAllottedHours(new Float(250f));
		
		expect(raDAO.getCumulatedHoursForAssignment(assignment))
				.andReturn(pag);
		
		replay(raDAO);
		
		ProjectAssignmentStatus status = util.getAssignmentStatus(assignment);
		
		verify(raDAO);
		
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.OVER_ALLOTTED));
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.RUNNING));
		assertEquals(2, status.getStatusses().size());
	}	
	
	@Test
	public final void testGetAssignmentStatusFlex()
	{
		AssignmentAggregateReportElement pag = new AssignmentAggregateReportElement();
		pag.setHours(new Double(25));
		
		ProjectAssignment assignment = new ProjectAssignment();
		ProjectAssignmentType type = new ProjectAssignmentType();
		type.setAssignmentTypeId(EhourConstants.ASSIGNMENT_TIME_ALLOTTED_FLEX);
		assignment.setAssignmentType(type);
		assignment.setAllottedHours(new Float(250f));
		assignment.setAllowedOverrun(new Float(200f));
		
		expect(raDAO.getCumulatedHoursForAssignment(assignment))
				.andReturn(pag);
		
		replay(raDAO);
		
		ProjectAssignmentStatus status = util.getAssignmentStatus(assignment);
		
		verify(raDAO);
		
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.IN_ALLOTTED));
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.RUNNING));
		assertEquals(2, status.getStatusses().size());
	}
	
	@Test
	public final void testGetAssignmentStatusFlexOverrun()
	{
		AssignmentAggregateReportElement pag = new AssignmentAggregateReportElement();
		pag.setHours(new Double(260));
		
		ProjectAssignment assignment = new ProjectAssignment();
		ProjectAssignmentType type = new ProjectAssignmentType();
		type.setAssignmentTypeId(EhourConstants.ASSIGNMENT_TIME_ALLOTTED_FLEX);
		assignment.setAssignmentType(type);
		assignment.setAllottedHours(new Float(250f));
		assignment.setAllowedOverrun(new Float(200f));
		
		expect(raDAO.getCumulatedHoursForAssignment(assignment))
				.andReturn(pag);
		
		replay(raDAO);
		
		ProjectAssignmentStatus status = util.getAssignmentStatus(assignment);
		
		verify(raDAO);
		
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.IN_OVERRUN));
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.RUNNING));
		assertEquals(2, status.getStatusses().size());
	}	
	
	@Test
	public final void testGetAssignmentStatusFlexOverOverrun()
	{
		AssignmentAggregateReportElement pag = new AssignmentAggregateReportElement();
		pag.setHours(new Double(560));
		
		ProjectAssignment assignment = new ProjectAssignment();
		ProjectAssignmentType type = new ProjectAssignmentType();
		type.setAssignmentTypeId(EhourConstants.ASSIGNMENT_TIME_ALLOTTED_FLEX);
		assignment.setAssignmentType(type);
		assignment.setAllottedHours(new Float(250f));
		assignment.setAllowedOverrun(new Float(200f));
		
		expect(raDAO.getCumulatedHoursForAssignment(assignment))
				.andReturn(pag);
		
		replay(raDAO);
		
		ProjectAssignmentStatus status = util.getAssignmentStatus(assignment);
		
		verify(raDAO);
		
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.OVER_OVERRUN));
		assertTrue(status.getStatusses().contains(ProjectAssignmentStatus.Status.RUNNING));
		assertEquals(2, status.getStatusses().size());
	}	

//	@Test
//	public void testGetAssignmentStatusFixed()
//	{
//		ProjectAssignment assignment = new ProjectAssignment();
//		ProjectAssignmentType type = new ProjectAssignmentType(EhourConstants.ASSIGNMENT_TIME_ALLOTTED_FIXED);
//		assignment.setAssignmentType(type);
//		
//	}

}
