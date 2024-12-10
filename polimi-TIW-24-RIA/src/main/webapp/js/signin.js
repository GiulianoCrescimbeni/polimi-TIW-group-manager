$(document).ready(function() {
    const form = $('#register-form');
    const usernameInput = $('#username');
	const emailInput = $('#register-email');
    const usernameError = $('#username-error');
    const password = $('#register-password');
    const verifyPassword = $('#register-verify-password');
    const errorMessage = $('#error-message');
    const passwordError = $('#password-error');
	const emailError = $('#email-error');

    let typingTimeout; // Variabile per memorizzare il timeout
	
	function isValidEmail(email) {
	    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$/;
	    return emailRegex.test(email);
	}

    usernameInput.on('input', function() {
        clearTimeout(typingTimeout); // Cancella il timeout precedente

        const username = usernameInput.val();
        if (username) {
            typingTimeout = setTimeout(function() { // Imposta un nuovo timeout
                $.ajax({
                    url: 'checkusername', // Usa un URL relativo
                    method: 'GET',
                    data: { username: username },
                    success: function(data) {
                        if (data.exists) {
							usernameError.text("Username already in use.")
                            usernameError.show();
                            console.log("Already in use");
                        } else {
                            usernameError.hide();
                        }
                    },
                    error: function(error) {
                        console.error('Error:', error);
                    }
                });
            }, 500); // Ritardo di 500 millisecondi
        } else {
            usernameError.hide();
        }
    });

    function checkPasswords() {
        if (password.val() !== verifyPassword.val()) {
			passwordError.text("The passwords do not match.")
            passwordError.show();
        } else {
            passwordError.hide();
        }
    }
	
	function checkEmail() {
		if (!isValidEmail(emailInput.val())) {
			emailError.text("Invalid email format.");
			emailError.show();
		} else {
			emailError.hide();
		}
	}

    password.on('input', checkPasswords);
    verifyPassword.on('input', checkPasswords);
	emailInput.on('input', checkEmail);

    form.on('submit', function(event) {
        if (usernameError.is(':visible') || passwordError.is(':visible') || emailError.is(':visible')) {
            event.preventDefault(); // Impedisce l'invio del modulo se ci sono errori
        }
    });
});
