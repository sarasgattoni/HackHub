package it.hackhub.service;

import it.hackhub.model.CallRequest;
import it.hackhub.model.SupportChat;
import it.hackhub.model.enums.RequestState;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.CallRequestRepository;
import it.hackhub.repository.SupportChatRepository;

import java.time.LocalDateTime;

public class CallBookingHandler {

    private final CallRequestRepository callReqRepo = new CallRequestRepository();
    private final SupportChatRepository chatRepo = new SupportChatRepository();

    public void bookCall(Long chatId, LocalDateTime proposedTime, String topic) {
        HibernateExecutor.executeVoidTransaction(session -> {

            if (proposedTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Date proposed must be in the future");
            }

            SupportChat chat = chatRepo.findById(session, chatId)
                    .orElseThrow(() -> new IllegalArgumentException("Support chat not found. Impossible to book a call"));

            CallRequest request = new CallRequest(chat, proposedTime, topic);

            callReqRepo.save(session, request);
        });
    }

    public void evaluateCallRequest(Long callRequestId, boolean isAccepted) {
        HibernateExecutor.executeVoidTransaction(session -> {

            CallRequest request = callReqRepo.findById(session, callRequestId)
                    .orElseThrow(() -> new IllegalArgumentException("Call request not found"));

            if (request.getState() != RequestState.PENDING) {
                throw new IllegalStateException("This call request has already been considered");
            }

            if (isAccepted) {
                request.setState(RequestState.APPROVED);
            } else {
                request.setState(RequestState.DECLINED);
            }

            callReqRepo.save(session, request);
        });
    }
}