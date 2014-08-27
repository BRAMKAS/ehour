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

package net.rrm.ehour.ui.report.aggregate;

import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.report.reports.ReportData;
import net.rrm.ehour.report.reports.element.AssignmentAggregateReportElement;
import net.rrm.ehour.report.reports.element.ReportElement;
import net.rrm.ehour.ui.common.report.AggregatedReportConfig;
import net.rrm.ehour.ui.report.AbstractAggregateReportModel;
import net.rrm.ehour.ui.report.aggregate.node.CustomerNode;
import net.rrm.ehour.ui.report.node.ReportNode;
import net.rrm.ehour.ui.report.node.ReportNodeFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserAggregateReportModel extends AbstractAggregateReportModel {
    private static final long serialVersionUID = 2883703894793044411L;

    public UserAggregateReportModel(ReportCriteria reportCriteria) {
        super(reportCriteria, AggregatedReportConfig.AGGREGATE_USER);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ReportData preprocess(ReportData reportData, ReportCriteria reportCriteria) {
        List<AssignmentAggregateReportElement> reportElements = (List<AssignmentAggregateReportElement>) reportData.getReportElements();

        Collections.sort(reportElements, new Comparator<AssignmentAggregateReportElement>() {
            @Override
            public int compare(AssignmentAggregateReportElement o1, AssignmentAggregateReportElement o2) {
                return o1.getProjectAssignment().getUser().compareTo(o2.getProjectAssignment().getUser());
            }
        });

        return new ReportData(reportData.getLockedDays(), reportElements, reportData.getReportRange(), reportCriteria.getUserSelectedCriteria());
    }

    @Override
    public ReportNodeFactory<AssignmentAggregateReportElement> getReportNodeFactory() {
        return new ReportNodeFactory<AssignmentAggregateReportElement>() {
            @Override
            public ReportNode createReportNode(AssignmentAggregateReportElement aggregate, int hierarchyLevel) {
                switch (hierarchyLevel) {
                    case 0:
                        return new UserNode(aggregate);
                    case 1:
                        return new CustomerNode(aggregate);
                    case 2:
                        return new ProjectEndNode(aggregate);
                }

                throw new RuntimeException("Hierarchy level too deep");
            }

            /**
             * Only needed for the root node, user
             */

            public Serializable getElementId(AssignmentAggregateReportElement aggregate) {
                return aggregate.getProjectAssignment().getUser().getPK();
            }
        };
    }

    private static final class UserNode extends ReportNode {
        private static final long serialVersionUID = 8534482324216994500L;

        private UserNode(AssignmentAggregateReportElement aggregate) {
            super(aggregate.getProjectAssignment().getPK());
            this.columnValues = new Serializable[]{aggregate.getProjectAssignment().getUser().getFullName(),
                    aggregate.getProjectAssignment().getRole()};
        }

        @Override
        protected Serializable getElementId(ReportElement element) {
            AssignmentAggregateReportElement aggregate = (AssignmentAggregateReportElement) element;

            return aggregate.getProjectAssignment().getPK();
        }
    }

    private static final class ProjectEndNode extends ReportNode {
        private static final long serialVersionUID = 1L;

        private Number hours;

        private ProjectEndNode(AssignmentAggregateReportElement aggregate) {
            super(aggregate.getProjectAssignment().getProject().getPK());
            hours = aggregate.getHours();

            this.columnValues = new Serializable[]{aggregate.getProjectAssignment().getProject().getName(),
                    aggregate.getProjectAssignment().getProject().getProjectCode(),
                    aggregate.getProjectAssignment().getHourlyRate(),
                    aggregate.getHours()};
        }

        @Override
        protected Serializable getElementId(ReportElement element) {
            AssignmentAggregateReportElement aggregate = (AssignmentAggregateReportElement) element;

            return aggregate.getProjectAssignment().getProject().getPK();
        }

        @Override
        public Number getHours() {
            return hours;
        }

        @Override
        protected boolean isLeaf() {
            return true;
        }
    }
}
