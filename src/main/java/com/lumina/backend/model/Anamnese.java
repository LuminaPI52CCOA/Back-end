package com.lumina.backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
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
    private Boolean alergiaMedicamentos;
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

    public Anamnese(Long idAnamnese, Cliente fkCliente, LocalDate dataAnamnese, Boolean fazendoTratamento, String descricaoTratamento, Boolean doresCabecaFaceAtm, Boolean alergiaMedicamentos, String descricaoAlergiaMedicamentos, Boolean reacaoAnestesiaLocal, Boolean sensibilidadeDentaria, Boolean bruxismoApertamento, Boolean sangramentoGengival, Boolean possuiHabito, String descricaoHabito, Boolean historicoDiabetes, Boolean sangramentoExcessivo, Boolean problemaCardiaco, String descricaoProblemaCardiaco, Boolean pressaoArterialNormal, String descricaoPressaoArterial, Boolean historicoDesmaioConvulsao, Boolean gestante) {
        this.idAnamnese = idAnamnese;
        this.fkCliente = fkCliente;
        this.dataAnamnese = dataAnamnese;
        this.fazendoTratamento = fazendoTratamento;
        this.descricaoTratamento = descricaoTratamento;
        this.doresCabecaFaceAtm = doresCabecaFaceAtm;
        this.alergiaMedicamentos = alergiaMedicamentos;
        this.descricaoAlergiaMedicamentos = descricaoAlergiaMedicamentos;
        this.reacaoAnestesiaLocal = reacaoAnestesiaLocal;
        this.sensibilidadeDentaria = sensibilidadeDentaria;
        this.bruxismoApertamento = bruxismoApertamento;
        this.sangramentoGengival = sangramentoGengival;
        this.possuiHabito = possuiHabito;
        this.descricaoHabito = descricaoHabito;
        this.historicoDiabetes = historicoDiabetes;
        this.sangramentoExcessivo = sangramentoExcessivo;
        this.problemaCardiaco = problemaCardiaco;
        this.descricaoProblemaCardiaco = descricaoProblemaCardiaco;
        this.pressaoArterialNormal = pressaoArterialNormal;
        this.descricaoPressaoArterial = descricaoPressaoArterial;
        this.historicoDesmaioConvulsao = historicoDesmaioConvulsao;
        this.gestante = gestante;
    }

    public Anamnese() {}

    public Long getIdAnamnese() {
        return idAnamnese;
    }

    public void setIdAnamnese(Long idAnamnese) {
        this.idAnamnese = idAnamnese;
    }

    public Cliente getFkCliente() {
        return fkCliente;
    }

    public void setFkCliente(Cliente fkCliente) {
        this.fkCliente = fkCliente;
    }

    public LocalDate getDataAnamnese() {
        return dataAnamnese;
    }

    public void setDataAnamnese(LocalDate dataAnamnese) {
        this.dataAnamnese = dataAnamnese;
    }

    public Boolean getFazendoTratamento() {
        return fazendoTratamento;
    }

    public void setFazendoTratamento(Boolean fazendoTratamento) {
        this.fazendoTratamento = fazendoTratamento;
    }

    public String getDescricaoTratamento() {
        return descricaoTratamento;
    }

    public void setDescricaoTratamento(String descricaoTratamento) {
        this.descricaoTratamento = descricaoTratamento;
    }

    public Boolean getDoresCabecaFaceAtm() {
        return doresCabecaFaceAtm;
    }

    public void setDoresCabecaFaceAtm(Boolean doresCabecaFaceAtm) {
        this.doresCabecaFaceAtm = doresCabecaFaceAtm;
    }

    public Boolean getAlergiaMedicamentos() {
        return alergiaMedicamentos;
    }

    public void setAlergiaMedicamentos(Boolean alergiaMedicamentos) {
        this.alergiaMedicamentos = alergiaMedicamentos;
    }

    public String getDescricaoAlergiaMedicamentos() {
        return descricaoAlergiaMedicamentos;
    }

    public void setDescricaoAlergiaMedicamentos(String descricaoAlergiaMedicamentos) {
        this.descricaoAlergiaMedicamentos = descricaoAlergiaMedicamentos;
    }

    public Boolean getReacaoAnestesiaLocal() {
        return reacaoAnestesiaLocal;
    }

    public void setReacaoAnestesiaLocal(Boolean reacaoAnestesiaLocal) {
        this.reacaoAnestesiaLocal = reacaoAnestesiaLocal;
    }

    public Boolean getSensibilidadeDentaria() {
        return sensibilidadeDentaria;
    }

    public void setSensibilidadeDentaria(Boolean sensibilidadeDentaria) {
        this.sensibilidadeDentaria = sensibilidadeDentaria;
    }

    public Boolean getBruxismoApertamento() {
        return bruxismoApertamento;
    }

    public void setBruxismoApertamento(Boolean bruxismoApertamento) {
        this.bruxismoApertamento = bruxismoApertamento;
    }

    public Boolean getSangramentoGengival() {
        return sangramentoGengival;
    }

    public void setSangramentoGengival(Boolean sangramentoGengival) {
        this.sangramentoGengival = sangramentoGengival;
    }

    public Boolean getPossuiHabito() {
        return possuiHabito;
    }

    public void setPossuiHabito(Boolean possuiHabito) {
        this.possuiHabito = possuiHabito;
    }

    public String getDescricaoHabito() {
        return descricaoHabito;
    }

    public void setDescricaoHabito(String descricaoHabito) {
        this.descricaoHabito = descricaoHabito;
    }

    public Boolean getHistoricoDiabetes() {
        return historicoDiabetes;
    }

    public void setHistoricoDiabetes(Boolean historicoDiabetes) {
        this.historicoDiabetes = historicoDiabetes;
    }

    public Boolean getSangramentoExcessivo() {
        return sangramentoExcessivo;
    }

    public void setSangramentoExcessivo(Boolean sangramentoExcessivo) {
        this.sangramentoExcessivo = sangramentoExcessivo;
    }

    public Boolean getProblemaCardiaco() {
        return problemaCardiaco;
    }

    public void setProblemaCardiaco(Boolean problemaCardiaco) {
        this.problemaCardiaco = problemaCardiaco;
    }

    public String getDescricaoProblemaCardiaco() {
        return descricaoProblemaCardiaco;
    }

    public void setDescricaoProblemaCardiaco(String descricaoProblemaCardiaco) {
        this.descricaoProblemaCardiaco = descricaoProblemaCardiaco;
    }

    public Boolean getPressaoArterialNormal() {
        return pressaoArterialNormal;
    }

    public void setPressaoArterialNormal(Boolean pressaoArterialNormal) {
        this.pressaoArterialNormal = pressaoArterialNormal;
    }

    public String getDescricaoPressaoArterial() {
        return descricaoPressaoArterial;
    }

    public void setDescricaoPressaoArterial(String descricaoPressaoArterial) {
        this.descricaoPressaoArterial = descricaoPressaoArterial;
    }

    public Boolean getHistoricoDesmaioConvulsao() {
        return historicoDesmaioConvulsao;
    }

    public void setHistoricoDesmaioConvulsao(Boolean historicoDesmaioConvulsao) {
        this.historicoDesmaioConvulsao = historicoDesmaioConvulsao;
    }

    public Boolean getGestante() {
        return gestante;
    }

    public void setGestante(Boolean gestante) {
        this.gestante = gestante;
    }
}
