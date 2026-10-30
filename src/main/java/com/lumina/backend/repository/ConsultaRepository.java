package com.lumina.backend.repository;

import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    @Query("""
        SELECT c FROM Consulta c 
        WHERE c.usuario.idUsuario = :idUsuario 
          AND c.data = :data 
          AND c.horarioInicio >= :horarioInicio 
          AND (c.status IS NULL OR UPPER(c.status) NOT IN ('CANCELADA', 'CANCELADO', 'FINALIZADA', 'CONCLUIDA'))
        ORDER BY c.horarioInicio ASC
    """)
    List<Consulta> findProximasConsultas(
            @Param("idUsuario") Long idUsuario,
            @Param("data") LocalDate data,
            @Param("horarioInicio") LocalTime horarioInicio
    );

    @Query("""
        SELECT c FROM Consulta c 
        WHERE c.usuario.idUsuario = :idUsuario 
          AND c.data = :data 
          AND (c.status IS NULL OR UPPER(c.status) NOT IN ('CANCELADA', 'CANCELADO', 'FINALIZADA', 'CONCLUIDA'))
        ORDER BY c.horarioInicio ASC
    """)
    List<Consulta> findConsultasDoDia(
            @Param("idUsuario") Long idUsuario,
            @Param("data") LocalDate data
    );

    @Query("""
        SELECT c FROM Consulta c 
        WHERE c.data = :data 
          AND c.horarioInicio >= :horarioInicio 
          AND c.horarioInicio <= :horarioFim 
          AND (c.lembreteEnviado = false OR c.lembreteEnviado IS NULL) 
          AND (c.status IS NULL OR UPPER(c.status) NOT IN ('CANCELADA', 'CANCELADO', 'FINALIZADA', 'CONCLUIDA'))
    """)
    List<Consulta> findConsultasParaLembrete(
            @Param("data") LocalDate data,
            @Param("horarioInicio") LocalTime horarioInicio,
            @Param("horarioFim") LocalTime horarioFim
    );

    List<Consulta> findByUsuarioIdUsuario(Long idUsuario);

    @Query("""
        SELECT c FROM Consulta c
        WHERE (:idUsuario IS NULL OR c.usuario.idUsuario = :idUsuario)
          AND (:dataInicio IS NULL OR c.data >= :dataInicio)
          AND (:dataFim IS NULL OR c.data <= :dataFim)
        ORDER BY c.data ASC, c.horarioInicio ASC
    """)
    List<Consulta> buscarPorPeriodo(
            @Param("idUsuario") Long idUsuario,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    /**
     * Intervalos que ocupam a agenda do dentista no dia. Consultas canceladas liberam o horário;
     * {@code idConsultaIgnorada} exclui a própria consulta durante um reagendamento.
     */
    @Query("""
        SELECT new com.lumina.backend.repository.IntervaloHorario(c.horarioInicio, c.horarioFim)
        FROM Consulta c
        WHERE c.usuario.idUsuario = :idUsuario
          AND c.data = :data
          AND (c.status IS NULL OR UPPER(c.status) NOT IN ('CANCELADA', 'CANCELADO'))
          AND (:idConsultaIgnorada IS NULL OR c.idConsulta <> :idConsultaIgnorada)
        ORDER BY c.horarioInicio ASC
    """)
    List<IntervaloHorario> buscarIntervalosOcupados(
            @Param("idUsuario") Long idUsuario,
            @Param("data") LocalDate data,
            @Param("idConsultaIgnorada") Long idConsultaIgnorada
    );

    /**
     * Bloqueia (SELECT ... FOR UPDATE) a linha da consulta e devolve o dentista dono dela.
     * Deve ser a primeira leitura da transação de reagendamento.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = "SELECT fk_usuario FROM consulta WHERE id_consulta = :idConsulta FOR UPDATE", nativeQuery = true)
    List<Number> bloquearParaAtualizacao(@Param("idConsulta") Long idConsulta);

    boolean existsByClienteIdClienteAndUsuarioIdUsuario(Long idCliente, Long idUsuario);

    @Query("""
        SELECT c FROM Consulta c 
        WHERE c.cliente.idCliente IN :clienteIds 
          AND c.data >= :dataAtual 
          AND (c.status IS NULL OR UPPER(c.status) NOT IN ('CANCELADA', 'CANCELADO'))
        ORDER BY c.data ASC, c.horarioInicio ASC
    """)
    List<Consulta> findConsultasFuturasPorClientes(
            @Param("clienteIds") List<Long> clienteIds,
            @Param("dataAtual") LocalDate dataAtual
    );

    @Query("""
        SELECT c FROM Consulta c
        WHERE c.data = :dataAlvo
          AND (c.status IS NULL OR UPPER(c.status) NOT IN ('CANCELADA', 'CANCELADO', 'FINALIZADA', 'CONCLUIDA'))
          AND NOT EXISTS (
              SELECT 1 FROM NotificacaoWhatsapp n
              WHERE n.consulta.idConsulta = c.idConsulta
          )
        ORDER BY c.horarioInicio ASC
    """)
    List<Consulta> findConsultasElegiveisD1SemNotificacao(
            @Param("dataAlvo") LocalDate dataAlvo
    );
}
