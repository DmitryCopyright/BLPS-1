package dmitryv.lab1.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AutomoderateReport")
public class AutomoderateReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long report_id;

    @Column(nullable = false)
    private Long user_id;

    @Column(nullable = false)
    private Boolean passed;

    @Column(nullable = false)
    private LocalDateTime checked_at = LocalDateTime.now();

    public AutomoderateReport() {
    }

    public AutomoderateReport(Long userId, Boolean passed) {
        this.user_id = userId;
        this.passed = passed;
    }

    // Getters and setters
    public Long getReportId() {
        return report_id;
    }

    public void setReportId(Long reportId) {
        this.report_id = reportId;
    }

    public Long getUserId() {
        return user_id;
    }

    public void setUserId(Long userId) {
        this.user_id = userId;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public LocalDateTime getCheckedAt() {
        return checked_at;
    }

    public void setCheckedAt(LocalDateTime checked_at) {
        this.checked_at = checked_at;
    }
}
