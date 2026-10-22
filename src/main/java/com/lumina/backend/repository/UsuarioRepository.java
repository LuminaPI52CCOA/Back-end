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
}
