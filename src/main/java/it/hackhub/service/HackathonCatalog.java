package it.hackhub.service;

import it.hackhub.model.Hackathon;
import it.hackhub.model.state.PendingState;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.HackathonRepository;

import java.util.List;

public class HackathonCatalog {

    private final HackathonRepository hackathonRepository = new HackathonRepository();

    public List<Hackathon> findAll() {
        return HibernateExecutor.execute(session ->
                hackathonRepository.findAll(session)
        );
    }

    public Hackathon find(Long itemId) {
        return HibernateExecutor.execute(session ->
                hackathonRepository.findById(session, itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Hackathon not found with ID: " + itemId))
        );
    }

    public List<Hackathon> findPendingApproval() {
        return HibernateExecutor.execute(session ->
                session.createQuery("FROM Hackathon h WHERE h.stateName = :state", Hackathon.class)
                        .setParameter("state", new PendingState().getName())
                        .list()
        );
    }
}