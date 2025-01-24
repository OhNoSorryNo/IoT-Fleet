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
                    alert('Agent registered successfully!');
                    // Re-fetch all agents to ensure the UI is accurate
                    await loadUserAgents();
                    // Close the popup
                    document.getElementById('add-agent-popup').style.display = 'none';
                } else {
                    const errorText = await response.text();
                    alert('Failed to register agent.');
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

        try {
            await loadUserAgents();

            // Event-Listener for the "Select Mode"-Button
            const selectModeBtn = document.getElementById('select-mode-btn');
            selectModeBtn.addEventListener('click', () => {
                const checkboxes = document.querySelectorAll('.device-select');
                const dropdown = document.getElementById('action-dropdown');
                const applyButton = document.getElementById('apply-action-btn');

                const isSelectionMode = dropdown.style.display === 'none';
                dropdown.style.display = isSelectionMode ? 'block' : 'none';
                applyButton.style.display = isSelectionMode ? 'block' : 'none';

                checkboxes.forEach(checkbox => {
                    checkbox.style.display = isSelectionMode ? 'block' : 'none';
                });
            });

            // Event-Listener for the "Apply"-Button
            const applyButton = document.getElementById('apply-action-btn');
            applyButton.addEventListener('click', async () => {
                const selectedAction = document.getElementById('action-dropdown').value;
                if (!selectedAction) {
                    alert('Please select an action!');
                    return;
                }

                const selectedDevices = Array.from(document.querySelectorAll('.device-select:checked'))
                    .map(checkbox => checkbox.getAttribute('data-agent-id'));

                if (selectedDevices.length === 0) {
                    alert('No devices selected!');
                    return;
                }

                try {
                    const response = await fetch('/agents/apply-action', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            action: selectedAction,
                            devices: selectedDevices,
                        }),
                    });

                    if (response.ok) {
                        alert('Action applied successfully!');
                    } else {
                        alert('Failed to apply action. Please try again.');
                    }
                } catch (error) {
                    console.error('Error applying action:', error);
                    alert('An error occurred. Please try again.');
                }
            });

        } catch (error) {
            console.error('Error loading dashboard:', error);
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
            console.log(agents);
            renderAgentsGrid(agents);
            // Trigger a manual status refresh immediately after loading agents
            pollAgentStatus();
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
        gridItem.setAttribute('data-agent-id', String(agent.agentId));
        gridItem.innerHTML = `
             <input type="checkbox" class="device-select" data-agent-id="${agent.agentId}" style="display: none;">
            <h2>${agent.agentId}</h2>
            <div class="status-led ${agent.status ? 'active' : 'inactive'}"></div>
            <i class="fas fa-info-circle info-icon" title="Info"></i>
        `;

        if(agent.updateRequested) {
            const updateButton = document.createElement('button');
            updateButton.className = 'update-button';
            updateButton.innerText = 'Update';
            updateButton.addEventListener('click', () => openUpdatePopup(agent.agentId));
            gridItem.appendChild(updateButton);
        }
        gridContainer.appendChild(gridItem);

        const infoIcon = gridItem.querySelector('.info-icon');
        infoIcon.addEventListener('click', () => {
            fetchAgentDetails(agent.agentId);
        });
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

    checkFirmwareUpdates();
}

// Update popup
function openUpdatePopup(agentId) {
    const popup = document.getElementById('update-agent-confirm-popup');
    popup.style.display = 'flex';

    document.getElementById('confirm-update').onclick = () => confirmUpdate(agentId);
    document.getElementById('cancel-update').onclick = () => closeUpdatePopup();
    document.getElementById('close-update-popup').onclick = () => closeUpdatePopup();
    window.addEventListener('click', outsideClickListener);
}
function closeUpdatePopup() {
    document.getElementById('update-agent-confirm-popup').style.display = 'none';
    window.removeEventListener('click', outsideClickListener);
}

function outsideClickListener(event) {
    const popup = document.getElementById('update-agent-confirm-popup');
    if (event.target === popup) {
        closeUpdatePopup();
    }
}

