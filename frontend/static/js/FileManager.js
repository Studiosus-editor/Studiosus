class FileManager {
  constructor() {
    this.filePrefix = "code-editor-file-";
  }

  loadFromLocalStorage(file) {
    const storedFile = localStorage.getItem(this.filePrefix + file);
    const fileData = storedFile ? JSON.parse(storedFile) : null;
    return Array.isArray(fileData?.content) ? fileData.content : [];
  }

  saveToLocalStorage(content, file) {
    const fileData = {
      name: file,
      content: content,
      timestamp: new Date().getTime(), // Current time
    };
    localStorage.setItem(this.filePrefix + file, JSON.stringify(fileData));
  }

  getAllFiles() {
    const files = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key.startsWith(this.filePrefix)) {
        const fileData = JSON.parse(localStorage.getItem(key));
        files.push(fileData);
      }
    }
    // Sort files by timestamp
    files.sort((a, b) => a.timestamp - b.timestamp);
    // Return file names
    return files.map((file) => file.name);
  }

  deleteFile(file) {
    localStorage.removeItem(this.filePrefix + file);
  }

  // store currently Highlighted file in local storage
  saveCurrentFile(file) {
    localStorage.setItem("currentFile", file);
  }

  // get currently Highlighted file from local storage
  getCurrentFile() {
    return localStorage.getItem("currentFile");
  }
}
