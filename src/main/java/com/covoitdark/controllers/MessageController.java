package com.covoitdark.controllers;

import com.covoitdark.dao.MessageDAO;
import com.covoitdark.models.Message;
import com.covoitdark.utils.SessionManager;

import java.util.Arrays;
import java.util.List;

// Note: direct messaging uses receiver IDs, not request IDs — authorization is by userId match

public class MessageController {

    private final MessageDAO messageDAO = new MessageDAO();

    public boolean sendMessage(int requestId, String content, boolean isQuickResponse) {
        if (content == null || content.trim().isEmpty()) return false;
        
        int senderId = SessionManager.getInstance().getCurrentUserId();
        Message msg = new Message(0, requestId, senderId, content.trim(), isQuickResponse);
        return messageDAO.create(msg);
    }

    public List<Message> getMessages(int requestId) {
        return messageDAO.findByRequest(requestId);
    }

    public List<String> getQuickResponses(boolean isDriver) {
        if (isDriver) {
            return Arrays.asList(
                "Je suis sur le point de partir.",
                "Je suis arrivé au point de rendez-vous.",
                "Peux-tu te presser un peu ?",
                "J'ai un peu de retard."
            );
        } else {
            return Arrays.asList(
                "J'arrive dans 5 minutes.",
                "Je suis au point de rendez-vous.",
                "Où êtes-vous exactement ?",
                "Merci !"
            );
        }
    }
}
