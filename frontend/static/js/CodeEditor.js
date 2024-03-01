class TextEditor {
  constructor(editorId, textareaId, lineNumbersId, overlayId) {
    this.getHtmlElements(editorId, textareaId, lineNumbersId, overlayId);
    this.fileManager = new FileManager();
    this.charObjects = this.fileManager.loadFromLocalStorage();
    this.dragCounter = 0;
    this.textarea.value = this.charObjects.map((obj) => obj.char).join("");
    this.updateLineNumbers();
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

  updateLineNumbers() {
    const numberOfLines = this.textarea.value.split("\n").length;
    this.lineNumbers.innerHTML = Array(numberOfLines)
      .fill("<span></span>")
      .join("");
    this.textarea.style.height = "auto"; // Reset, because textarea won't shrink
    this.textarea.style.height = `${this.textarea.scrollHeight}px`;
  }

  onKeyUp(event) {
    const newText = this.textarea.value;
    this.charObjects = this.createCharObjects(newText, this.charObjects);
    this.updateLineNumbers();
    this.fileManager.saveToLocalStorage(this.charObjects);
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
      this.updateLineNumbers();
      this.fileManager.saveToLocalStorage(this.charObjects);
    };
    reader.readAsText(file);
  };
}

new TextEditor("editor", "editor-field", "line-numbers", "overlay");
