package dmitryv.lab1.controllers;

import dmitryv.lab1.models.Topic;
import dmitryv.lab1.repos.TopicRepo;
import dmitryv.lab1.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import dmitryv.lab1.models.Message;
import dmitryv.lab1.models.User;
import dmitryv.lab1.requests.MessageEditReq;
import dmitryv.lab1.requests.MessageReq;
import dmitryv.lab1.requests.MessageReqFilters;
import dmitryv.lab1.responses.MessageRes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/message")
public class MessageController {

    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ModeratorService moderatorService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TopicService topicService;
    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "Get all messages", description = "Retrieve a list of all messages")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of messages",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MessageRes.class))})
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_MODERATOR')")
    @GetMapping(path = "all", produces = "application/json")
    public ResponseEntity<MessageRes> getAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userService.get(username);

        if (userOptional.isPresent()) {
            List<Message> messages = messageService.getAll();
            return new ResponseEntity<>(MessageRes.builder().messages(messages).build(), HttpStatus.OK);
        } else {

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_MODERATOR') or (hasAuthority('ROLE_USER') and #userId == principal.userId)")
    @GetMapping(path = "user/{userId}", produces = "application/json")
    public ResponseEntity<MessageRes> getForUser(@PathVariable long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> requester = userService.get(username);

        if (!requester.isPresent()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Message> messages = messageService.getMessagesByUserId(userId);
        return new ResponseEntity<>(MessageRes.builder().messages(messages).build(), HttpStatus.OK);
    }


    @PreAuthorize("hasAnyRole('USER', 'MODERATOR')")
    @PostMapping(path = "filtered", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageRes> getFilteredMessages(@Valid @RequestBody MessageReqFilters req) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userService.get(username);

        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(MessageRes.builder().msg("User not found").build(), HttpStatus.UNAUTHORIZED);
        }

        User user = userOptional.get();
        boolean isModerator = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().contains("MODERATOR"));

        List<Message> messages;

        if (isModerator) {

            messages = messageService.getFilteredMessages(req, Optional.empty());
        } else {

            messages = messageService.getFilteredMessages(req, Optional.of(user.getUserId()));
        }

        return new ResponseEntity<>(MessageRes.builder().messages(messages).build(), HttpStatus.ACCEPTED);
    }



    @Operation(summary = "Add a new message", description = "Add a new message with the given details")
    @ApiResponse(responseCode = "201", description = "Message created")
    @ApiResponse(responseCode = "418", description = "Message contains forbidden words")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping(path = "add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addMessage(@RequestBody MessageReq messageReq) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userService.get(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();

        Message newMessage = new Message();
        newMessage.setTextMessage(messageReq.getText_message());
        newMessage.setUser(user);
        newMessage.setName(user.getName());
        newMessage.setPublishedDate(LocalDateTime.now());

        Optional<Message> messageOptional = messageService.addMessage(newMessage, user, messageReq.getTopicName());

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            messagingTemplate.convertAndSend("/topic/" + messageReq.getTopicName(), message);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PreAuthorize("hasAuthority('ROLE_MODERATOR') or (hasAuthority('ROLE_USER') and @messageService.isOwner(authentication.name, #req.messageIds))")
    @DeleteMapping(path = "delete", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageRes> delete(@Valid @RequestBody MessageReq req) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userService.get(username);

        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(MessageRes.builder().msg("User not found").build(), HttpStatus.UNAUTHORIZED);
        }

        List<Long> failedDeletes = new ArrayList<>();

        for (long messageId : req.getMessageIds()) {
            Message message = messageService.get(messageId);
            if (message != null && (message.getUser().getUsername().equals(username) || userOptional.get().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MODERATOR")))) {
                messageService.deleteMessage(messageId);
            } else {
                failedDeletes.add(messageId);
            }
        }

        if (!failedDeletes.isEmpty()) {
            String failedMessageIds = failedDeletes.stream().map(String::valueOf).collect(Collectors.joining(", "));
            return new ResponseEntity<>(
                    MessageRes.builder().msg("Failed to delete messages with ID: " + failedMessageIds + ". Users can only delete their own messages unless they are moderators.").build(),
                    HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(MessageRes.builder().msg("Successfully deleted").build(), HttpStatus.OK);
    }


    @Operation(summary = "Modify a message", description = "Modify the text of an existing message")
    @ApiResponse(responseCode = "200", description = "Message successfully modified",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = MessageRes.class))})
    @ApiResponse(responseCode = "401", description = "User is unauthorized or does not exist")
    @ApiResponse(responseCode = "403", description = "Forbidden to edit message")
    @ApiResponse(responseCode = "404", description = "Message not found")
    @PreAuthorize("hasAuthority('ROLE_MODERATOR') or (hasAuthority('ROLE_USER') and @messageService.isOwner(authentication, #editReq.messageId))")
    @PutMapping(path = "modify", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageRes> modifyMessage(@Valid @RequestBody MessageEditReq editReq) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userService.get(username);

        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(MessageRes.builder().msg("User not found").build(), HttpStatus.UNAUTHORIZED);
        }

        Long userId = userOptional.get().getUserId();
        Optional<Message> messageOptional = messageService.editMessage(userId, editReq.getMessageId(), editReq.getNewText());

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            topicService.createTopicUpdateRecord(message.getTopic());
            return new ResponseEntity<>(MessageRes.builder().msg("Message successfully modified").build(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(MessageRes.builder().msg("Forbidden to edit message or message not found").build(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/topics")
    public ResponseEntity<List<Topic>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    @GetMapping("/topics/{topicName}/messages")
    public ResponseEntity<List<Message>> getMessagesByTopic(@PathVariable String topicName) {
        Optional<Topic> topic = topicRepo.findByName(topicName);
        if (topic.isPresent()) {
            return ResponseEntity.ok(new ArrayList<>(topic.get().getMessages()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}