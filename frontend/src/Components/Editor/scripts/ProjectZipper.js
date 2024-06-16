import JSZip from 'jszip';

export default class ProjectZipper {
    constructor() {
        this.zip = new JSZip();
        this.zipName = "project";
    }

    addItemsToZip(folder, parentZip) {
        let currentZip;

        if (parentZip) {
            currentZip = parentZip.folder(folder.name);
        } else {
            currentZip = this.zip;
        }

        folder.files.forEach((file) => {
            currentZip.file(file.name, file.content);
        });

        folder.childFolders.forEach((childFolder) => {
            this.addItemsToZip(childFolder, currentZip);
        });
    }


    updateFileContentFromLocalStorage(folder) {
        folder.files.forEach((file) => {
            const contentFromStorage = localStorage.getItem(file.id);
            file.content = contentFromStorage;
        });

        folder.childFolders.forEach((childFolder) => {
            this.updateFileContentFromLocalStorage(childFolder);
        });

        return folder;
    }

    async downloadProject() {
        const content = await this.zip.generateAsync({ type: "blob" });

        const url = window.URL.createObjectURL(content);
        const anchor = document.createElement("a");
        anchor.href = url;
        anchor.download = `${this.zipName}.zip`;
        document.body.appendChild(anchor);
        anchor.click();
        document.body.removeChild(anchor);

        window.URL.revokeObjectURL(url);
        this.resetZip();
    }

    setZipName(name) {
        this.zipName = name;
    }

    resetZip() {
        this.zip = new JSZip();
    }
}
