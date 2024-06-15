// Responsible for handling file operations for unregistered users

export function createFileForArray(root, fileName, folderId) {
    let currentMaxId = 0;

    function updateMaxId(folder) {
        folder.files.forEach(file => {
            if (file.id > currentMaxId) {
                currentMaxId = file.id;
            }
        });
        folder.childFolders.forEach(childFolder => {
            updateMaxId(childFolder);
        });
    }
    updateMaxId(root);

    function addFile(folder) {
        if (folder.id === folderId) {
            folder.files.push({
                id: currentMaxId + 1,
                name: fileName,
                parentFolderId: folderId
            });
            return true;
        }
        for (let childFolder of folder.childFolders) {
            if (addFile(childFolder)) return true;
        }
        return false;
    }

    addFile(root);
    return currentMaxId + 1;
}

export function deleteFileForArray(root, fileId) {

    function removeFile(folder) {
        const fileIndex = folder.files.findIndex(file => file.id === fileId);
        if (fileIndex !== -1) {
            folder.files.splice(fileIndex, 1);
            return true;
        }

        for (let childFolder of folder.childFolders) {
            const fileFound = removeFile(childFolder);
            if (fileFound) return true;
        }
        return false;
    }

    const fileRemoved = removeFile(root);
}

export function createFolderForArray(root, folderName, parentFolderId) {
    let currentMaxFolderId = 0;

    function updateMaxFolderId(folder) {
        if (folder.id > currentMaxFolderId) {
            currentMaxFolderId = folder.id;
        }
        folder.childFolders.forEach(childFolder => {
            updateMaxFolderId(childFolder);
        });
    }

    updateMaxFolderId(root);

    function addFolder(folder) {
        if (folder.id === parentFolderId) {
            folder.childFolders.push({
                id: currentMaxFolderId + 1,
                name: folderName,
                parentFolderId: parentFolderId,
                files: [],
                childFolders: []
            });
            return true;
        }
        for (let childFolder of folder.childFolders) {
            if (addFolder(childFolder)) return true;
        }
        return false;
    }

    addFolder(root);
    return currentMaxFolderId + 1;
}

export function deleteFolderForArray(root, folderId) {

    function removeFolder(folders, parentId) {
        for (let i = 0; i < folders.length; i++) {
            const folder = folders[i];
            if (folder.id === folderId) {
                folders.splice(i, 1);
                return true;
            }
            const folderRemoved = removeFolder(folder.childFolders, folder.id);
            if (folderRemoved) return true;
        }
        return false;
    }

    removeFolder([root], null);
}

export function renameFileForArray(root, fileId, newName) {
    function renameFileInFolder(folder) {
        const file = folder.files.find(file => file.id === fileId);
        if (file) {
            file.name = newName; 
            return true;
        }

        for (let childFolder of folder.childFolders) {
            const fileRenamed = renameFileInFolder(childFolder);
            if (fileRenamed) return true;
        }
        return false;
    }

    renameFileInFolder(root);
}

export function renameFolderForArray(root, folderId, newName) {

    function renameFolderInHierarchy(folder) {
        if (folder.id === folderId) {
            folder.name = newName;
            return true;
        }

        for (let childFolder of folder.childFolders) {
            const folderRenamed = renameFolderInHierarchy(childFolder);
            if (folderRenamed) return true;
        }
        return false;
    }

    renameFolderInHierarchy(root);
}

export function moveFileForArray(root, fileId, moveToFolderId) {
    let fileToMove = null;

    // Finds and removes the file from the hierarchy, while also storing it for later moving
    function findAndRemoveFile(folder) {
        for (let i = 0; i < folder.files.length; i++) {
            if (folder.files[i].id == fileId) {
                fileToMove = folder.files.splice(i, 1)[0];
                return true;
            }
        }
        for (let childFolder of folder.childFolders) {
            const found = findAndRemoveFile(childFolder);
            if (found) return true;
        }
        return false;
    }

    function insertFileIntoFolder(folder) {
        if (folder.id === moveToFolderId) {
            fileToMove.parentFolderId = moveToFolderId;
            folder.files.push(fileToMove);
            return true
        }
        for (let childFolder of folder.childFolders) {
            const inserted = insertFileIntoFolder(childFolder);
            if (inserted) return true;
        }
        return false;
    }

    findAndRemoveFile(root);
    insertFileIntoFolder(root);
}

export function moveFolderForArray(root, folderId, newParentId) {
    let folderToMove = null;
    let found = false;

    function findAndRemove(folder) {
        if (folder.childFolders) {
            for (let i = 0; i < folder.childFolders.length; i++) {
                if (folder.childFolders[i].id === folderId) {
                    folderToMove = folder.childFolders.splice(i, 1)[0];
                    return true;
                }
                found = findAndRemove(folder.childFolders[i]);
                if (found) break;
            }
        }
        return found;
    }

    findAndRemove(root);

    folderToMove.parentFolderId = newParentId;

    function addFolderToNewParent(folder) {
        if (folder.id === newParentId) {
            folder.childFolders.push(folderToMove);
            return true;
        }
        if (folder.childFolders) {
            for (let child of folder.childFolders) {
                const added = addFolderToNewParent(child);
                if (added) return true;
            }
        }
        return false;
    }

    addFolderToNewParent(root);
}

export function removeFilesFromLocalStorage(projectStructure, folderId) {
    
    function traverseAndRemoveFiles(folder, removeFiles) {
        if (folderId === folder.id || removeFiles) {
            folder.files.forEach(file => {
                localStorage.removeItem(file.id.toString());
            });
            removeFiles = true;
        }

        folder.childFolders.forEach(childFolder => {
            traverseAndRemoveFiles(childFolder, removeFiles);
        });
    }

    traverseAndRemoveFiles(projectStructure, false);
}