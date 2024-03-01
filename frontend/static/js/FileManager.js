class FileManager {
  constructor() {}

  loadFromLocalStorage(file = "file1") {
    const storedFiles = localStorage.getItem(file);
    return storedFiles ? JSON.parse(storedFiles) : [];
  }

  saveToLocalStorage(content, file = "file1") {
    localStorage.setItem(file, JSON.stringify(content));
  }
}
