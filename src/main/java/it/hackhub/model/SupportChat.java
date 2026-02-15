package it.hackhub.model;

import it.hackhub.model.accounts.StaffProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_chats")
@Getter @Setter
@NoArgsConstructor
public class SupportChat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ParticipatingTeam team;

    @ManyToOne(optional = false)
    private StaffProfile mentor;

    @OneToMany(mappedBy = "supportChat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    public SupportChat(ParticipatingTeam team, StaffProfile mentor) {
        this.team = team;
        this.mentor = mentor;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        message.setSupportChat(this);
    }
}