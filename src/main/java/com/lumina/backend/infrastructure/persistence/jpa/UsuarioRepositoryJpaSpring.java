package com.lumina.backend.infrastructure.persistence.jpa;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepositoryJpaSpring extends JpaRepository<UsuarioJpaEntity, Long> {

    @Modifying
    @Transactional
    @Query("""
    UPDATE UsuarioJpaEntity u SET
    u.ativo = :ativo
    WHERE u.idUsuario = :id
    """)
    int logicalDelete(@Param("ativo") Boolean ativo, @Param("id") Long id);

    Optional<UsuarioJpaEntity> findByEmail(String email);

    Page<UsuarioJpaEntity> findByPerfil_IdPerfil(Integer idPerfil, Pageable pageable);

    Page<UsuarioJpaEntity> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<UsuarioJpaEntity> findByPerfil_IdPerfilAndNomeContainingIgnoreCase(Integer idPerfil, String nome, Pageable pageable);
}