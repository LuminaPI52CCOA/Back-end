package com.lumina.backend.repository;

import com.lumina.backend.model.TipoMidia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoMidiaRepository extends JpaRepository<TipoMidia, Long> {

    @Query("SELECT t FROM TipoMidia t WHERE t.ativo = true AND (t.usuario IS NULL OR t.usuario.idUsuario = :usuarioId) ORDER BY t.idTipoMidia ASC")
    List<TipoMidia> findDisponiveisPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT t FROM TipoMidia t WHERE t.ativo = true AND t.usuario IS NULL ORDER BY t.idTipoMidia ASC")
    List<TipoMidia> findGlobais();

    Optional<TipoMidia> findByIdTipoMidiaAndAtivoTrue(Long idTipoMidia);
}
