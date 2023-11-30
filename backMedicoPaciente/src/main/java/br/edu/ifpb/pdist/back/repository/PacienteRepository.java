package br.edu.ifpb.pdist.back.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pdist.back.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    Optional<Paciente> findByCpf(String string);

}
