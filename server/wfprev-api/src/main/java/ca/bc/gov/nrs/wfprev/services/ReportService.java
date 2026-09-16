package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.CulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.FuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFiscalEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.data.repositories.CulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.FuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class ReportService {

    @Value("${spring.application.baseUrl}")
    private String baseUrl;

    private static final String PROJECT_URL_PREFIX = "/edit-project?projectGuid=";

    private static final String FISCAL_QUERY_STRING = "&tab=fiscal&fiscalGuid=";

    private final FuelManagementReportRepository fuelManagementRepository;
    private final CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository;
    private final ProgramAreaRepository programAreaRepository;
    private final FeaturesService featuresService;
    private final CsvReportGenerator csvReportGenerator;
    private final XlsxReportGenerator xlsxReportGenerator;

    public ReportService(FuelManagementReportRepository fuelManagementRepository,
                         CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository,
                         ProgramAreaRepository programAreaRepository,
                         FeaturesService featuresService,
                         CsvReportGenerator csvReportGenerator,
                         XlsxReportGenerator xlsxReportGenerator) {
        this.fuelManagementRepository = fuelManagementRepository;
        this.culturalPrescribedFireReportRepository = culturalPrescribedFireReportRepository;
        this.programAreaRepository = programAreaRepository;
        this.featuresService = featuresService;
        this.csvReportGenerator = csvReportGenerator;
        this.xlsxReportGenerator = xlsxReportGenerator;
    }

    public ReportService(FuelManagementReportRepository fuelManagementRepository,
                         CulturalPrescribedFireReportRepository culturalPrescribedFireReportRepository,
                         ProgramAreaRepository programAreaRepository,
                         FeaturesService featuresService) {
        this(fuelManagementRepository, culturalPrescribedFireReportRepository, programAreaRepository, featuresService,
                new CsvReportGenerator(), new XlsxReportGenerator());
    }

    public CsvReportGenerator getCsvReportGenerator() {
        return csvReportGenerator;
    }

    public XlsxReportGenerator getXlsxReportGenerator() {
        return xlsxReportGenerator;
    }

    public static class ReportDataBundle {
        List<FuelManagementReportEntity> fuel;
        List<CulturalPrescribedFireReportEntity> crx;

        ReportDataBundle(List<FuelManagementReportEntity> fuel,
                         List<CulturalPrescribedFireReportEntity> crx) {
            this.fuel = fuel;
            this.crx = crx;
        }

        public List<FuelManagementReportEntity> getFuel() {
            return fuel;
        }

        public List<CulturalPrescribedFireReportEntity> getCrx() {
            return crx;
        }
    }

    private ReportDataBundle resolveReportData(ReportRequestModel request) {
        List<FuelManagementReportEntity> fuel = new ArrayList<>();
        List<CulturalPrescribedFireReportEntity> crx = new ArrayList<>();

        if (request == null || ((request.getProjects() == null || request.getProjects().isEmpty()) && request.getProjectFilter() == null)) {
            throw new IllegalArgumentException("At least one project or a filter is required");
        }

        List<ReportRequestModel.Project> projectsToReport = request.getProjects();

        if (projectsToReport == null || projectsToReport.isEmpty()) {
            // Fetch projects using filter
            var entities = featuresService.findFilteredProjects(request.getProjectFilter(), 1, Integer.MAX_VALUE, null, null);
            projectsToReport = new ArrayList<>();
            for (var entity : entities) {
                ReportRequestModel.Project p = new ReportRequestModel.Project();
                p.setProjectGuid(entity.getProjectGuid());

                FeatureQueryParams filter = request.getProjectFilter();
                List<ProjectFiscalEntity> fiscals = featuresService.findFilteredProjectFiscals(
                        entity.getProjectGuid(),
                        filter.getFiscalYears(),
                        filter.getActivityCategoryCodes(),
                        filter.getPlanFiscalStatusCodes()
                );

                if (!fiscals.isEmpty()) {
                    p.setProjectFiscalGuids(fiscals.stream()
                            .map(ProjectFiscalEntity::getProjectPlanFiscalGuid)
                            .toList());
                } else { 
                    p.setProjectFiscalGuids(new ArrayList<>());
                }
                
                projectsToReport.add(p);
            }
        }

        for (ReportRequestModel.Project p : projectsToReport) {
            UUID projectGuid = Objects.requireNonNull(p.getProjectGuid(), "projectGuid is required");
            List<UUID> fiscals = p.getProjectFiscalGuids();

            if (fiscals != null && !fiscals.isEmpty()) {
                crx.addAll(culturalPrescribedFireReportRepository
                        .findByProjectGuidAndProjectPlanFiscalGuidIn(projectGuid, fiscals));
                fuel.addAll(fuelManagementRepository
                        .findByProjectGuidAndProjectPlanFiscalGuidIn(projectGuid, fiscals));
            } else {
                // Now includes rows where project_plan_fiscal_guid IS NULL
                crx.addAll(culturalPrescribedFireReportRepository.findByProjectGuid(projectGuid));
                fuel.addAll(fuelManagementRepository.findByProjectGuid(projectGuid));
            }
        }

        return new ReportDataBundle(fuel, crx);
    }

    private ReportDataBundle getPreparedReportData(ReportRequestModel request) {
        ReportDataBundle data = resolveReportData(request);

        // Remove nulls up front (defensive)
        data.fuel.removeIf(Objects::isNull);
        data.crx.removeIf(Objects::isNull);

        // Pre-process
        data.fuel.forEach(this::setFuelManagementFields);
        data.crx.forEach(this::setCrxFields);

        // If absolutely nothing to write, fail early
        if (data.fuel.isEmpty() && data.crx.isEmpty()) {
            throw new IllegalArgumentException("No fiscal data found for the provided projects");
        }

        return data;
    }

    public void exportXlsx(ReportRequestModel request, OutputStream outputStream)
            throws ServiceException, IOException, InterruptedException {
        ReportDataBundle data = getPreparedReportData(request);
        xlsxReportGenerator.generateXlsx(data.fuel, data.crx, outputStream);
    }

    public void writeCsvZipFromEntities(ReportRequestModel request, OutputStream zipOutStream) throws ServiceException {
        ReportDataBundle data = getPreparedReportData(request);
        csvReportGenerator.generateCsvZip(data.fuel, data.crx, zipOutStream);
    }

    private void setFuelManagementFields(FuelManagementReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity != null) {
            if (entity.getProjectGuid() != null) {
                entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
            }

            if (entity.getProjectPlanFiscalGuid() != null) {
                entity.setLinkToFiscalActivity(
                        urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());
            }

            if (entity.getProgramAreaGuid() != null) {
                programAreaRepository.findById(entity.getProgramAreaGuid())
                        .ifPresent(programArea -> entity.setBusinessArea(programArea.getProgramAreaName()));
            }

            // 2025 -> 2025/26 format
            entity.setFiscalYear(formatFiscalYearIfNumeric(entity.getFiscalYear()));
        }
    }

    private void setCrxFields(CulturalPrescribedFireReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity != null) {
            if (entity.getProjectGuid() != null) {
                entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
                if (entity.getProjectPlanFiscalGuid() != null) {
                    entity.setLinkToFiscalActivity(
                            urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());
                }
            }

            if (entity.getProgramAreaGuid() != null) {
                programAreaRepository.findById(entity.getProgramAreaGuid())
                        .ifPresent(programArea -> entity.setBusinessArea(programArea.getProgramAreaName()));
            }

            // 2025 -> 2025/26 format
            entity.setFiscalYear(formatFiscalYearIfNumeric(entity.getFiscalYear()));
        }
    }

    private String formatFiscalYearIfNumeric(String fiscalYear) {
        if (fiscalYear == null)
            return "";
        try {
            int year = Integer.parseInt(fiscalYear);
            return year + "/" + String.format("%02d", (year + 1) % 100);
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
