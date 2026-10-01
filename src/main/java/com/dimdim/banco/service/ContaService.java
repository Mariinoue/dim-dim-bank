package com.dimdim.banco.service;

import com.dimdim.banco.model.Conta;
import com.dimdim.banco.model.StatusConta;
import com.dimdim.banco.repository.ContaRepository;
import com.dimdim.banco.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ContaService {

    private static final Long NENHUMA = -1L;

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public ContaService(ContaRepository contaRepository, TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<Conta> listar() {
        return comSaldo(contaRepository.findAllByOrderByTitularAsc());
    }

    @Transactional(readOnly = true)
    public List<Conta> listarAtivas() {
        return comSaldo(contaRepository.findByStatusOrderByTitularAsc(StatusConta.ATIVA));
    }

    @Transactional(readOnly = true)
    public Conta buscar(Long id) {
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Conta não encontrada."));
        conta.setSaldoAtual(saldoDe(conta));
        return conta;
    }

    public Conta salvar(Conta form) {
        boolean duplicada = form.getId() == null
                ? contaRepository.existsByAgenciaAndNumero(form.getAgencia(), form.getNumero())
                : contaRepository.existsByAgenciaAndNumeroAndIdNot(form.getAgencia(), form.getNumero(), form.getId());
        if (duplicada) {
            throw new RegraNegocioException("Já existe uma conta com essa agência e número.");
        }
        if (form.getId() == null) {
            return contaRepository.save(form);
        }
        Conta existente = contaRepository.findById(form.getId())
                .orElseThrow(() -> new RegraNegocioException("Conta não encontrada."));
        existente.setTitular(form.getTitular());
        existente.setDocumento(form.getDocumento());
        existente.setTipo(form.getTipo());
        existente.setAgencia(form.getAgencia());
        existente.setNumero(form.getNumero());
        existente.setSaldoInicial(form.getSaldoInicial());
        existente.setStatus(form.getStatus());
        return contaRepository.save(existente);
    }

    public void excluir(Long id) {
        if (transacaoRepository.existsByContaId(id)) {
            throw new RegraNegocioException(
                    "A conta possui transações. Exclua as transações primeiro ou altere o status para Encerrada.");
        }
        contaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal saldoDe(Conta conta) {
        return conta.getSaldoInicial().add(transacaoRepository.somaMovimentos(conta.getId(), NENHUMA));
    }

    @Transactional(readOnly = true)
    public BigDecimal saldoTotal() {
        return contaRepository.findAll().stream()
                .filter(c -> c.getStatus() == StatusConta.ATIVA)
                .map(this::saldoDe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Conta> comSaldo(List<Conta> contas) {
        contas.forEach(c -> c.setSaldoAtual(saldoDe(c)));
        return contas;
    }
}
