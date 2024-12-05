/*
*   The script for on the website. This allows for other languages to later be added - and then be selected
*   by the user.
*
* @author streitwies
* @author jasmin1707
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
    const Uppercaseletter= document.getElementById("password_requirements_uppercase_letter");
    const Lowercaseletter= document.getElementById("password_requirements_lowercase_letter");
    const number= document.getElementById("password_requirements_number");
    const specialCharacter= document.getElementById("password_requirements_special_character");
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
        // Validate uppercase letter
        const UppercaseLetters = /[A-Z]/g;
        if (passwordInput.value.match(UppercaseLetters)) {
            Uppercaseletter.classList.remove("invalid");
            Uppercaseletter.classList.add("valid");
        } else {
            Uppercaseletter.classList.remove("valid");
            Uppercaseletter.classList.add("invalid");
        }

        // Validate lowercase letter
        const LowercaseLetters = /[a-z]/g;
        if (passwordInput.value.match(LowercaseLetters)) {
            Lowercaseletter.classList.remove("invalid");
            Lowercaseletter.classList.add("valid");
        } else {
            Lowercaseletter.classList.remove("valid");
            Lowercaseletter.classList.add("invalid");
        }

        // Validate numbers
        const numbers = /[0-9]/g;
        if (passwordInput.value.match(numbers)) {
            number.classList.remove("invalid");
            number.classList.add("valid");
        } else {
            number.classList.remove("valid");
            number.classList.add("invalid");
        }

        // Validate special character
        const specialCharacters = /[$&+,:;=?@#|'<>.^*()%!-]/g;
        if (passwordInput.value.match(specialCharacters)) {
            specialCharacter.classList.remove("invalid");
            specialCharacter.classList.add("valid");
        } else {
            specialCharacter.classList.remove("valid");
            specialCharacter.classList.add("invalid");
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
    document.getElementById("password_requirements_uppercase_letter").innerHTML = langData["password_requirements_uppercase_letter"];
    document.getElementById("password_requirements_lowercase_letter").innerHTML = langData["password_requirements_lowercase_letter"];
    document.getElementById("password_requirements_number").innerHTML = langData["password_requirements_number"];
    document.getElementById("password_requirements_special_character").innerHTML = langData["password_requirements_special_character"];
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
                    console.log('Login successful. Redirecting...');
                    window.location.href = '/dashboard.html';
                } else {
                    alert('Login failed. Please check your credentials.');
                }
            } catch (error) {
                console.error('Error during login:', error);
                alert('An error occurred. Please try again.');
            }
        });
    }

    //added by me
    // Add new agent popup functionality
    const addAgentForm = document.getElementById('add-agent-form');
    if (addAgentForm) {
        addAgentForm.addEventListener('submit', async function (event) {
            event.preventDefault(); // Prevent the default form submission

            const secretKeyInput = document.getElementById('secret-key');
            const secretKey = secretKeyInput.value;

            try {
                const response = await fetch('/agents/registeragentforuser', {
                    method: 'POST',
                    credentials: 'include', // Include cookies in the request
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams({ secretKey: secretKey }).toString()
                });

                if (response.ok) {
                    const agent = await response.json();
                    alert('Agent registered successfully!');
                    // Optionally, update the UI to show the new agent
                    addAgentToGrid(agent);
                    // Close the popup
                    document.getElementById('add-agent-popup').style.display = 'none';
                } else {
                    const errorText = await response.text();
                    alert('Failed to register agent: ' + errorText);
                }
            } catch (error) {
                console.error('Error during agent registration:', error);
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
                    //const result = await response.text();
                    alert('Registration successful! ');
                    window.location.href = 'index.html';
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

// checks if a user is logged in and if so loads the agents for that specific user
document.addEventListener('DOMContentLoaded', async function () {
    if (window.location.pathname.includes('dashboard.html')) {
        try {
            const response = await fetch('/auth/check', {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                window.location.href = 'index.html';
                return;
            }
            await loadUserAgents();

        } catch (error) {
            console.error('Error while authenticating login:', error);
            window.location.href = 'index.html';
        }
    }
});

// gets the agents from the server and puts them in the grid
async function loadUserAgents() {
    try {
        const response = await fetch('/auth/user/agents', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const agents = await response.json();
            console.log(agents)
            renderAgentsGrid(agents);
        } else {
            console.error('Failed to load user agents:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching user agents:', error);
    }
}

// renders the agents grid including the one to add a new agent
function renderAgentsGrid(agents) {
    const gridContainer = document.getElementById('agents-grid');
    gridContainer.innerHTML = '';

    agents.forEach(agent => {
        const gridItem = document.createElement('div');
        gridItem.className = 'grid-item';
        gridItem.setAttribute('data-agent-id', agent.agentId);
        gridItem.innerHTML = `
            <h2>${agent.agentId}</h2>
            <div class="status-led ${agent.status ? 'active' : 'inactive'}"></div>
        `;
        gridContainer.appendChild(gridItem);
    });

    const addAgentItem = document.createElement('div');
    addAgentItem.className = 'grid-item add-agent';
    addAgentItem.innerHTML = '<i class="fa fa-plus"></i>';

    gridContainer.appendChild(addAgentItem);

    addAgentItem.addEventListener('click', () => {
        document.getElementById('add-agent-popup').style.display = 'flex';
    });

    const closeAddAgentBtn = document.getElementById('close-add-agent');
    if (closeAddAgentBtn) {
        closeAddAgentBtn.addEventListener('click', function () {
            document.getElementById('add-agent-popup').style.display = 'none';
        });
    }

    const addAgentPopup = document.getElementById('add-agent-popup');
    window.addEventListener('click', function (event) {
        if (event.target === addAgentPopup) {
            addAgentPopup.style.display = 'none';
        }
    });
}

// Dynamically add an agent to the grid
function addAgentToGrid(agent) {
    const gridContainer = document.getElementById('agents-grid');

    // Create a new grid item for the agent
    const gridItem = document.createElement('div');
    gridItem.className = 'grid-item';
    gridItem.innerHTML = `
        <h2>${agent.agentId}</h2>
        <div class="status-led ${agent.status ? 'active' : 'inactive'}"></div>
    `;

    // Append the new grid item to the container
    const addAgentItem = document.querySelector('.grid-item.add-agent');
    gridContainer.insertBefore(gridItem, addAgentItem);
}

// gets the status
async function pollAgentStatus() {
    try {
        const response = await fetch('/auth/user/agents', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const agents = await response.json();
            updateAgentStatusInGrid(agents);
        } else {
            console.error('Failed to load user agents:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching user agents:', error);
    }
}


// updates the status indicator
function updateAgentStatusInGrid(agents) {
    agents.forEach(agent => {
        // Find the corresponding grid item by agent ID
        const gridItem = document.querySelector(`.grid-item[data-agent-id="${agent.agentId}"]`);
        if (gridItem) {
            const statusLed = gridItem.querySelector('.status-led');
            if (statusLed) {
                if (agent.status) {
                    statusLed.classList.remove('inactive');
                    statusLed.classList.add('active');
                } else {
                    statusLed.classList.remove('active');
                    statusLed.classList.add('inactive');
                }
            }
        }
    });
}

// polls every 10 seconds
setInterval(pollAgentStatus, 10000);

// initially sets up the status
document.addEventListener('DOMContentLoaded', function () {
    if (window.location.pathname.includes('dashboard.html')) {
        pollAgentStatus();
    }
});

// Logout
document.addEventListener('DOMContentLoaded', function () {
    const logoutBtn = document.getElementById('logout-btn');

    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            try {
                const response = await fetch('/auth/logout', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    credentials: 'include',
                });

                if (response.ok) {
                    console.log('Logout successful. Redirecting to login page...');
                    alert('Logout successful.');
                    window.location.href = '/index.html';
                } else {
                    alert('Logout failed.');
                }
            } catch (error) {
                console.error('Error during logout:', error);
                alert('An error occurred. Please try again.');
            }
        });
    }
});

// Profile and Notification Popups
document.addEventListener('DOMContentLoaded', function () {
    const profileBtn = document.getElementById("profile-btn");
    const notificationsBtn = document.getElementById("notifications-btn");

    const profilePopup = document.getElementById("profile-popup");
    const notificationsPopup = document.getElementById("notifications-popup");

    const closeProfile = document.getElementById("close-profile");
    const closeNotifications = document.getElementById("close-notifications");

    // Open popups
    profileBtn.addEventListener("click", () => profilePopup.style.display = "flex");
    notificationsBtn.addEventListener("click", () => notificationsPopup.style.display = "flex");

    // Close popups
    closeProfile.addEventListener("click", () => profilePopup.style.display = "none");
    closeNotifications.addEventListener("click", () => notificationsPopup.style.display = "none");
});

// Gets the Csrf Token
function getCsrfToken() {
    const csrfCookie = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN='));
    return csrfCookie ? csrfCookie.split('=')[1] : '';
}