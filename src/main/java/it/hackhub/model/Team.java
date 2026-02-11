package it.hackhub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
@Getter @Setter
@NoArgsConstructor
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    public Team(String name, User leader) {
        this.name = name;
        this.setLeader(leader);
    }

    public void setLeader(User user) {
        this.leader = user;
        addMember(user);
    }

    public void addMember(User user) {
        this.members.add(user);
    }

    public boolean isLeader(User user) {
        return this.leader.getId().equals(user.getId());
    }

    public int getSize() {
        return members.size();
    }
}