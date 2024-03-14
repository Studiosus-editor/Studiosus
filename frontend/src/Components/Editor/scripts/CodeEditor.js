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
    this.currentFileName = this.fileManager.getCurrentFile() || initialFileName;
    this.charObjects = this.fileManager.loadFromLocalStorage(
      this.currentFileName
    );
    this.dragCounter = 0;
    this.textarea.value = this.charObjects.map((obj) => obj.char).join("");
    this.addEventListeners();
  }

  getHtmlElements(editorId, textareaId, lineNumbersId, overlayId) {
    this.editor = document.getElementById(editorId);
    this.textarea = document.getElementById(textareaId);
    this.lineNumbers = document.getElementById(lineNumbersId);
    this.overlay = document.getElementById(overlayId);

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

  onKeyUp(event) {
    const newText = this.textarea.value;
    this.charObjects = this.createCharObjects(newText, this.charObjects);
    this.fileManager.saveToLocalStorage(this.charObjects, this.currentFileName);
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
      this.textarea.value.trim() !== "" &&
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
          user: "username", // Mock username
          date: Date.now(),
        };
      }
    });
  };

  readFile = (file) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.textarea.value = e.target.result;
      this.charObjects = this.createCharObjects(e.target.result);
      this.fileManager.saveToLocalStorage(
        this.charObjects,
        this.currentFileName
      );
    };
    reader.readAsText(file);
  };

  loadFile(file) {
    this.currentFileName = file;
    this.charObjects = this.fileManager.loadFromLocalStorage(
      this.currentFileName
    );
    this.textarea.value = this.charObjects.map((obj) => obj.char).join("");
    // save highlighted file in local storage
    this.fileManager.saveCurrentFile(file);
  }
}