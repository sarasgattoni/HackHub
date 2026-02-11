package it.hackhub.model;

import it.hackhub.model.enums.ChatParticipantType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter @Setter
@NoArgsConstructor
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private SupportChat supportChat;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatParticipantType senderType;

    @Column(nullable = false)
    private Long senderId;

    public Message(String content, ChatParticipantType senderType, Long senderId) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.senderType = senderType;
        this.senderId = senderId;
    }
}