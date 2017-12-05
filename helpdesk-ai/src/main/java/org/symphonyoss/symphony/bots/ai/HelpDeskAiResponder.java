package org.symphonyoss.symphony.bots.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.symphony.bots.ai.impl.SymphonyAiMessage;
import org.symphonyoss.symphony.bots.ai.impl.SymphonyAiResponder;
import org.symphonyoss.symphony.bots.ai.message.MessageProducer;
import org.symphonyoss.symphony.bots.helpdesk.service.membership.client.MembershipClient;
import org.symphonyoss.symphony.clients.MessagesClient;
import org.symphonyoss.symphony.clients.UsersClient;

/**
 * Created by rsanchez on 30/11/17.
 */
public class HelpDeskAiResponder extends SymphonyAiResponder {

  private static final Logger LOGGER = LoggerFactory.getLogger(HelpDeskAiResponder.class);

  private final MessageProducer messageProducer;

  public HelpDeskAiResponder(MessagesClient messagesClient, MembershipClient membershipClient, UsersClient usersClient) {
    super(messagesClient);
    this.messageProducer = new MessageProducer(messagesClient, membershipClient, usersClient);
  }

  @Override
  protected void publishMessage(AiResponseIdentifier respond, SymphonyAiMessage symphonyAiMessage) {
    try {
      messageProducer.publishMessage(symphonyAiMessage, respond.getResponseIdentifier());
    } catch (MessagesException e) {
      LOGGER.error("Ai could not send message: ", e);
    }
  }

}
