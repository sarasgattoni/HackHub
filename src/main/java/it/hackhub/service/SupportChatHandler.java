package it.hackhub.service;

import it.hackhub.model.Message;
import it.hackhub.model.SupportChat;
import it.hackhub.model.enums.ChatParticipantType;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.SupportChatRepository;

import java.util.List;

public class SupportChatHandler {

    private final SupportChatRepository chatRepo = new SupportChatRepository();

    public void sendMessage(Long chatId, Long senderId, ChatParticipantType senderType, String content) {
        HibernateExecutor.executeVoidTransaction(session -> {

            SupportChat chat = chatRepo.findById(session, chatId)
                    .orElseThrow(() -> new IllegalArgumentException("Support Chat not found"));

            Message message = new Message(content, senderType, senderId);

            chat.addMessage(message);

            chatRepo.save(session, chat);
        });
    }

    public List<Message> getChatHistory(Long chatId) {
        return HibernateExecutor.execute(session -> {
            SupportChat chat = chatRepo.findById(session, chatId)
                    .orElseThrow(() -> new IllegalArgumentException("Support Chat not found"));

            List<Message> messages = chat.getMessages();
            messages.size();

            return messages;
        });
    }
}