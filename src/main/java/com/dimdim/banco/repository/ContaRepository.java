package com.dimdim.banco.repository;

import com.dimdim.banco.model.Conta;
import com.dimdim.banco.model.StatusConta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findAllByOrderByTitularAsc();

    List<Conta> findByStatusOrderByTitularAsc(StatusConta status);

    boolean existsByAgenciaAndNumero(String agencia, String numero);

    boolean existsByAgenciaAndNumeroAndIdNot(String agencia, String numero, Long id);

    long countByStatus(StatusConta status);
}
