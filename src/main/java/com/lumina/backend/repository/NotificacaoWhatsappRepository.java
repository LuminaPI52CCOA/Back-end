package com.lumina.backend.repository;

import com.lumina.backend.model.NotificacaoWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacaoWhatsappRepository extends JpaRepository<NotificacaoWhatsapp, Long> {

    Optional<NotificacaoWhatsapp> findByTokenConfirmacao(String tokenConfirmacao);

    Optional<NotificacaoWhatsapp> findByConsultaIdConsulta(Long idConsulta);

    boolean existsByConsultaIdConsulta(Long idConsulta);

    List<NotificacaoWhatsapp> findByDataConsultaOrderByHorarioConsultaAsc(LocalDate dataConsulta);

    List<NotificacaoWhatsapp> findByDataConsultaAndStatusIgnoreCaseOrderByHorarioConsultaAsc(
            LocalDate dataConsulta,
            String status
    );
}
