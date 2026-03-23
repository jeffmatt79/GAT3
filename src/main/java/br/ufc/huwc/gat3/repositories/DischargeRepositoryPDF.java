package br.ufc.huwc.gat3.repositories;

import br.ufc.huwc.gat3.model.Discharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DischargeRepositoryPDF extends JpaRepository<Discharge, Long> {

    Optional<Discharge> findByAghuDischargeId(Long aghuDischargeId);

}

