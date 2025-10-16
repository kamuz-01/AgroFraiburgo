// validacoes-criar_conta.js
document.addEventListener("DOMContentLoaded", function () {
    // Máscaras
    Inputmask({
        mask: '999.999.999-99',
        placeholder: ' ',
        clearIncomplete: true,
        showMaskOnHover: false
    }).mask(document.getElementById("cpf"));

    Inputmask({
        mask: '(99) 99999-9999',
        placeholder: ' ',
        clearIncomplete: true,
        showMaskOnHover: false
    }).mask(document.getElementById("telefone"));

    flatpickr("#dataNascimento", {
        locale: "pt",
        altInput: true,
        altFormat: "d/m/Y",
        dateFormat: "Y-m-d",
        maxDate: "today",
        defaultDate: "2007-01-01",
        disableMobile: true,
        onChange: function () {
            const hiddenInput = document.getElementById("dataNascimento");
            const visibleInput = hiddenInput._flatpickr.altInput;
            const errorElement = document.getElementById("dataNascimentoError");
            const result = window.validateNascimento(hiddenInput.value);

            if (!result.valid) {
                hiddenInput.setAttribute("aria-invalid", "true");
                visibleInput.setAttribute("aria-invalid", "true");
                errorElement.textContent = result.message;
                errorElement.style.display = "block";
            } else {
                hiddenInput.setAttribute("aria-invalid", "false");
                visibleInput.setAttribute("aria-invalid", "false");
                errorElement.textContent = "";
                errorElement.style.display = "none";
            }
        }
    });
});

// Função genérica de validação
window.validateField = function (field, errorElement, validationFn) {
    const result = validationFn(field.value);
    if (!result.valid) {
        field.setAttribute('aria-invalid', 'true');
        errorElement.textContent = result.message;
        errorElement.style.display = 'block';
        return false;
    } else {
        field.setAttribute('aria-invalid', 'false');
        errorElement.textContent = '';
        errorElement.style.display = 'none';
        return true;
    }
}

// Funções de validação específicas
window.validateFullName = function (value) {
    if (!value.trim()) return { valid: false, message: 'Nome é obrigatório' };
    if (value.trim().length < 2) return { valid: false, message: 'Nome deve ter pelo menos 2 caracteres' };
    return { valid: true };
}

window.validateSobrenome = function (value) {
    if (!value.trim()) return { valid: false, message: 'Sobrenome é obrigatório' };
    if (value.trim().length < 2) return { valid: false, message: 'Sobrenome deve ter pelo menos 2 caracteres' };
    return { valid: true };
}

window.validateCPF = function (value) {
    const cpf = value.replace(/[^\d]+/g, '');
    if (!cpf) return { valid: false, message: 'CPF é obrigatório' };
    if (cpf.length !== 11 || /^(\d)\1+$/.test(cpf)) return { valid: false, message: 'CPF inválido' };

    let sum = 0;
    for (let i = 0; i < 9; i++) {
        sum += parseInt(cpf.charAt(i)) * (10 - i);
    }
    let rev = 11 - (sum % 11);
    if (rev === 10 || rev === 11) rev = 0;
    if (rev !== parseInt(cpf.charAt(9))) return { valid: false, message: 'CPF inválido' };

    sum = 0;
    for (let i = 0; i < 10; i++) {
        sum += parseInt(cpf.charAt(i)) * (11 - i);
    }
    rev = 11 - (sum % 11);
    if (rev === 10 || rev === 11) rev = 0;
    if (rev !== parseInt(cpf.charAt(10))) return { valid: false, message: 'CPF inválido' };

    return { valid: true };
}

window.validateEmail = function (value) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!value.trim()) return { valid: false, message: 'E-mail é obrigatório' };
    if (!emailRegex.test(value)) return { valid: false, message: 'E-mail inválido' };
    return { valid: true };
}

window.validateUserName = function (value) {
    if (!value.trim()) return { valid: false, message: 'Nome de usuário é obrigatório' };
    if (value.trim().length < 2) return { valid: false, message: 'Nome de usuário deve ter pelo menos 2 caracteres' };
    return { valid: true };
}

