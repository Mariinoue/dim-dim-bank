package com.dimdim.banco.service;

import com.dimdim.banco.model.Conta;
import com.dimdim.banco.model.StatusConta;
import com.dimdim.banco.model.Transacao;
import com.dimdim.banco.repository.ContaRepository;
import com.dimdim.banco.repository.TransacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TransacaoService {

    private static final Long NENHUMA = -1L;

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository, ContaRepository contaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional(readOnly = true)
    public List<Transacao> listar() {
        return transacaoRepository.findAllByOrderByDataHoraDesc();
    }

    @Transactional(readOnly = true)
    public List<Transacao> ultimas(int quantidade) {
        return transacaoRepository.findAllByOrderByDataHoraDesc(PageRequest.of(0, quantidade));
    }

    @Transactional(readOnly = true)
    public List<Transacao> extrato(Long contaId) {
        return transacaoRepository.findByContaIdOrderByDataHoraDesc(contaId);
    }

    @Transactional(readOnly = true)
    public Transacao buscar(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Transação não encontrada."));
    }

    @Transactional(readOnly = true)
    public long total() {
        return transacaoRepository.count();
    }

    public Transacao salvar(Transacao form) {
        Conta conta = contaRepository.findById(form.getConta().getId())
                .orElseThrow(() -> new RegraNegocioException("Conta não encontrada."));
        if (conta.getStatus() != StatusConta.ATIVA) {
            throw new RegraNegocioException("Só é possível movimentar contas com status Ativa.");
        }

        Transacao alvo = form;
        Long ignorar = NENHUMA;
        if (form.getId() != null) {
            alvo = buscar(form.getId());
            if (!alvo.getConta().getId().equals(conta.getId())) {
                throw new RegraNegocioException("Não é possível mover a transação para outra conta.");
            }
            alvo.setTipo(form.getTipo());
            alvo.setValor(form.getValor());
            alvo.setDescricao(form.getDescricao());
            ignorar = alvo.getId();
        }
        alvo.setConta(conta);

        BigDecimal saldoResultante = conta.getSaldoInicial()
                .add(transacaoRepository.somaMovimentos(conta.getId(), ignorar))
                .add(alvo.getEfeito());
        if (saldoResultante.signum() < 0) {
            throw new RegraNegocioException("Saldo insuficiente para esta operação.");
        }
        return transacaoRepository.save(alvo);
    }

    public void excluir(Long id) {
        Transacao transacao = buscar(id);
        Conta conta = transacao.getConta();
        BigDecimal saldoResultante = conta.getSaldoInicial()
                .add(transacaoRepository.somaMovimentos(conta.getId(), id));
        if (saldoResultante.signum() < 0) {
            throw new RegraNegocioException("Excluir esta transação deixaria o saldo da conta negativo.");
        }
        transacaoRepository.delete(transacao);
    }
}
