package com.dimdim.banco.controller;

import com.dimdim.banco.model.Conta;
import com.dimdim.banco.model.StatusConta;
import com.dimdim.banco.model.TipoConta;
import com.dimdim.banco.service.ContaService;
import com.dimdim.banco.service.RegraNegocioException;
import com.dimdim.banco.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;
    private final TransacaoService transacaoService;

    public ContaController(ContaService contaService, TransacaoService transacaoService) {
        this.contaService = contaService;
        this.transacaoService = transacaoService;
    }

    @ModelAttribute("tipos")
    public TipoConta[] tipos() {
        return TipoConta.values();
    }

    @ModelAttribute("statusList")
    public StatusConta[] statusList() {
        return StatusConta.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("contas", contaService.listar());
        return "conta/list";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("conta", contaService.buscar(id));
        model.addAttribute("extrato", transacaoService.extrato(id));
        return "conta/details";
    }

    @GetMapping("/new")
    public String novo(Model model) {
        model.addAttribute("conta", new Conta());
        return "conta/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("conta") Conta conta, BindingResult result,
                        RedirectAttributes redirect) {
        return gravar(conta, result, redirect, "Conta aberta com sucesso.");
    }

    @GetMapping("/{id}/edit")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("conta", contaService.buscar(id));
        return "conta/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("conta") Conta conta,
                            BindingResult result, RedirectAttributes redirect) {
        conta.setId(id);
        return gravar(conta, result, redirect, "Conta atualizada com sucesso.");
    }

    @PostMapping("/{id}/delete")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            contaService.excluir(id);
            redirect.addFlashAttribute("message", "Conta excluída com sucesso.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/contas";
    }

    private String gravar(Conta conta, BindingResult result, RedirectAttributes redirect, String sucesso) {
        if (!result.hasErrors()) {
            try {
                contaService.salvar(conta);
                redirect.addFlashAttribute("message", sucesso);
                return "redirect:/contas";
            } catch (RegraNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        return "conta/form";
    }
}
