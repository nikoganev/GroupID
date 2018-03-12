package org.symphonyoss.symphony.bots.helpdesk.messageproxy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.RoomException;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.symphony.clients.model.SymRoomAttributes;

/**
 * Created by rsanchez on 01/12/17.
 */
@Service
public class RoomService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomService.class);

  private final SymphonyClient symphonyClient;

  public RoomService(SymphonyClient symphonyClient) {
    this.symphonyClient = symphonyClient;
  }

  /**
   * Creates a new service stream for a ticket.
   * @param ticketId the ticket ID to create the service stream for
   * @param groupId group Id
   * @param podName Pod Name
   * @param viewHistory View history flag
   * @return the created stream
   */
  private Room newServiceStream(String ticketId, String groupId, String podName,
      Boolean viewHistory)
      throws RoomException {
    SymRoomAttributes roomAttributes = new SymRoomAttributes();

    roomAttributes.setCreatorUser(symphonyClient.getLocalUser());
    roomAttributes.setDescription("Service room for ticket " + ticketId + ".");
    roomAttributes.setDiscoverable(false);
    roomAttributes.setMembersCanInvite(true);
    roomAttributes.setName(buildRoomName(podName, groupId, ticketId));
    roomAttributes.setReadOnly(false);
    roomAttributes.setPublic(false);
    roomAttributes.setViewHistory(viewHistory);

    Room room = symphonyClient.getRoomService().createRoom(roomAttributes);
    room.getRoomDetail().getRoomAttributes().setViewHistory(viewHistory);
    LOGGER.info("Created new room: " + roomAttributes.getName());

    return room;
  }

  /**
   * Builds a valid room name within the maximum number of characters
   * @param podName the Pod name to be displayed
   * @param groupId the GroupId
   * @param ticketId the TicketId
   * @return the built Room Name
   */
  private String buildRoomName(String podName, String groupId, String ticketId) {
    int availableChars = 50 - ("[] Ticket Room #" + ticketId).length();

    if (podName != null) {
      availableChars -= "[] ".length();

      podName = truncate(podName, Math.max(availableChars / 2, availableChars - groupId.length()));
      groupId = truncate(groupId, availableChars - podName.length());

      return "[" + podName + "] [" + groupId + "] Ticket Room #" + ticketId;
    } else {
      groupId = truncate(groupId, availableChars);

      return "[" + groupId + "] Ticket Room #" + ticketId;
    }
  }

  /**
   * Truncates a string with a given length
   * @param string the string to be truncated
   * @param length the maximum length
   * @return the truncated string
   */
  private String truncate(String string, int length) {
    if (string.length() > length) {
      return string.substring(0, length);
    } else {
      return string;
    }
  }

  /**
   * Creates a new service stream for a ticket. There is a retry behavior to avoid errors when
   * the POD doesn't support to create private room with the view history flag set to TRUE.
   * @param ticketId the ticket ID to create the service stream for
   * @param groupId group Id
   * @return the created stream
   */
  public Room createServiceStream(String ticketId, String groupId) throws RoomException {
    return createServiceStream(ticketId, groupId, null);
  }

  /**
   * Creates a new service stream for a ticket. There is a retry behavior to avoid errors when
   * the POD doesn't support to create private room with the view history flag set to TRUE.
   * @param ticketId the ticket ID to create the service stream for
   * @param groupId group Id
   * @param podName Pod Name
   * @return the created stream
   */
  public Room createServiceStream(String ticketId, String groupId, String podName)
      throws RoomException {
    try {
      return newServiceStream(ticketId, groupId, podName, Boolean.TRUE);
    } catch (RoomException e) {
      return newServiceStream(ticketId, groupId, podName, Boolean.FALSE);
    }
  }
}
