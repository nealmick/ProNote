var editor = ace.edit("editor");
var savedTheme = localStorage.getItem("editorTheme") || "ace/theme/monokai";
var savedLanguage = localStorage.getItem("editorLanguage") || "ace/mode/python";
var savedFontSize = localStorage.getItem("editorFontSize") || "14px"; // Default font size or saved font size

editor.setTheme(savedTheme);
editor.session.setMode(savedLanguage);
editor.setOption("fontSize", savedFontSize); // Set the font size from local storage
editor.getSession().setValue(document.getElementById("content").value);

// Enable word wrap
editor.getSession().setUseWrapMode(true);
// dissable margin line on the right side of the editor
editor.setOption("showPrintMargin", false);

document.getElementById("theme-selector").value = savedTheme;
document.getElementById("language-selector").value = savedLanguage;
document.getElementById("font-size-selector").value = savedFontSize; // Set the font size selector

document
  .getElementById("theme-selector")
  .addEventListener("change", function () {
    editor.setTheme(this.value);
    localStorage.setItem("editorTheme", this.value);
  });

document
  .getElementById("language-selector")
  .addEventListener("change", function () {
    editor.session.setMode(this.value);
    localStorage.setItem("editorLanguage", this.value);
  });

document
  .getElementById("font-size-selector")
  .addEventListener("change", function () {
    editor.setOption("fontSize", this.value);
    localStorage.setItem("editorFontSize", this.value); // Save font size to local storage
  });

editor.getSession().on("change", function () {
  document.getElementById("content").value = editor.getSession().getValue();
});
