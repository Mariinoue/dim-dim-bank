package com.dimdim.banco.controller;

import com.dimdim.banco.model.StatusConta;
import com.dimdim.banco.repository.ContaRepository;
import com.dimdim.banco.service.ContaService;
import com.dimdim.banco.service.TransacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ContaService contaService;
    private final TransacaoService transacaoService;
    private final ContaRepository contaRepository;

    public HomeController(ContaService contaService, TransacaoService transacaoService, ContaRepository contaRepository) {
        this.contaService = contaService;
        this.transacaoService = transacaoService;
        this.contaRepository = contaRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("contasAtivas", contaRepository.countByStatus(StatusConta.ATIVA));
        model.addAttribute("totalTransacoes", transacaoService.total());
        model.addAttribute("saldoTotal", contaService.saldoTotal());
        model.addAttribute("ultimas", transacaoService.ultimas(5));
        return "index";
    }
}
