package com.lumina.backend.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryJpaSpring extends JpaRepository<ClienteJpaEntity, Long> {
    boolean existsByCpf(String cpf);
    Optional<ClienteJpaEntity> findByEmail(String email);
    Optional<ClienteJpaEntity> findByCpf(String cpf);
    List<ClienteJpaEntity> findByNomeContainingIgnoreCase(String nome);
    Page<ClienteJpaEntity> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("""
            SELECT c FROM ClienteJpaEntity c
            WHERE c.idCliente IN (
                SELECT co.cliente.idCliente FROM Consulta co WHERE co.usuario.idUsuario = :idUsuario
            )
            AND (:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            AND (:cpf IS NULL OR c.cpf = :cpf)
            """)
    Page<ClienteJpaEntity> buscarDoDentista(@Param("idUsuario") Long idUsuario,
                                            @Param("nome") String nome,
                                            @Param("cpf") String cpf,
                                            Pageable pageable);
}