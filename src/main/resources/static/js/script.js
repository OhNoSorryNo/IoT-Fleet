/*
*   The script for on the website. This allows for other languages to later be added - and then be selected
*   by the user.
*
* @author streitwies
*/

// will be called in index.html (when at least two languages are available)
async function changeLanguage(lang) {
    await setLanguagePreference(lang);

    const langData = await fetchLanguageData(lang);
    updateContent(langData);

}

// Fetches the language data
async function fetchLanguageData() {
    const response = await fetch(`languages/en.json`);
    return response.json();
}

// Sets the language preference
function setLanguagePreference(lang) {
    localStorage.setItem("language", lang);
    location.reload();
}

// Updates content based on selected language
function updateContent(langData) {
    document.querySelectorAll("[data-i18n]").forEach((element) => {
        const key = element.getAttribute("data-i18n");

        if (element.tagName === "INPUT" && key === "placeholder_text") {
            // Sets Placeholder if element is a placeholder_text attribute
            element.placeholder = langData[key];
        } else {
            // Sets text content for anything else
            element.innerHTML = langData[key];
        }
    });
}

// Calls updateContent() on page load
window.addEventListener("DOMContentLoaded", async () => {
    const userPreferredLanguage = localStorage.getItem("language") || "en";
    const langData = await fetchLanguageData(userPreferredLanguage);
    updateContent(langData);

    const passwordInput = document.getElementById("password");
    const letter= document.getElementById("password_requirements_letter");
    const number= document.getElementById("password_requirements_number");
    const length= document.getElementById("password_requirements_length");

    // Show requirements on click
    passwordInput.onfocus = function () {
        document.getElementById("password_requirements").style.display = "block";
    }

    // Hide requirements when clicked outside input field
    passwordInput.onblur = function () {
       document.getElementById("password_requirements").style.display = "none";
    }

    // For when the user starts typing in the password input field
    passwordInput.onkeyup = function () {
    // Validate letter
    var letters = /[a-zA-Z]/g;
    if (passwordInput.value.match(letters)) {
        letter.classList.remove("invalid");
        letter.classList.add("valid");
    } else {
        letter.classList.remove("valid");
        letter.classList.add("invalid");
    }

    // Validate numbers
    var numbers = /[0-9]/g;
    if (passwordInput.value.match(numbers)) {
        number.classList.remove("invalid");
        number.classList.add("valid");
    } else {
        number.classList.remove("valid");
        number.classList.add("invalid");
    }

    // Validate length
    if (passwordInput.value.length >= 8) {
        length.classList.remove("invalid");
        length.classList.add("valid");
    } else {
        length.classList.remove("valid");
        length.classList.add("invalid");
    }

    // Update validation messages dynamically
    updatePasswordValidationMessages(langData);
}
});

// Updates password validation messages
function updatePasswordValidationMessages(langData) {
    document.getElementById("password_requirements_letter").innerHTML = langData["password_requirements_letter"];
    document.getElementById("password_requirements_number").innerHTML = langData["password_requirements_number"];
    document.getElementById("password_requirements_length").innerHTML = langData["password_requirements_length"];
}

// Processes the Login From and Register From submissions
document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.querySelector('#loginForm');
    const registerForm = document.querySelector('#registerForm');
    const csrfToken = getCsrfToken();

    // Login Form Submission
    if (loginForm) {
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');

        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const username = usernameInput.value;
            const password = passwordInput.value;

            const formData = new URLSearchParams();
            formData.append('username', username);
            formData.append('password', password);

            try {
                const response = await fetch('/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-CSRF-TOKEN': csrfToken
                    },
                    body: formData.toString(),
                });

                if (response.ok) {
                    const result = await response.text();
                    alert(result);
                } else {
                    alert('Login failed. Please check your credentials.');
                }
            } catch (error) {
                console.error('Error during login:', error);
                alert('An error occurred. Please try again.');
            }
        });
    }

    // Register Form Submission
    if (registerForm) {
        const emailInput = document.getElementById('email');
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');
        const passwordConfirmationInput = document.getElementById('password_confirmation');

        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const email = emailInput.value;
            const username = usernameInput.value;
            const password = passwordInput.value;
            const password_confirmation = passwordConfirmationInput.value;

            if (password !== password_confirmation) {
                alert('The passwords do not match.');
                return;
            }

            const formData = new URLSearchParams();
            formData.append('email', email);
            formData.append('username', username);
            formData.append('password', password);

            try {
                const response = await fetch('/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-CSRF-TOKEN': csrfToken
                    },
                    body: formData.toString(),
                });

                if (response.ok) {
                    const result = await response.text();
                    alert('Registration successful! ' + result);
                    window.location.href = 'index.html'; // Redirect to login page
                } else {
                    const result = await response.text();
                    alert('Registration failed: ' + result);
                }
            } catch (error) {
                console.error('Error during registration:', error);
                alert('An error occurred. Please try again.');
            }
        });
    }
});

// Gets the Csrf Token
function getCsrfToken() {
    const csrfCookie = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN='));
    return csrfCookie ? csrfCookie.split('=')[1] : '';
}