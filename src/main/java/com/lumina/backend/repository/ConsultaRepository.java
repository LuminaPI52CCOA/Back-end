package com.lumina.backend.repository;

import com.lumina.backend.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
