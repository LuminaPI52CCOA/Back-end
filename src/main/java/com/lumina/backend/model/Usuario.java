package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    @ManyToOne
    @JoinColumn(name = "fk_perfil")
    private Perfil perfil;
    private String cro;
    private Boolean ativo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + (this.perfil != null && this.perfil.getNome() != null ? this.perfil.getNome().toUpperCase() : "USER")));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public boolean estaAtivo() {
        return Boolean.TRUE.equals(this.ativo);
    }
}
