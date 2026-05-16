package org.main.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.main.services.RecuperacaoSenhaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class RecuperacaoSenhaController {

    private final RecuperacaoSenhaService recuperacaoSenhaService;

    public RecuperacaoSenhaController(RecuperacaoSenhaService recuperacaoSenhaService) {
        this.recuperacaoSenhaService = recuperacaoSenhaService;
    }

    @GetMapping("/recuperar_senha")
    public String mostrarFormularioRecuperacao(Model model) {
        model.addAttribute("modoReset", false);
        return "recuperar_senha";
    }

    @PostMapping("/recuperar_senha")
    public String solicitarRecuperacao(@RequestParam String email,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        try {
            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath(request.getContextPath())
                    .replaceQuery(null)
                    .build()
                    .toUriString();
            recuperacaoSenhaService.solicitarRecuperacaoSenha(email, baseUrl, extrairIpCliente(request));
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Se o e-mail estiver cadastrado, você receberá instruções para redefinir a senha.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }

        return "redirect:/recuperar_senha";
    }

    @GetMapping("/redefinir_senha")
    public String mostrarFormularioRedefinicao(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("modoReset", true);
        model.addAttribute("token", token);
        model.addAttribute("tokenValido", recuperacaoSenhaService.tokenValido(token));
        return "redefinir_senha";
    }

    @PostMapping("/redefinir_senha")
    public String redefinirSenha(@RequestParam String token,
                                 @RequestParam String novaSenha,
                                 @RequestParam String confirmarSenha,
                                 RedirectAttributes redirectAttributes) {
        try {
            recuperacaoSenhaService.redefinirSenha(token, novaSenha, confirmarSenha);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha atualizada com sucesso. Agora você pode entrar novamente.");
            return "redirect:/login?senhaAtualizada=true";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
            return "redirect:/redefinir_senha?token=" + token;
        }
    }

    private String extrairIpCliente(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
