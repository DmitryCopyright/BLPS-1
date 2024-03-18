package dmitryv.lab1.services;

import dmitryv.lab1.models.AutomoderateReport;
import dmitryv.lab1.models.User;
import dmitryv.lab1.repos.AutomoderateReportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dmitryv.lab1.models.Message;

import java.util.HashSet;
import java.util.Set;

@Service
public class ModeratorService {

    private static final Set<String> forbiddenWords = new HashSet<>();
    private final AutomoderateReportRepo reportRepository;

    @Autowired
    public ModeratorService(AutomoderateReportRepo reportRepository) {
        this.reportRepository = reportRepository;
    }

    static {
        forbiddenWords.add("dopsa");
        forbiddenWords.add("psj");
        forbiddenWords.add("badword");
    }

    public boolean moderate(Message message, User user) {
        boolean passed = true;
        String messageText = message.getTextMessage().toLowerCase();
        for (String word : forbiddenWords) {
            if (messageText.contains(word)) {
                passed = false;
                break;
            }
        }


        AutomoderateReport report = new AutomoderateReport(user.getUserId(), passed);
        reportRepository.save(report);

        return passed;
    }
}
