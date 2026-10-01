package com.dimdim.banco;

import com.dimdim.banco.model.Conta;
import com.dimdim.banco.model.TipoConta;
import com.dimdim.banco.model.TipoTransacao;
import com.dimdim.banco.model.Transacao;
import com.dimdim.banco.service.ContaService;
import com.dimdim.banco.service.RegraNegocioException;
import com.dimdim.banco.service.TransacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DimdimApplicationTests {

    @Autowired
    ContaService contaService;
    @Autowired
    TransacaoService transacaoService;
    @Autowired
    MockMvc mvc;

    private Conta novaConta(String numero, String saldoInicial) {
        Conta c = new Conta();
        c.setTitular("Patinhas Pataki");
        c.setDocumento("12345678901");
        c.setTipo(TipoConta.CORRENTE);
        c.setAgencia("0001");
        c.setNumero(numero);
        c.setSaldoInicial(new BigDecimal(saldoInicial));
        return contaService.salvar(c);
    }

    private Transacao transacao(Conta conta, TipoTransacao tipo, String valor) {
        Transacao t = new Transacao();
        t.setConta(conta);
        t.setTipo(tipo);
        t.setValor(new BigDecimal(valor));
        t.setDescricao("teste");
        return transacaoService.salvar(t);
    }

    @Test
    void saldoConsideraCreditosEDebitos() {
        Conta conta = novaConta("10001", "100.00");
        transacao(conta, TipoTransacao.DEPOSITO, "50.00");
        transacao(conta, TipoTransacao.SAQUE, "30.00");

        assertThat(contaService.buscar(conta.getId()).getSaldoAtual()).isEqualByComparingTo("120.00");
    }

    @Test
    void naoPermiteSaldoNegativo() {
        Conta conta = novaConta("10002", "10.00");

        assertThatThrownBy(() -> transacao(conta, TipoTransacao.SAQUE, "10.01"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    @Test
    void naoExcluiContaComTransacoes() {
        Conta conta = novaConta("10003", "0.00");
        transacao(conta, TipoTransacao.DEPOSITO, "5.00");

        assertThatThrownBy(() -> contaService.excluir(conta.getId()))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoPermiteContaDuplicada() {
        novaConta("10004", "0.00");

        assertThatThrownBy(() -> novaConta("10004", "0.00"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void crudDeTransacao() {
        Conta conta = novaConta("10005", "100.00");
        Transacao criada = transacao(conta, TipoTransacao.SAQUE, "40.00");

        Transacao form = new Transacao();
        form.setId(criada.getId());
        form.setConta(conta);
        form.setTipo(TipoTransacao.SAQUE);
        form.setValor(new BigDecimal("60.00"));
        form.setDescricao("corrigida");
        transacaoService.salvar(form);
        assertThat(contaService.buscar(conta.getId()).getSaldoAtual()).isEqualByComparingTo("40.00");

        transacaoService.excluir(criada.getId());
        assertThat(contaService.buscar(conta.getId()).getSaldoAtual()).isEqualByComparingTo("100.00");
    }

    @Test
    void paginasRenderizam() throws Exception {
        Conta conta = novaConta("10006", "100.00");
        transacao(conta, TipoTransacao.DEPOSITO, "10.00");

        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/contas")).andExpect(status().isOk());
        mvc.perform(get("/contas/new")).andExpect(status().isOk());
        mvc.perform(get("/contas/" + conta.getId())).andExpect(status().isOk());
        mvc.perform(get("/contas/" + conta.getId() + "/edit")).andExpect(status().isOk());
        mvc.perform(get("/transacoes")).andExpect(status().isOk());
        mvc.perform(get("/transacoes/new")).andExpect(status().isOk());
        mvc.perform(get("/transacoes/new").param("contaId", conta.getId().toString())).andExpect(status().isOk());
    }

    @Test
    void formularioInvalidoVoltaComErros() throws Exception {
        mvc.perform(post("/contas").param("titular", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("conta/form"));
    }

    @Test
    void criaContaETransacaoPeloFormulario() throws Exception {
        mvc.perform(post("/contas")
                        .param("titular", "Milionario Pataki").param("documento", "98765432100")
                        .param("tipo", "POUPANCA").param("agencia", "0002").param("numero", "20001")
                        .param("saldoInicial", "500.00").param("status", "ATIVA"))
                .andExpect(status().is3xxRedirection());

        Conta conta = contaService.listar().stream().filter(c -> "20001".equals(c.getNumero())).findFirst().orElseThrow();

        mvc.perform(post("/transacoes")
                        .param("conta.id", conta.getId().toString()).param("tipo", "DEPOSITO")
                        .param("valor", "25.50").param("descricao", "salario"))
                .andExpect(status().is3xxRedirection());

        assertThat(contaService.buscar(conta.getId()).getSaldoAtual()).isEqualByComparingTo("525.50");

        mvc.perform(get("/transacoes/new").param("contaId", conta.getId().toString()))
                .andExpect(status().isOk());
    }
}