async function checkFirmwareUpdates() {
    try {
        const response = await fetch('/auth/user/agents', { method: 'GET', credentials: 'include' });

        if (response.ok) {
            const agents = await response.json();

            // checking for newest firmware
            const firmwareResponse = await fetch('/firmware/latest', { method: 'GET' });
            const latestFirmware = await firmwareResponse.json();


            agents.forEach(agent => {
                console.log('Agent object:', agent);
                console.log(typeof agent.agentId);
                const gridItem = document.querySelector(`[data-agent-id="${agent.agentId}"]`);

                if (!gridItem) return; // cancel if ui not loaded yet

                const existingUpdateButton = gridItem.querySelector('.update-button');

                if (agent.updateRequested) {
                    if (existingUpdateButton) {
                        existingUpdateButton.remove();
                    }
                    return;
                }

                // show update button if new firmware available
                if (agent.hasOwnProperty('firmwareVersion') && agent.hasOwnProperty('newFirmware')) {

                    if ((!agent.firmwareVersion && agent.newFirmware) || agent.firmwareVersion && agent.newFirmware && String(agent.firmwareVersion.id) !== String(agent.newFirmware.id))  {

                        if (!gridItem.querySelector('.update-button')) {
                            const updateButton = document.createElement('button');
                            updateButton.className = 'update-button';
                            updateButton.innerText = 'Update';
                            updateButton.addEventListener('click', () => openUpdatePopup(agent.agentId));
                            gridItem.appendChild(updateButton);
                        }
                    } else {
                        if (existingUpdateButton) {
                            existingUpdateButton.remove();
                        }
                    }
                } else {
                    if (existingUpdateButton) {
                        existingUpdateButton.remove();
                    }
                }
            });
        }
    } catch (error) {
        console.error('Error while checking firmware:', error);
    }
}

setInterval(checkFirmwareUpdates, 60000);

// sets updateRequested to true if the user confirmed the update
async function confirmUpdate(agentId) {
    try {
        const response = await fetch(`/agents/${agentId}/update-request`, {
            method: 'PUT',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            alert('Update requested.');
            closeUpdatePopup();
            await checkFirmwareUpdates();
        } else {
            const errorText = await response.text();
            alert('Error while requesting update: ' + errorText);
        }
    } catch (error) {
        console.error('Error during update:', error);
        alert('Error during the update process.');
    }
}

// Dynamically add an agent to the grid
function addAgentToGrid(agent) {
    const gridContainer = document.getElementById('agents-grid');

    // Create a new grid item for the agent
    async function addAgentToGrid(agent) {
        await loadUserAgents(); // Fetches all agents and re-renders the grid
    }

    // Append the new grid item to the container
    const addAgentItem = document.querySelector('.grid-item.add-agent');
    gridContainer.insertBefore(gridItem, addAgentItem);
}

// gets the status
async function pollAgentStatus() {
    const gridItems = document.querySelectorAll('.grid-item[data-agent-id]'); // Get all agent items from the grid

    for (const gridItem of gridItems) {
        const agentId = String(gridItem.getAttribute('data-agent-id')); // Extract the agent ID

        try {
            // Fetch the status from the backend
            const response = await fetch(`https://localhost:8443/agents/${agentId}/status`, {
                method: 'GET',
                credentials: 'include',
            });

            if (response.ok) {
                const isOnline = await response.text(); // Response is "true" or "false"
                const statusLed = gridItem.querySelector('.status-led'); // Find the LED for this agent

                // Update the LED based on the status
                if (isOnline.trim() === 'true') {
                    statusLed.classList.remove('inactive');
                    statusLed.classList.add('active');
                } else {
                    statusLed.classList.remove('active');
                    statusLed.classList.add('inactive');
                }
            } else {
                console.error(`Failed to fetch status for agent ${agentId}:`, response.statusText);
            }
        } catch (error) {
            console.error(`Error fetching status for agent ${agentId}:`, error);
        }
    }
}

// initially sets up the status
document.addEventListener('DOMContentLoaded', function () {
    if (window.location.pathname.includes('dashboard.html')) {
        pollAgentStatus();
        setInterval(pollAgentStatus, 10000);
    }
});

// Fetches agent details and displays them in a popup
async function fetchAgentDetails(agentId) {
    try {
        const response = await fetch(`/agents/${agentId}/details`, {
            method: 'GET',
            credentials: 'include',
        });

        if (response.ok) {
            const agentDetails = await response.json();
            displayAgentInfoPopup(agentDetails);
        } else {
            console.error(`Failed to fetch agent details for ${agentId}:`, response.statusText);
        }
    } catch (error) {
        console.error(`Error fetching agent details for ${agentId}:`, error);
    }
}

// Displays the detail view popup with details
function displayAgentInfoPopup(agentDetails) {
    const agentInfoList = document.getElementById('agent-info-list');
    agentInfoList.innerHTML = `
        <li><strong>Agent ID:</strong> ${agentDetails.agentId}</li>
        <li><strong>ID:</strong> ${agentDetails.id}</li>
        <li><strong>Secret Key:</strong> ${agentDetails.secretKey}</li>
        <li><strong>Last Seen:</strong> ${agentDetails.lastSeen}</li>
        <li><strong>Online:</strong> ${agentDetails.online ? 'Yes' : 'No'}</li>
        <li><strong>Firmware Version:</strong> ${agentDetails.firmwareVersion}</li>
        <li><strong>Ping Frequency:</strong> ${agentDetails.pingFrequency} ms</li>
        <li><strong>Agent Type:</strong> ${agentDetails.agentType}</li>
    `;

    const popup = document.getElementById('agent-info-popup');
    popup.style.display = 'flex';

    const removeBtn = document.getElementById('remove_btn')
    removeBtn.onclick = () => {
        showRemoveConfirmationPopup(agentDetails.agentId);
    };

    const closeBtn = document.getElementById('close-agent-info');
    closeBtn.addEventListener('click', () => {
        popup.style.display = 'none';
    });

    window.addEventListener('click', (event) => {
        if (event.target === popup) {
            popup.style.display = 'none';
        }
    });
}

// shows popup for removing a device from dashboard
function showRemoveConfirmationPopup(agentId) {
    const removePopup = document.getElementById('remove-agent-confirm-popup');
    const agentInfoPopup = document.getElementById('agent-info-popup');
    removePopup.style.display = 'flex';

    const confirmButton = document.getElementById('confirm-remove');
    const cancelButton = document.getElementById('cancel-remove');
    const closeRemovePopup = document.getElementById('close-remove-popup');

    confirmButton.onclick = async function () {
        await removeAgent(agentId);
        removePopup.style.display = 'none';
        agentInfoPopup.style.display = 'none';
        console.log('agentInfoPopup closed after confirming removal.');
        await loadUserAgents();
    };

    cancelButton.onclick = function () {
        removePopup.style.display = 'none';
    };
    closeRemovePopup.onclick = function () {
        removePopup.style.display = 'none';
    };

    window.addEventListener('click', (event) => {
        if (event.target === removePopup) {
            removePopup.style.display = 'none';
        }
    });
}

// removes an agent from dashboard
async function removeAgent(agentId) {
    try {
        const response = await fetch(`/agents/${agentId}/remove`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ userId: null }),
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Failed to remove agent:', errorText);
            alert('Failed to remove the agent. Please try again.');
        }
    } catch (error) {
        console.error('Error removing agent:', error);
        alert('An error occurred. Please try again.');
    }
}

