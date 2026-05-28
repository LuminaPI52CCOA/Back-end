package com.lumina.backend.repository;

import com.lumina.backend.model.ClienteConvenio;
import com.lumina.backend.model.Convenio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteConvenioRepository extends JpaRepository<ClienteConvenio, Long> {

    @Query("SELECT cc.convenio FROM ClienteConvenio cc WHERE cc.cliente.idCliente = :clienteId")
    List<Convenio> findConveniosByClienteId(@Param("clienteId") Long clienteId);
}
