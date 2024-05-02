/**
 * FileManager class for managing files in local storage.
 */
export default class FileManager {
  /**
   * Constructs a new FileManager instance.
   */
  constructor() {
    this.filePrefix = "code-editor-file-";
  }

  /**
   * Loads file content from local storage.
   * @param {string} file - The name of the file to load.
   * @returns {Array} The content of the file as an array.
   */
  loadFromLocalStorage(file) {
    const storedFile = localStorage.getItem(this.filePrefix + file);
    const fileData = storedFile ? JSON.parse(storedFile) : null;
    return Array.isArray(fileData?.content) ? fileData.content : [];
  }

  /**
   * Saves file content to local storage.
   */
  saveToLocalStorage(content, file) {
    const fileData = {
      name: file,
      content: content,
      timestamp: new Date().getTime(), // Current time
    };
    localStorage.setItem(this.filePrefix + file, JSON.stringify(fileData));
  }

  /**
   * Retrieves all file names from local storage.
   */
  getAllFiles() {
    const files = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key.startsWith(this.filePrefix)) {
        const fileData = JSON.parse(localStorage.getItem(key));
        files.push(fileData);
      }
    }
    // Sort the files by their names
    files.sort((a, b) => a.name.localeCompare(b.name));
    // Return file names
    return files.map((file) => file.name);
  }

  /**
   * Deletes a file from local storage.
   * @param {string} file - The name of the file to delete.
   */
  deleteFile(file) {
    localStorage.removeItem(this.filePrefix + file);
  }

  /**
   * Stores the currently highlighted file in local storage.
   */
  saveCurrentFile(file) {
    localStorage.setItem("currentFile", file);
  }

  /**
   * Retrieves the currently highlighted file from local storage.
   */
  getCurrentFile() {
    return localStorage.getItem("currentFile");
    
  }
  
  func_savedata(data) { 
    var string_data = typeof data === "string" ? data : JSON.stringify(data);
    var file = new File([string_data], this.getCurrentFile() + ".txt", {
      type: "text;charset=utf-8",
    });

    var anchor = document.createElement("a");
    anchor.setAttribute("href", window.URL.createObjectURL(file));
    anchor.setAttribute("download", this.getCurrentFile() + ".txt",);
    anchor.click();
    URL.revokeObjectURL(anchor.href);
  }
}
