package dmitryv.lab1.services;

import org.springframework.stereotype.Service;
import dmitryv.lab1.models.Message;

import java.util.HashSet;
import java.util.Set;

@Service
public class ModeratorService {

    private static final Set<String> forbiddenWords = new HashSet<>();

    static {
        forbiddenWords.add("dopsa");
        forbiddenWords.add("psj");
        forbiddenWords.add("badword");
    }

    public static boolean moderate(Message message) {
        String messageText = message.getTextMessage().toLowerCase();
        for (String word : forbiddenWords) {
            if (messageText.contains(word)) {
                return false;
            }
        }
        return true;
    }
}
