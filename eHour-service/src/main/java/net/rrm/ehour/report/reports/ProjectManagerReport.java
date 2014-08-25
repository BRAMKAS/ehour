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

package net.rrm.ehour.report.reports;

<<<<<<< HEAD
import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.domain.Project;
import net.rrm.ehour.report.reports.element.AssignmentAggregateReportElement;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Project manager report
 */

public class ProjectManagerReport implements Serializable {
    private static final long serialVersionUID = 1768574303126675320L;

    private Project project;
    private SortedSet<AssignmentAggregateReportElement> aggregates = new TreeSet<AssignmentAggregateReportElement>();
    private DateRange reportRange;
    private Float totalHoursBooked;
    private Float totalHoursAvailable;

    /**
     * Derive the totals
     */
    public void deriveTotals() {
        float hours = 0;
        float avail = 0;

        if (aggregates != null) {
            for (AssignmentAggregateReportElement aggregate : aggregates) {
                if (aggregate.getHours() == null) {
                    continue;
                }

                hours += aggregate.getHours().floatValue();
                avail += aggregate.getAvailableHours().or(0f);
            }
        }

        totalHoursBooked = hours;
        totalHoursAvailable = avail;
    }

    /**
     * @return the aggregates
     */
    public SortedSet<AssignmentAggregateReportElement> getAggregates() {
        return aggregates;
    }

    /**
     * @param aggregates the aggregates to set
     */
    public void setAggregates(SortedSet<AssignmentAggregateReportElement> aggregates) {
        this.aggregates = aggregates;
    }

    /**
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * @return the reportRange
     */
    public DateRange getReportRange() {
        return reportRange;
    }

    /**
     * @param reportRange the reportRange to set
     */
    public void setReportRange(DateRange reportRange) {
        this.reportRange = reportRange;
    }

    /**
     * @return the totalHoursAvailable
     */
    public Float getTotalHoursAvailable() {
        return totalHoursAvailable;
    }

    /**
     * @return the totalHoursBooked
     */
    public Float getTotalHoursBooked() {
        return totalHoursBooked;
    }

=======
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.domain.MailLogAssignment;
import net.rrm.ehour.domain.Project;
import net.rrm.ehour.report.reports.element.ActivityAggregateReportElement;

/**
 * Project manager report 
 **/

public class ProjectManagerReport implements Serializable
{
	private static final long serialVersionUID = 1768574303126675320L;
	
	private Project									project;
	private	SortedSet<ActivityAggregateReportElement>	aggregates = new TreeSet<ActivityAggregateReportElement>();
	private	DateRange								reportRange;
	private	SortedSet<MailLogAssignment>			sentMail;
	private	Float									totalHoursBooked;
	private	Float									totalHoursAvailable;

	/**
	 * Derive the totals
	 *
	 */
	public void deriveTotals()
	{
		float 	hours = 0;
		float	avail = 0;
		
		if (aggregates != null)
		{
			for (ActivityAggregateReportElement aggregate : aggregates)
			{
				if (aggregate.getHours() == null)
				{
					continue;
				}
				
				hours += aggregate.getHours().floatValue();

				//TODO-NK Need to see how we have to do the same for Activity
//				if (aggregate.getProjectAssignment().getAssignmentType().isAllottedType())
//				{
				avail += aggregate.getActivity().getAllottedHours().floatValue();	
//				}
			}
		}
		
		totalHoursBooked = new Float(hours);
		totalHoursAvailable = new Float(avail);
	}
	
	/**
	 * @return the aggregates
	 */
	public SortedSet<ActivityAggregateReportElement> getAggregates()
	{
		return aggregates;
	}
	/**
	 * @param aggregates the aggregates to set
	 */
	public void setAggregates(SortedSet<ActivityAggregateReportElement> aggregates)
	{
		this.aggregates = aggregates;
	}
	/**
	 * @return the project
	 */
	public Project getProject()
	{
		return project;
	}
	/**
	 * @param project the project to set
	 */
	public void setProject(Project project)
	{
		this.project = project;
	}
	/**
	 * @return the sentMail
	 */
	public SortedSet<MailLogAssignment> getSentMail()
	{
		return sentMail;
	}
	/**
	 * @param sentMail the sentMail to set
	 */
	public void setSentMail(SortedSet<MailLogAssignment> sentMail)
	{
		this.sentMail = sentMail;
	}
	/**
	 * @return the reportRange
	 */
	public DateRange getReportRange()
	{
		return reportRange;
	}
	/**
	 * @param reportRange the reportRange to set
	 */
	public void setReportRange(DateRange reportRange)
	{
		this.reportRange = reportRange;
	}
	/**
	 * @return the totalHoursAvailable
	 */
	public Float getTotalHoursAvailable()
	{
		return totalHoursAvailable;
	}
	/**
	 * @param totalHoursAvailable the totalHoursAvailable to set
	 */
	public void setTotalHoursAvailable(Float totalHoursAvailable)
	{
		this.totalHoursAvailable = totalHoursAvailable;
	}
	/**
	 * @return the totalHoursBooked
	 */
	public Float getTotalHoursBooked()
	{
		return totalHoursBooked;
	}
	/**
	 * @param totalHoursBooked the totalHoursBooked to set
	 */
	public void setTotalHoursBooked(Float totalHoursBooked)
	{
		this.totalHoursBooked = totalHoursBooked;
	}
>>>>>>> 420c91d... EHV-23, EHV-24: Modifications in Service, Dao and UI layers for Customer --> Project --> Activity structure
}
