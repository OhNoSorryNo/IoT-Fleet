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
});

// Processes the Login From - and Register From submissions
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