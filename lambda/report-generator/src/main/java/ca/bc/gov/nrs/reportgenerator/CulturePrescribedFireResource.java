package ca.bc.gov.nrs.reportgenerator;

import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;

import ca.bc.gov.nrs.reportgenerator.model.CulturePrescribedFireReportData;
import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Path("/culture-prescribed-fire")
public class CulturePrescribedFireResource {
    private static final Logger LOG = Logger.getLogger(CulturePrescribedFireResource.class);

    private static final String TEST_REPORT_NAME = "WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jasper";

    @Inject
    ReadOnlyStreamingService repo;

    @Inject
    JasperReportService jasperReportService;

    @POST
    @Consumes("application/json")
    @Produces(ExtendedMediaType.APPLICATION_XLSX)
    public Response generateXlsx(List<CulturePrescribedFireReportData> fields) {
        LOG.info("Received XLSX generation request with fields: " + fields);
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(fields);
            JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME, new HashMap<>(), dataSource);
            byte[] xlsxBytes = jasperReportService.exportXlsx(jasperPrint).toByteArray();
            LOG.info("Exported XLSX successfully.");
            return Response.ok(xlsxBytes)
                .type(ExtendedMediaType.APPLICATION_XLSX)
                .header("Content-Disposition", "attachment; filename=wfprev_culture_prescribed_fire.xlsx")
                .build();
        } catch (Exception e) {
            LOG.error("Error generating XLSX report", e);
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Error: ").append(e.getMessage()).append("\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                errorMsg.append(ste.toString()).append("\n");
            }
            return Response.serverError().entity(errorMsg.toString()).build();
        }
    }
}
