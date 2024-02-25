class TextEditor {
  constructor(textareaId, lineNumbersId) {
    this.textarea = document.getElementById(textareaId);
    this.lineNumbers = document.getElementById(lineNumbersId);

    if (!this.textarea) {
      throw new Error(`Textarea with id ${textareaId} not found`);
    }
    if (!this.lineNumbers) {
      throw new Error(
        `Line numbers element with id ${lineNumbersId} not found`
      );
    }

    this.charObjects = this.loadFromLocalStorage();
    this.textarea.value = this.charObjects.map((obj) => obj.char).join("");
    this.updateLineNumbers();
    this.textarea.addEventListener("keyup", (event) => this.onKeyUp(event));
  }

  loadFromLocalStorage() {
    const storedText = localStorage.getItem("text");
    return storedText ? JSON.parse(storedText) : [];
  }

  saveToLocalStorage() {
    localStorage.setItem("text", JSON.stringify(this.charObjects));
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
    const newCharObjects = [];

    for (let i = 0; i < newText.length; i++) {
      const char = newText[i];

      if (this.charObjects[i] && this.charObjects[i].char === char) {
        newCharObjects.push(this.charObjects[i]);
      } else {
        newCharObjects.push({
          char: char,
          position: i,
          user: "username", // Mock username
          date: Date.now(),
        });
      }
    }

    this.charObjects = newCharObjects;
    this.updateLineNumbers();
    this.saveToLocalStorage();
  }
}

new TextEditor("editor-field", "line-numbers");
