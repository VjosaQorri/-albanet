package com.example.albanet.ticket.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Controller for Server-Sent Events to push real-time ticket updates to dashboards
 */
@Controller
@RequestMapping("/staff/tickets")
public class TicketSseController {

    private static final Logger logger = LoggerFactory.getLogger(TicketSseController.class);

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final AdminTicketService adminTicketService;

    public TicketSseController(AdminTicketService adminTicketService) {
        this.adminTicketService = adminTicketService;
    }

    /**
     * SSE endpoint for dashboard updates
     * @param team Optional filter for specific team (IT1, IT2, FINANCE)
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTicketUpdates(@RequestParam(required = false) String team) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
        String key = team != null ? team : "ALL";

        logger.info("New SSE connection established for team: {}", key);

        // Add emitter to the list for this team
        emitters.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);
        logger.info("Active connections for team {}: {}", key, emitters.get(key).size());

        // Remove emitter when completed or timeout
        emitter.onCompletion(() -> {
            logger.info("SSE connection completed for team: {}", key);
            removeEmitter(key, emitter);
        });
        emitter.onTimeout(() -> {
            logger.warn("SSE connection timeout for team: {}", key);
            removeEmitter(key, emitter);
        });
        emitter.onError((e) -> {
            logger.error("SSE connection error for team: {}", key, e);
            removeEmitter(key, emitter);
        });

        // Send initial connection message
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to ticket stream"));
            logger.info("Initial message sent to team: {}", key);
        } catch (IOException e) {
            logger.error("Failed to send initial message to team: {}", key, e);
            removeEmitter(key, emitter);
        }

        return emitter;
    }

    /**
     * Notify all connected dashboards about a new ticket
     */
    public void notifyNewTicket(String team) {
        logger.info("Notifying new ticket for team: {}", team);
        String message = "New ticket created for " + team + " team. Refreshing...";

        // Notify team-specific listeners
        sendToTeam(team, "new-ticket", message);

        // Notify "ALL" listeners (admin dashboard)
        sendToTeam("ALL", "new-ticket", message);

        logger.info("Notification sent to team {} and ALL. Active connections - Team: {}, ALL: {}",
                    team,
                    emitters.getOrDefault(team, new CopyOnWriteArrayList<>()).size(),
                    emitters.getOrDefault("ALL", new CopyOnWriteArrayList<>()).size());
    }

    /**
     * Notify about ticket update
     */
    public void notifyTicketUpdate(Long ticketId, String team) {
        String message = "Ticket #" + ticketId + " updated";

        // Notify team-specific listeners
        sendToTeam(team, "ticket-update", message);

        // Notify "ALL" listeners
        sendToTeam("ALL", "ticket-update", message);
    }

    private void sendToTeam(String team, String eventName, String message) {
        CopyOnWriteArrayList<SseEmitter> teamEmitters = emitters.get(team);
        if (teamEmitters != null && !teamEmitters.isEmpty()) {
            logger.info("Sending {} event to {} connections for team: {}", eventName, teamEmitters.size(), team);
            int successCount = 0;
            for (SseEmitter emitter : teamEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(message));
                    successCount++;
                } catch (IOException e) {
                    logger.error("Failed to send event to emitter for team: {}", team, e);
                    removeEmitter(team, emitter);
                }
            }
            logger.info("Successfully sent {} event to {}/{} connections for team: {}",
                        eventName, successCount, teamEmitters.size(), team);
        } else {
            logger.warn("No active connections found for team: {}", team);
        }
    }

    private void removeEmitter(String key, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> teamEmitters = emitters.get(key);
        if (teamEmitters != null) {
            teamEmitters.remove(emitter);
            if (teamEmitters.isEmpty()) {
                emitters.remove(key);
            }
        }
    }
}