// for changing the ui name
document.addEventListener('DOMContentLoaded', () => {
    const editUiNameBtn = document.getElementById('edit-ui-name');
    const uiNameInput = document.getElementById('ui-name');
    const saveUiNameBtn = document.getElementById('save-ui-name');

    // Load the current uiName and ensure username remains untouched
    async function loadUiName() {
        try {
            const response = await fetch('/auth/currentUser', { credentials: 'include' });
            if (response.ok) {
                const user = await response.json();
                // Populate the uiName input with uiName or username (fallback for new users)
                uiNameInput.value = user.uiName || user.username || '';
                uiNameInput.readOnly = true; // Disable editing by default
                saveUiNameBtn.style.display = 'none'; // Hide save button initially
            } else {
                console.error('Failed to load UI Name:', response.statusText);
                uiNameInput.value = 'Error loading name'; // Display error
            }
        } catch (error) {
            console.error('Error loading UI Name:', error);
            uiNameInput.value = 'Error loading name'; // Display error
        }
    }

    // Enable editing when clicking the edit button
    editUiNameBtn.addEventListener('click', () => {
        uiNameInput.readOnly = false; // Allow editing
        saveUiNameBtn.style.display = 'inline-block'; // Show save button
        uiNameInput.focus(); // Focus on the input
    });

    // Save the updated uiName
    saveUiNameBtn.addEventListener('click', async (event) => {
        event.preventDefault();
        const newUiName = uiNameInput.value.trim(); // Get the new uiName

        if (!newUiName) {
            alert('UI Name cannot be empty!');
            return;
        }

        try {
            const response = await fetch('/auth/uiName', {
                method: 'PUT', // PUT request to update the UI name
                credentials: 'include', // Include session cookies
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({ uiName: newUiName }).toString(),
            });

            if (response.ok) {
                alert('UI Name updated successfully!');
                uiNameInput.readOnly = true; // Disable editing
                saveUiNameBtn.style.display = 'none'; // Hide save button
            } else {
                console.error('Failed to update UI Name:', response.statusText);
                alert('Failed to update UI Name. Please try again.');
            }
        } catch (error) {
            console.error('Error updating UI Name:', error);
            alert('An error occurred while updating your UI Name. Please try again.');
        }
    });

    // Load uiName on page load
    loadUiName();
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

// Notifications, Profile and Help Popups
document.addEventListener('DOMContentLoaded', function () {
    const profileBtn = document.getElementById("profile-btn");
    const notificationsBtn = document.getElementById("notifications-btn");
    const helpBtn = document.getElementById("help-btn");

    const profilePopup = document.getElementById("profile-popup");
    const notificationsPopup = document.getElementById("notifications-popup");
    const helpPopup = document.getElementById("help-popup");

    const closeProfile = document.getElementById("close-profile");
    const closeNotifications = document.getElementById("close-notifications");
    const closeHelp = document.getElementById("close-help");

    // Open popups
    profileBtn.addEventListener("click", () => profilePopup.style.display = "flex");
    notificationsBtn.addEventListener("click", () => notificationsPopup.style.display = "flex");
    helpBtn.addEventListener("click", () => helpPopup.style.display = "flex");

    // Close popups
    closeProfile.addEventListener("click", () => profilePopup.style.display = "none");
    closeNotifications.addEventListener("click", () => notificationsPopup.style.display = "none");
    closeHelp.addEventListener("click", () => helpPopup.style.display = "none");

    profilePopup.addEventListener('click', (event) => {
        if (event.target === profilePopup) {
            profilePopup.style.display = 'none';
        }
    });
    //notificationsPopup doesn't close upon  click outside
    notificationsPopup.addEventListener('click', (event) => {
        if (event.target === notificationsPopup) {
            notificationsPopup.style.display = 'none';
        }
    });
    helpPopup.addEventListener('click', (event) => {
        if (event.target === helpPopup) {
            helpPopup.style.display = 'none';
        }
    });
});

document.addEventListener('DOMContentLoaded', async function () {
    if (window.location.pathname.includes('dashboard.html')) {
        try {
            await loadUserAgents();

            // Event listener for filter button
            const filterBtn = document.getElementById('filter-btn');
            const filterDropdown = document.getElementById('filter-dropdown');
            const applyFilterBtn = document.getElementById('apply-filter-btn');

            filterBtn.addEventListener('click', async () => {
                if (filterDropdown.style.display === 'none') {
                    await loadFilterOptions();
                    filterDropdown.style.display = 'block';
                    applyFilterBtn.style.display = 'block';
                } else {
                    filterDropdown.style.display = 'none';
                    applyFilterBtn.style.display = 'none';
                }
            });

            // Event listener for apply filter button
            applyFilterBtn.addEventListener('click', async () => {
                const selectedCategory = filterDropdown.value;
                if (selectedCategory) {
                    await filterDevicesByCategory(selectedCategory);
                } else {
                    alert('Please select a category!');
                }
            });

        } catch (error) {
            console.error('Error loading dashboard:', error);
            window.location.href = 'index.html';
        }
    }
});

async function loadFilterOptions() {
    try {
        const response = await fetch('/agents/categories', {
            method: 'GET',
            credentials: 'include',
        });

        if (response.ok) {
            const categories = await response.json();
            const filterDropdown = document.getElementById('filter-dropdown');
            filterDropdown.innerHTML = '<option value="" disabled selected>Select a category</option>'; // Reset

            categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.name;
                option.innerText = category.name;
                filterDropdown.appendChild(option);
            });
        } else {
            console.error('Failed to load categories:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching categories:', error);
    }
}

async function filterDevicesByCategory(categoryName) {
    try {
        const response = await fetch(`/agents/filter?tagName=${encodeURIComponent(categoryName)}`, {
            method: 'GET',
            credentials: 'include',
        });

        if (response.ok) {
            const agents = await response.json();
            renderAgentsGrid(agents);
        } else {
            console.error('No agents found for category:', categoryName);
            alert('No agents found for this category.');
        }
    } catch (error) {
        console.error('Error filtering agents:', error);
    }
}

// Gets the Csrf Token
function getCsrfToken() {
    const csrfCookie = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN='));
    return csrfCookie ? csrfCookie.split('=')[1] : '';
}