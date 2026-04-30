package com.lumina.backend.dto.anamnese;

import java.time.LocalDate;

public class AnamneseResponse {
    private Long idAnamnese;
    private AnamneseCliente anamneseCliente;
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

    public static class AnamneseCliente {
        private Integer idCliente;
        private String nome;
        private String cpf;
        private String rg;
        private LocalDate dataNascimento;
        private String numeroCelular;
        private String email;
        private Character sexo;
        private String naturalidade;
        private String nacionalidade;
        private Integer fkEstadoCivil;
        private String enderecoResidencial;
        private String cep;
        private Integer fkClienteIndicacao;
        private Integer fkResponsavel;
        private String grauParentescoResponsavel;

        public Integer getIdCliente() {
            return idCliente;
        }

        public void setIdCliente(Integer idCliente) {
            this.idCliente = idCliente;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCpf() {
            return cpf;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }

        public String getRg() {
            return rg;
        }

        public void setRg(String rg) {
            this.rg = rg;
        }

        public LocalDate getDataNascimento() {
            return dataNascimento;
        }

        public void setDataNascimento(LocalDate dataNascimento) {
            this.dataNascimento = dataNascimento;
        }

        public String getNumeroCelular() {
            return numeroCelular;
        }

        public void setNumeroCelular(String numeroCelular) {
            this.numeroCelular = numeroCelular;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Character getSexo() {
            return sexo;
        }

        public void setSexo(Character sexo) {
            this.sexo = sexo;
        }

        public String getNaturalidade() {
            return naturalidade;
        }

        public void setNaturalidade(String naturalidade) {
            this.naturalidade = naturalidade;
        }

        public String getNacionalidade() {
            return nacionalidade;
        }

        public void setNacionalidade(String nacionalidade) {
            this.nacionalidade = nacionalidade;
        }

        public Integer getFkEstadoCivil() {
            return fkEstadoCivil;
        }

        public void setFkEstadoCivil(Integer fkEstadoCivil) {
            this.fkEstadoCivil = fkEstadoCivil;
        }

        public String getEnderecoResidencial() {
            return enderecoResidencial;
        }

        public void setEnderecoResidencial(String enderecoResidencial) {
            this.enderecoResidencial = enderecoResidencial;
        }

        public String getCep() {
            return cep;
        }

        public void setCep(String cep) {
            this.cep = cep;
        }

        public Integer getFkClienteIndicacao() {
            return fkClienteIndicacao;
        }

        public void setFkClienteIndicacao(Integer fkClienteIndicacao) {
            this.fkClienteIndicacao = fkClienteIndicacao;
        }

        public Integer getFkResponsavel() {
            return fkResponsavel;
        }

        public void setFkResponsavel(Integer fkResponsavel) {
            this.fkResponsavel = fkResponsavel;
        }

        public String getGrauParentescoResponsavel() {
            return grauParentescoResponsavel;
        }

        public void setGrauParentescoResponsavel(String grauParentescoResponsavel) {
            this.grauParentescoResponsavel = grauParentescoResponsavel;
        }
    }

    public AnamneseResponse(){

    }

    public AnamneseResponse(Long idAnamnese, AnamneseCliente anamneseCliente, LocalDate dataAnamnese, Boolean fazendoTratamento, String descricaoTratamento, Boolean doresCabecaFaceAtm, Boolean alergiaMedicamentos, String descricaoAlergiaMedicamentos, Boolean reacaoAnestesiaLocal, Boolean sensibilidadeDentaria, Boolean bruxismoApertamento, Boolean sangramentoGengival, Boolean possuiHabito, String descricaoHabito, Boolean historicoDiabetes, Boolean sangramentoExcessivo, Boolean problemaCardiaco, String descricaoProblemaCardiaco, Boolean pressaoArterialNormal, String descricaoPressaoArterial, Boolean historicoDesmaioConvulsao, Boolean gestante) {
        this.idAnamnese = idAnamnese;
        this.anamneseCliente = anamneseCliente;
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

    public AnamneseCliente getAnamneseCliente() {
        return anamneseCliente;
    }

    public void setAnamneseCliente(AnamneseCliente anamneseCliente) {
        this.anamneseCliente = anamneseCliente;
    }

    public Long getIdAnamnese() {
        return idAnamnese;
    }

    public void setIdAnamnese(Long idAnamnese) {
        this.idAnamnese = idAnamnese;
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
