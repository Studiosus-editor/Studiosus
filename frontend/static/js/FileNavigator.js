class FileNavigator {
  constructor(fileSelectorId, fileManager, codeEditor) {
    this.fileSelector = document.getElementById(fileSelectorId);
    this.fileManager = fileManager;

    this.codeEditor = codeEditor;
    this.updateFileList();
    this.highlightCurrentFile();

    this.createNewPage = this.createNewPage.bind(this);
    this.createNewPageBtn = document.getElementById("create-new-file");
    this.createNewPageBtn.addEventListener("click", this.createNewPage);
  }

  highlightCurrentFile() {
    const currentTab = document.querySelector(
      `#file-selector li[data-file="${this.codeEditor.currentFileName}"]`
    );
    if (currentTab) {
      currentTab.classList.add("active");
    }
  }

  updateFileList() {
    const files = this.fileManager.getAllFiles();
    this.fileSelector.innerHTML = "";
    const ul = document.createElement("ul");
    files.forEach((file) => {
      const li = document.createElement("li");
      li.textContent = file;
      li.dataset.file = file;

      // Create delete button
      const deleteButton = document.createElement("button");
      deleteButton.textContent = "X";
      deleteButton.addEventListener("click", (event) => {
        event.stopPropagation(); // Prevent the li click event from firing

        // Delete the file from localStorage
        this.fileManager.deleteFile(file);

        // Make code editor focus the next file on left if exists, else right, else make focus default
        const files = this.fileManager.getAllFiles();
        const index = files.indexOf(file);
        if (index > 0) {
          this.codeEditor.loadFile(files[index - 1]);
        } else if (index < files.length - 1) {
          this.codeEditor.loadFile(files[index + 1]);
        } else {
          this.codeEditor.loadFile("main");
        }

        // Update the file list
        this.updateFileList();
        this.highlightCurrentFile();
      });

      // Append the delete button to the li
      li.appendChild(deleteButton);

      li.addEventListener("click", () => {
        // Remove 'active' class from all tabs
        const tabs = document.querySelectorAll("#file-selector li");
        tabs.forEach((tab) => tab.classList.remove("active"));

        // Add 'active' class to clicked tab
        li.classList.add("active");

        // Load the file
        this.codeEditor.loadFile(file);

        // Here you can add the code to open the file in the editor
        console.log("You selected: " + file);
      });
      ul.appendChild(li);
    });
    this.fileSelector.appendChild(ul);
  }

  createNewPage() {
    // Ask the user for the new page name
    const newPageName = prompt("Enter the new page name:");
    if (newPageName === null || newPageName.trim() === "") {
      alert("Invalid page name");
      return;
    }

    // Check if file with the same name exitst
    const files = this.fileManager.getAllFiles();
    if (files.includes(newPageName)) {
      alert("File with the same name already exists");
      return;
    }

    // Create the new page
    this.fileManager.saveToLocalStorage([], newPageName);

    // Load the new page in the code editor
    this.codeEditor.loadFile(newPageName);

    // Update the file list
    this.updateFileList();
    this.highlightCurrentFile();
  }
}

const fileManager = new FileManager();
const codeEditor = new CodeEditor(
  "editor",
  "editor-field",
  "line-numbers",
  "overlay",
  fileManager
);
const fileNavigator = new FileNavigator(
  "file-selector",
  fileManager,
  codeEditor
);
