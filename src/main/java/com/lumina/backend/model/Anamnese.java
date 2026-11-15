package com.lumina.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "anamnese")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Anamnese {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnamnese;

    @ManyToOne
    @JoinColumn(name = "fk_cliente")
    private Cliente fkCliente;
    private LocalDate dataAnamnese;
    private Boolean fazendoTratamento;
    private String descricaoTratamento;
    private Boolean doresCabecaFaceAtm;
    @Column(name = "alergia_medicamentosa")
    private Boolean alergiaMedicamentos;
    @Column(name = "descricao_alergia_medicamentosa")
    private String descricaoAlergiaMedicamentos;
    private Boolean reacaoAnestesiaLocal;
    private Boolean sensibilidadeDentaria;
    private Boolean bruxismoApertamento;
    private Boolean sangramentoGengival;
    private Boolean possuiHabito;
    private String descricaoHabito;
    private Boolean historicoDiabetes;
    private Boolean sangramentoExcessivo;
    private Boolean problemaCardiaco;
    private String descricaoProblemaCardiaco;
    private Boolean pressaoArterialNormal;
    private String descricaoPressaoArterial;
    private Boolean historicoDesmaioConvulsao;
    private Boolean gestante;
}