window.validatePassword = function (value) {
    if (!value) return { valid: false, message: 'Senha é obrigatória' };
    if (value.length < 8) return { valid: false, message: 'Senha deve ter pelo menos 8 caracteres' };
    if (!/[A-Z]/.test(value)) return { valid: false, message: 'Senha deve conter pelo menos 1 letra maiúscula' };
    if (!/\d/.test(value)) return { valid: false, message: 'Senha deve conter pelo menos 1 número' };
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(value)) return { valid: false, message: 'Senha deve conter pelo menos 1 caractere especial' };
    return { valid: true };
}

window.validateConfirmPassword = function (value) {
    const password = document.getElementById('senha').value;
    if (!value) return { valid: false, message: 'Confirmação de senha é obrigatória' };
    if (value !== password) return { valid: false, message: 'Senhas não coincidem' };
    return { valid: true };
}

window.validateNascimento = function (value) {
    if (!value) return { valid: false, message: 'Data de nascimento é obrigatória' };
    const nascimento = new Date(value);
    const hoje = new Date();
    let idade = hoje.getFullYear() - nascimento.getFullYear();
    const m = hoje.getMonth() - nascimento.getMonth();
    if (m < 0 || (m === 0 && hoje.getDate() < nascimento.getDate())) {
        idade--;
    }
    if (idade < 18) return { valid: false, message: 'É necessário ter pelo menos 18 anos' };
    return { valid: true };
}

window.validatePhone = function (value) {
    const digits = value.replace(/\D/g, '');
    if (!digits) return { valid: false, message: 'Telefone é obrigatório' };
    if (digits.length < 10) return { valid: false, message: 'Telefone deve ter pelo menos 10 dígitos' };
    return { valid: true };
}

window.validateCity = function (value) {
    if (!value.trim()) return { valid: false, message: 'Cidade é obrigatória' };
    return { valid: true };
}

window.validateState = function (value) {
    if (!value) return { valid: false, message: 'Estado é obrigatório' };
    return { valid: true };
}

window.validateTipoUsuario = function () {
    const userType = document.querySelector('input[name="tipoUsuario"]:checked');
    const errorElement = document.getElementById('tipoUsuarioError');
    if (!userType) {
        errorElement.textContent = 'Tipo de usuário é obrigatório';
        errorElement.style.display = 'block';
        return false;
    }
    errorElement.textContent = '';
    errorElement.style.display = 'none';
    return true;
}

// Validação em tempo real da confirmação de senha
window.validatePasswordMatch = function () {
    const password = document.getElementById('senha').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorElement = document.getElementById('confirmPasswordError');
    if (!confirmPassword) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
        document.getElementById('confirmPassword').setAttribute('aria-invalid', 'false');
        return;
    }
    if (password !== confirmPassword) {
        errorElement.textContent = 'Senhas não coincidem';
        errorElement.style.display = 'block';
        document.getElementById('confirmPassword').setAttribute('aria-invalid', 'true');
    } else {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
        document.getElementById('confirmPassword').setAttribute('aria-invalid', 'false');
    }
}

// Eventos de validação em tempo real
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById('nome').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('nomeError'), window.validateFullName);
    });

    document.getElementById('sobrenome').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('sobrenomeError'), window.validateSobrenome);
    });

    document.getElementById('cpf').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('cpfError'), window.validateCPF);
    });

    document.getElementById('email').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('emailError'), window.validateEmail);
    });

    document.getElementById('nomeLogin').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('nomeLoginError'), window.validateUserName);
    });

    document.getElementById('senha').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('senhaError'), window.validatePassword);
        if (document.getElementById('confirmPassword').value) {
            window.validateField(document.getElementById('confirmPassword'), document.getElementById('confirmPasswordError'), window.validateConfirmPassword);
        }
    });

    document.getElementById('confirmPassword').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('confirmPasswordError'), window.validateConfirmPassword);
    });

    document.getElementById('telefone').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('telefoneError'), window.validatePhone);
    });

    document.getElementById('cidade').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('cidadeError'), window.validateCity);
    });

    document.getElementById('estado').addEventListener('blur', function () {
        window.validateField(this, document.getElementById('estadoError'), window.validateState);
    });

    document.querySelectorAll('input[name="tipoUsuario"]').forEach(radio => {
        radio.addEventListener('change', window.validateTipoUsuario);
    });

    document.getElementById('senha').addEventListener('input', window.validatePasswordMatch);
    document.getElementById('confirmPassword').addEventListener('input', window.validatePasswordMatch);

    // Foco no primeiro campo ao carregar
    document.getElementById('nome').focus();
});