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
   * @param {Array} content - The content of the file to save.
   * @param {string} file - The name of the file to save.
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
   * @returns {Array} An array of file names.
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
   * @param {string} file - The name of the currently highlighted file.
   */
  saveCurrentFile(file) {
    localStorage.setItem("currentFile", file);
  }

  /**
   * Retrieves the currently highlighted file from local storage.
   * @returns {string} The name of the currently highlighted file.
   */
  getCurrentFile() {
    return localStorage.getItem("currentFile");
  }
}
