package com.lumina.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteConvenioRepository extends JpaRepository<ClienteConvenioRepository, Long> {

    void findByClienteIdCliente(Integer id);
}
