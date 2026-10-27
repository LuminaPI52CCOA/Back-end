package com.lumina.backend.repository;

import com.lumina.backend.model.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Modifying
    @Transactional
    @Query("""
UPDATE Usuario u SET
u.ativo = :ativo
WHERE u.idUsuario = :id
""")
    int logicalDelete(@Param("ativo") Boolean ativo,
                        @Param("id") Long id);

    Optional<Usuario> findByEmail(String username);

    Page<Usuario> findByPerfil_IdPerfil(Integer idPerfil, Pageable pageable);

    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Usuario> findByPerfil_IdPerfilAndNomeContainingIgnoreCase(Integer idPerfil, String nome, Pageable pageable);

    @Query("""
        SELECT u FROM Usuario u
        WHERE UPPER(u.perfil.nome) = 'DENTISTA'
          AND (u.ativo IS NULL OR u.ativo = true)
          AND (:nome IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
    """)
    Page<Usuario> buscarDentistasAtivos(@Param("nome") String nome, Pageable pageable);

    /**
     * Bloqueia (SELECT ... FOR UPDATE) a linha do dentista para serializar agendamentos
     * concorrentes na agenda dele, inclusive quando ainda não existe consulta no dia.
     * Usa SQL nativo para travar somente a tabela usuario (sem o JOIN EAGER com perfil).
     */
    @org.springframework.transaction.annotation.Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    @Query(value = "SELECT id_usuario FROM usuario WHERE id_usuario = :idUsuario FOR UPDATE", nativeQuery = true)
    List<Number> bloquearParaAgendamento(@Param("idUsuario") Long idUsuario);
}
