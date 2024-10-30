/*
*   The script for the text on the login page. This allows for other languages to later be added - and then be selected
*   by the user.
*
* @author streitwies
*/

// Gets the language data
async function loadLanguageData(lang) {
  const response = await fetch(`languages/${lang}.json`);
  return response.json();
}

// Sets the language preference
function setLanguage(lang) {
  localStorage.setItem("language", lang);
  location.reload();
}

// Updates content based on selected language
function update(langData) {
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

// Actually changes the language
async function changeLanguage(lang) {
  await setLanguage(lang);

  const langData = await loadLanguageData(lang);
  update(langData);
}

// Calls updateContent() on page load
window.addEventListener("DOMContentLoaded", async () => {
  const userPreferredLanguage = localStorage.getItem("language") || "en";
  const langData = await loadLanguageData(userPreferredLanguage);
  update(langData);
});