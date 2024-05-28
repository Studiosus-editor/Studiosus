import { writable } from 'svelte/store';

export default class FileNavigator {
  constructor(fileManager, codeEditor,currentFile) {
    this.fileManager = fileManager;
    this.codeEditor = codeEditor;
    this.currentPageName = writable(null);
    this.currentFile = currentFile;
  }

  // As the name suggests, creates a new page.
  // Prompts the user for the new page name, checks if a file with the same name already exists,
  //uses fileManager to save the new page, and loads the new page in the code editor.
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
    this.currentPageName.set(newPageName);
    this.currentFile.set(newPageName);
    
  }

  // Deletes the file from the fileManager and loads the next file in the code editor.
deleteFile(file) {
  // Get the current index of the file to be deleted
  const filesBeforeDeletion = this.fileManager.getAllFiles();
  const index = filesBeforeDeletion.indexOf(file);

  // Delete the file from localStorage
  this.fileManager.deleteFile(file);

  // Make code editor focus the next file on left if exists, else right, else make focus default
  const filesAfterDeletion = this.fileManager.getAllFiles();

  let nextFile;
  if (index > 0) {
    nextFile = filesAfterDeletion[index - 1];
  } else if (filesAfterDeletion.length > 0) {
    nextFile = filesAfterDeletion[0];
  } else {
    nextFile = "main";
  }
  this.codeEditor.loadFile(nextFile);
  this.currentPageName.set(nextFile);
  this.currentFile.set(nextFile); // Update the currentFile store
}
  get currentFileName() {
    return this.currentPageName;
  }
}
