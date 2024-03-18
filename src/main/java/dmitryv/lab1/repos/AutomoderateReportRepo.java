package dmitryv.lab1.repos;

import dmitryv.lab1.models.AutomoderateReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomoderateReportRepo extends JpaRepository<AutomoderateReport, Long> {
}
