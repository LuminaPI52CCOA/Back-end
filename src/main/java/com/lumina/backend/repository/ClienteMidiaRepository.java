package com.lumina.backend.repository;

import com.lumina.backend.model.ClienteMidia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteMidiaRepository extends JpaRepository<ClienteMidia, Long> {

    Optional<ClienteMidia> findByIdClienteMidiaAndAtivoTrue(Long idClienteMidia);

    Optional<ClienteMidia> findByIdClienteMidiaAndCliente_IdClienteAndAtivoTrue(Long idClienteMidia, Long clienteId);

    Optional<ClienteMidia> findByChaveS3(String chaveS3);

    @Query("""
        SELECT m FROM ClienteMidia m
        WHERE m.cliente.idCliente = :clienteId
          AND m.ativo = true
          AND m.status = 'ACTIVE'
          AND (:tipoId IS NULL OR m.tipoMidia.idTipoMidia = :tipoId)
          AND (:consultaId IS NULL OR (m.consulta IS NOT NULL AND m.consulta.idConsulta = :consultaId))
          AND (:dentistaId IS NULL OR m.usuario.idUsuario = :dentistaId)
          AND (:dataInicio IS NULL OR m.criadoEm >= :dataInicio)
          AND (:dataFim IS NULL OR m.criadoEm <= :dataFim)
        ORDER BY m.criadoEm DESC
    """)
    List<ClienteMidia> findMidiasAtivasComFiltros(
            @Param("clienteId") Long clienteId,
            @Param("tipoId") Long tipoId,
            @Param("consultaId") Long consultaId,
            @Param("dentistaId") Long dentistaId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );
}
