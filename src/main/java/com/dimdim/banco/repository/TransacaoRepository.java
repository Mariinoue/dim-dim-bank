package com.dimdim.banco.repository;

import com.dimdim.banco.model.Transacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findAllByOrderByDataHoraDesc();

    List<Transacao> findAllByOrderByDataHoraDesc(Pageable pageable);

    List<Transacao> findByContaIdOrderByDataHoraDesc(Long contaId);

    boolean existsByContaId(Long contaId);

    @Query("""
            select coalesce(sum(case when t.tipo = com.dimdim.banco.model.TipoTransacao.DEPOSITO
                                     then t.valor else -t.valor end), 0)
            from Transacao t
            where t.conta.id = :contaId and t.id <> :ignorarId
            """)
    BigDecimal somaMovimentos(@Param("contaId") Long contaId, @Param("ignorarId") Long ignorarId);
}
