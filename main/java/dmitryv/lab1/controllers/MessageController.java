package dmitryv.lab1.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dmitryv.lab1.models.Message;
import dmitryv.lab1.models.User;
import dmitryv.lab1.requests.MessageEditReq;
import dmitryv.lab1.requests.MessageReq;
import dmitryv.lab1.requests.MessageReqFilters;
import dmitryv.lab1.responses.MessageRes;
import dmitryv.lab1.security.JWTUtil;
import dmitryv.lab1.services.MessageService;
import dmitryv.lab1.services.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController @RequestMapping(path = "/message")
public class MessageController {

    @Autowired private JWTUtil jwtUtil;
    @Autowired private UserService userService;
    @Autowired private MessageService messageService;

    @Operation(summary = "Get all messages", description = "Retrieve a list of all messages")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of messages",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = MessageRes.class))})
    @GetMapping(path = "message/all", produces = "application/json")
    public ResponseEntity<MessageRes> getAll(HttpServletRequest rawReq) {
        String username = jwtUtil.decode(rawReq);
        Optional<User> userOptional = userService.get(username);

        if (userOptional.isPresent() && userOptional.get().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Moderator"))) {
            return new ResponseEntity<>(
                    MessageRes.builder().messages(messageService.getAll()).build(),
                    HttpStatus.OK);
        } else {
            // Возвращаем сообщения только текущего пользователя
            List<Message> userMessages = messageService.getAll().stream()
                    .filter(m -> m.getUser().getUsername().equals(username))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(
                    MessageRes.builder().messages(userMessages).build(),
                    HttpStatus.OK);
        }
    }

    @Operation(summary = "Get messages for a specific user", description = "Retrieve messages for a given user ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = MessageRes.class))})
    @ApiResponse(responseCode = "400", description = "User with the specified ID not found")
    @GetMapping(path = "user/{userId}", produces = "application/json")
    public ResponseEntity<MessageRes> getForUser(@PathVariable long userId, HttpServletRequest rawReq) {
        String username = jwtUtil.decode(rawReq);
        Optional<User> requester = userService.get(username);

        if (!requester.isPresent()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = requester.get();
        boolean isModerator = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("Moderator"));

        List<Message> messages;
        if (isModerator) {
            // Для модераторов: получаем все сообщения указанного пользователя
            messages = messageService.getMessagesByUserId(userId);
        } else if (user.getUserId() == userId) {
            // Для обычных пользователей: получаем только свои сообщения
            messages = messageService.getMessagesByUserId(userId);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(MessageRes.builder().messages(messages).build(), HttpStatus.OK);
    }


    @PostMapping(path = "filtered", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageRes> getFilteredMessages(@Valid @RequestBody MessageReqFilters req, HttpServletRequest rawReq) {
        String username = jwtUtil.decode(rawReq);
        Optional<User> userOptional = userService.get(username);

        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(MessageRes.builder().msg("User not found").build(), HttpStatus.UNAUTHORIZED);
        }

        User user = userOptional.get();
        boolean isModerator = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("Moderator"));

        List<Message> messages;

        if (isModerator) {
            // Модераторы получают доступ ко всем сообщениям
            messages = messageService.getFilteredMessages(req, Optional.empty());
        } else {
            // Ограничиваем фильтр сообщениями текущего пользователя
            messages = messageService.getFilteredMessages(req, Optional.of(user.getUserId()));
        }

        return new ResponseEntity<>(MessageRes.builder().messages(messages).build(), HttpStatus.ACCEPTED);
    }


    @Operation(summary = "Add a new message", description = "Add a new message with the given details")
    @ApiResponse(responseCode = "201", description = "Message created",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = MessageRes.class))})
    @ApiResponse(responseCode = "401", description = "User is unauthorized or does not exist")
    @ApiResponse(responseCode = "400", description = "Bad request, name not set")
    @ApiResponse(responseCode = "418", description = "I'm a teapot, moderation problem")
    @PutMapping(path = "add", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageRes> add(@Valid @RequestBody MessageReq req, HttpServletRequest rawReq) {
        return (userService.get(jwtUtil.decode(rawReq)))
                .map(u -> {
                    val message = req.toMessage();
                    Predicate<String> badField = x -> x == null || x.equals("");
                    if (badField.test(message.getName()))
                        return new ResponseEntity<>(
                                MessageRes.builder().msg("Use should set name").build(),
                                HttpStatus.BAD_REQUEST);
                    message.setUser(u);
                    return (messageService.add(message)) ?
                            new ResponseEntity<>(
                                    MessageRes.builder().msg("Your message was publish!").build(),
                                    HttpStatus.CREATED) :
                            new ResponseEntity<>(
                                    MessageRes.builder().msg("We found a problems while moderating. Please read our rules").build(),
                                    HttpStatus.I_AM_A_TEAPOT);
                        }
                ).orElse(new ResponseEntity<>(MessageRes.builder().msg("User didn't exist").build(), HttpStatus.UNAUTHORIZED));
    }

    @Operation(summary = "Delete messages", description = "Delete messages with the given IDs")
    @ApiResponse(responseCode = "200", description = "Messages successfully deleted")
    @DeleteMapping(path = "delete", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageRes> delete(@Valid @RequestBody MessageReq req, HttpServletRequest rawReq) {
        String username = jwtUtil.decode(rawReq);
        Optional<User> userOptional = userService.get(username);

        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(MessageRes.builder().msg("User not found").build(), HttpStatus.UNAUTHORIZED);
        }

        User user = userOptional.get();
        boolean isModerator = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("Moderator"));

        List<Long> failedDeletes = new ArrayList<>();

        for (long messageId : req.getMessageIds()) {
            Optional<Message> messageOptional = Optional.ofNullable(messageService.get(messageId));
            if (messageOptional.isPresent() && (isModerator || messageOptional.get().getUser().getUserId() == user.getUserId())) {
                messageService.deleteMessage(messageId);
            } else {
                failedDeletes.add(messageId);
            }
        }

        if (!failedDeletes.isEmpty()) {
            String failedMessageIds = failedDeletes.stream().map(String::valueOf).collect(Collectors.joining(", "));
            return new ResponseEntity<>(
                    MessageRes.builder().msg("Failed to delete messages with ID " + failedMessageIds + ". Users can only delete their own messages unless they are moderators.").build(),
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
    @PutMapping(path = "modify", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageRes> modifyMessage(@Valid @RequestBody MessageEditReq editReq, HttpServletRequest rawReq) {
        String token = jwtUtil.resolveToken(rawReq);
        if (token == null || !jwtUtil.validateToken(token)) {
            return new ResponseEntity<>(MessageRes.builder().msg("Token invalid or expired").build(), HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.getUsername(token); // Получаем имя пользователя из токена
        Optional<User> userOpt = userService.get(username); // Получаем объект User

        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(MessageRes.builder().msg("User not found").build(), HttpStatus.UNAUTHORIZED);
        }

        Long userId = userOpt.get().getUserId(); // Получаем ID пользователя

        boolean isEdited = messageService.editMessage(userId, editReq.getMessageId(), editReq.getNewText());
        if (isEdited) {
            return new ResponseEntity<>(MessageRes.builder().msg("Message successfully modified").build(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(MessageRes.builder().msg("Forbidden to edit message or message not found").build(), HttpStatus.FORBIDDEN);
        }
    }
}