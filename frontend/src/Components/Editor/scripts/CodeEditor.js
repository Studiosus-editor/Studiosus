import YamlChecker from './YamlChecker';
import { errorStore } from './store.js';

export default class CodeEditor {
  constructor(
    editorId,
    textareaId,
    lineNumbersId,
    overlayId,
    fileManager,
    initialFileName = "main"
  ) {
    this.getHtmlElements(editorId, textareaId, lineNumbersId, overlayId);
    this.fileManager = fileManager;
    this.currentFileName =
      this.fileManager.getCurrentFile() || initialFileName;
    this.charObjects = this.fileManager.loadFromLocalStorage(
      this.currentFileName
    );
    this.dragCounter = 0;
    this.textarea.innerText = this.charObjects.map((obj) => obj.char).join("");
    this.addEventListeners();
  }

  getHtmlElements(editorId, textareaId, lineNumbersId, overlayId) {
    this.editor = document.getElementById(editorId);
    this.textarea = document.getElementById(textareaId);
    this.lineNumbers = document.getElementById(lineNumbersId);
    this.overlay = document.getElementById(overlayId);
    this.editorWrapper = document.querySelector("#editor-wrapper");

    const missingElements = [];
    if (!this.editor) missingElements.push("editor");
    if (!this.textarea) missingElements.push("textarea");
    if (!this.lineNumbers) missingElements.push("line numbers");
    if (!this.overlay) missingElements.push("overlay");

    if (missingElements.length > 0) {
      throw new Error(`Missing elements: ${missingElements.join(", ")}`);
    }
  }

  addEventListeners() {
    this.textarea.addEventListener("keyup", (event) => this.onKeyUp(event));
    this.editor.addEventListener("dragenter", this.handleDragEvent);
    this.editor.addEventListener("dragleave", this.handleDragEvent);
    this.editor.addEventListener("drop", this.handleDropEvent);
  }

  onKeyUp() {
    const newText = this.textarea.innerText;
    this.charObjects = this.createCharObjects(newText, this.charObjects);
    this.fileManager.saveToLocalStorage(
      this.charObjects,
      this.currentFileName
    );
  }

  handleDragEvent = (event) => {
    event.preventDefault();
    if (event.type === "dragenter") {
      this.dragCounter++;
      if (this.dragCounter === 1) {
        this.textarea.style.filter = "blur(2px)";
        this.overlay.classList.remove("hidden");
      }
    } else if (event.type === "dragleave" || event.type === "drop") {
      this.dragCounter--;
      if (this.dragCounter === 0) {
        this.textarea.style.filter = "";
        this.overlay.classList.add("hidden");
      }
    }
  };

  handleDropEvent = (event) => {
    event.preventDefault();
    this.handleDragEvent(event);

    // If the textarea is not empty, ask the user for confirmation
    if (
      this.textarea.innerText.trim() !== "" &&
      !confirm("Textarea has content. Replace with file content?")
    ) {
      return;
    }

    const { items, files } = event.dataTransfer;
    const fileList = items ? [...items] : [...files];

    if (fileList.length > 1) {
      alert("Multiple file uploads are not supported yet.");
      return;
    }

    fileList.forEach((item) => {
      const file = items ? item.getAsFile() : item;
      if (file) {
        this.readFile(file);
      } else {
        alert("Dropped item is not a file");
      }
    });
  };

  createCharObjects = (text, existingCharObjects = []) => {
    return Array.from(text).map((char, position) => {
      if (
        existingCharObjects[position] &&
        existingCharObjects[position].char === char
      ) {
        return existingCharObjects[position];
      } else {
        return {
          char: char,
          position: position,
          user: "username",
          date: Date.now(),
        };
      }
    });
  };

  /**
   * Reads the contents of a file and updates the textarea and charObjects accordingly.
   *
   * @param {File} file - The file to be read.
   */
  readFile = (file) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      // Update the textarea value with the contents of the file
      this.textarea.innerText = e.target.result;
      // Create new charObjects based on the file contents
      this.charObjects = this.createCharObjects(e.target.result);
      // Save the updated charObjects to local storage
      this.fileManager.saveToLocalStorage(
        this.charObjects,
        this.currentFileName
      );
    };
    // Read the file as text
    reader.readAsText(file);
  };

  updateDimensions() {
    // Reset the textarea width to its default value
    this.textarea.style.width = "auto";

    // Update the textarea height
    this.textarea.style.height = "auto";
    this.textarea.style.height = this.textarea.scrollHeight + "px";

    // Calculate the width based on the longest line
    const lines = this.textarea.innerText.split("\n");
    let longestLine = lines[0];

    // Find the line with the most characters
    for (let line of lines) {
      if (line.length > longestLine.length) {
        longestLine = line;
      }
    }
    const newWidth = longestLine.length * 9; // 9px width per char

    // Set the width of the textarea to match the width of the longest line
    // only if the new width exceeds the default width
    if (newWidth > this.textarea.offsetWidth) {
      this.textarea.style.width = `${newWidth}px`;
    }
  }
  loadFile(file) {
    this.currentFileName = file;
    this.charObjects = this.fileManager.loadFromLocalStorage(
      this.currentFileName
    );
    this.textarea.innerText = this.charObjects.map((obj) => obj.char).join("");
    errorStore.set(null);

    this.checkYamlSyntax()
    // Reset the scroll position
    this.editorWrapper.scrollTop = 0;
    this.editorWrapper.scrollLeft = 0;

    //Update the width of text area
    this.updateDimensions();
  
    // Save highlighted file in local storage
    this.fileManager.saveCurrentFile(file);
  }

  removeNoBreakSpaceChars(text) {
    return text.replace(/\u00A0/g, " ");
  }

  checkYamlSyntax() {
    let yamlChecker = new YamlChecker(
      this.removeNoBreakSpaceChars(this.textarea.innerText)
    );
    let error = yamlChecker.validateYAML();
    if (error) {
      errorStore.set(error);
    }
  }
}
