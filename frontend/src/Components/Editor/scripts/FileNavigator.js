export default class FileNavigator {
  constructor(fileManager, codeEditor) {
    this.fileManager = fileManager;
    this.codeEditor = codeEditor;
  }

  createNewPage() {
    // Ask the user for the new page name
    const newPageName = prompt("Enter the new page name:");
    if (newPageName === null || newPageName.trim() === "") {
      alert("Invalid page name");
      return;
    }

    // Check if file with the same name exists
    const files = this.fileManager.getAllFiles();
    if (files.includes(newPageName)) {
      alert("File with the same name already exists");
      return;
    }

    // Create the new page
    this.fileManager.saveToLocalStorage([], newPageName);

    // Load the new page in the code editor
    this.codeEditor.loadFile(newPageName);
  }

  deleteFile(file) {
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
  }
}
