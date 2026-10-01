package com.dimdim.banco.controller;

import com.dimdim.banco.model.Conta;
import com.dimdim.banco.model.Transacao;
import com.dimdim.banco.model.TipoTransacao;
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
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;
    private final ContaService contaService;

    public TransacaoController(TransacaoService transacaoService, ContaService contaService) {
        this.transacaoService = transacaoService;
        this.contaService = contaService;
    }

    @ModelAttribute("tipos")
    public TipoTransacao[] tipos() {
        return TipoTransacao.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("transacoes", transacaoService.listar());
        return "transacao/list";
    }

    @GetMapping("/new")
    public String nova(@RequestParam(required = false) Long contaId, Model model) {
        Transacao transacao = new Transacao();
        if (contaId != null) {
            Conta conta = new Conta();
            conta.setId(contaId);
            transacao.setConta(conta);
        }
        return formulario(transacao, model);
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("transacao") Transacao transacao, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        return gravar(transacao, result, model, redirect, "Transação registrada com sucesso.");
    }

    @GetMapping("/{id}/edit")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(transacaoService.buscar(id), model);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("transacao") Transacao transacao,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        transacao.setId(id);
        return gravar(transacao, result, model, redirect, "Transação atualizada com sucesso.");
    }

    @PostMapping("/{id}/delete")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            transacaoService.excluir(id);
            redirect.addFlashAttribute("message", "Transação excluída com sucesso.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transacoes";
    }

    private String formulario(Transacao transacao, Model model) {
        model.addAttribute("transacao", transacao);
        model.addAttribute("contas", contaService.listarAtivas());
        return "transacao/form";
    }

    private String gravar(Transacao transacao, BindingResult result, Model model,
                          RedirectAttributes redirect, String sucesso) {
        if (!result.hasErrors()) {
            try {
                transacaoService.salvar(transacao);
                redirect.addFlashAttribute("message", sucesso);
                return "redirect:/transacoes";
            } catch (RegraNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        model.addAttribute("contas", contaService.listarAtivas());
        return "transacao/form";
    }
}
