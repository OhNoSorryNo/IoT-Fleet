/*
*   The script for on the login page. This allows for other languages to later be added - and then be selected
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
async function fetchLanguageData(lang) {
    const response = await fetch(`languages/de.json`);
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