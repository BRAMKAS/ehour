package net.rrm.ehour.ui.report.project;

import com.google.common.base.Optional;
import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.ui.common.model.KeyResourceModel;
import net.rrm.ehour.ui.report.builder.ReportFactory;
import net.rrm.ehour.ui.report.builder.ReportTabFactory;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;

@ReportFactory
public class ProjectReportTabFactory implements ReportTabFactory {
    @Override
    public Optional<ITab> createReportTab(final ReportCriteria criteria) {
        AbstractTab abstractTab = new AbstractTab(new KeyResourceModel("report.title.project")) {
            @Override
            public Panel getPanel(String panelId) {
                return getProjectReportPanel(panelId, criteria);
            }
        };

        return Optional.<ITab>of(abstractTab);
    }

    @Override
    public int getRenderPriority() {
        return 1;
    }

    private Panel getProjectReportPanel(String id, ReportCriteria reportCriteria) {
        ProjectAggregateReportModel aggregateReport = new ProjectAggregateReportModel(reportCriteria);
        return new ProjectReportPanel(id, aggregateReport);
    }
}
