package it.hackhub.model;

import it.hackhub.model.enums.RequestState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "call_requests")
@Getter @Setter
@NoArgsConstructor
public class CallRequest extends Request {

    @ManyToOne(optional = false)
    private SupportChat supportChat;

    @Column(nullable = false)
    private LocalDateTime proposedTime;

    @Column(length = 500)
    private String topic;

    public CallRequest(SupportChat supportChat, LocalDateTime proposedTime, String topic) {
        this.supportChat = supportChat;
        this.proposedTime = proposedTime;
        this.topic = topic;
        this.state = RequestState.PENDING;
    }
}