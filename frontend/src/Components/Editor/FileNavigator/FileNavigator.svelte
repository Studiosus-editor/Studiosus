<script>
  import { createEventDispatcher, onMount } from "svelte";
  import { _ } from "svelte-i18n";
  import ProjectZipper from "../scripts/ProjectZipper.js";
  import AddFileIcon from "../../../assets/svg/add-file-icon.svg";
  import AddFolderIcon from "../../../assets/svg/add-folder-icon.svg";
  import RepromptFolderDelete from "../../Modal/Components/RepromptFolderDelete.svelte";
  import Modal from "../../Modal/Modal.svelte";
  import { addToast } from "../../Modal/ToastNotification/toastStore.js";
  import {
    createFileStore,
    createFolderStore,
    currentContextMenuStore,
    currentFileNameStore,
    currentFileStore,
    currentFolderStore,
    currentHighlightedItemStore,
    currentlyDraggedItemId,
    currentlyDraggedItemNameStore,
    currentlyOpenedFolderStore,
    deleteFileStore,
    deleteFolderFileIdStore,
    deleteFolderFolderIdStore,
    deleteFolderStore,
    forbbidenDragIdArrayStore,
    isViewerOrTemplateStore,
    itemIsDraggedOver_ParentIdStore,
    moveToItemIdStore,
    openedRemoteFilesStore,
    projectNameStore,
    renameFileStore,
    renameFolderStore,
    showCreateFileStore,
    showCreateFolderStore,
    stompClientStore,
    stopClientConnectStore,
    textareaValueStore,
  } from "./../scripts/store.js";
  import CreateFileOrFolder from "./CreateOrRenameFileOrFolder.svelte";
  import File from "./File.svelte";
  import Folder from "./Folder.svelte";
  import {
    createFileForArray,
    createFolderForArray,
    deleteFileForArray,
    deleteFolderForArray,
    moveFileForArray,
    moveFolderForArray,
    removeFilesFromLocalStorage,
    renameFileForArray,
    renameFolderForArray,
  } from "../scripts/fileSystemOperations.js";
  import {
    createFile,
    createFolder,
    deleteFolder,
    fetchFileData,
    fetchProjectStructure,
    fetchTemplateStructure,
    handleDeleteFile,
    moveFile,
    moveFolder,
    renameFile,
    renameFolder,
  } from "../scripts/requests.js";

  export let params;
  export let textarea;
  export let isTemplate = false;

  let unregisteredUserStructure = {
    id: 1,
    parentFolderId: null,
    files: [],
    childFolders: [],
  };

  const backendUrl = __BACKEND_URL__;
  const dispatch = createEventDispatcher();

  let projectStructure = [];
  let isLoading = true;
  let errorLoadingStructure = false;

  let isFolderDeleteModalOpen = false;
  let deletionProcessFolderName;

  let isRootFolderDraggedOver;

  onMount(async () => {
    isViewerOrTemplateStore.set(
      Boolean(isTemplate || (params && params.role === "VIEWER"))
    );

    currentContextMenuStore.set(false);
    const storedProjectStructure = JSON.parse(
      localStorage.getItem("projectStructure")
    );
    if (params) {
      await loadProjectStructure();
    } else if (storedProjectStructure) {
      projectStructure = storedProjectStructure;
    } else {
      projectStructure = unregisteredUserStructure;
    }
    // set the current folder to the root folder,
    // so when a user creates a file, it will be created in the root folder
    currentFolderStore.set(projectStructure.id);
    //event listener from GeneralToolbar to listen if file uploading is called
    document.addEventListener("file-upload", handleFileUpload);
    document.addEventListener("download-project", handleProjectDownload);
    document.addEventListener("add-to-projects", handleAddToProjects);
  });

  // highlights root directory if it is not forbidden to drop items there
  $: isRootFolderDraggedOver =
    $itemIsDraggedOver_ParentIdStore === projectStructure.id &&
    !$forbbidenDragIdArrayStore.includes(projectStructure.id);

  // reactive statments for root elements to show folder/file creation
  $: showFileCreateOption =
    $showCreateFileStore === true &&
    $currentFolderStore === projectStructure.id;
  $: showFolderCreateOption =
    $showCreateFolderStore === true &&
    $currentFolderStore === projectStructure.id;

  $: if ($stopClientConnectStore === true) {
    stopClientConnectStore.set(false);
    // timeout is needed to wait for stompClient to be initialized
    setTimeout(() => {
      $stompClientStore.subscribe(
        `/topic/${params.id}/structureChange`,
        function () {
          let currentHighlightedItem = $currentHighlightedItemStore;
          let currentlyOpenedFolders = $currentlyOpenedFolderStore;
          loadProjectStructure();
          currentHighlightedItemStore.set(currentHighlightedItem);
          currentlyOpenedFolderStore.set(currentlyOpenedFolders);
        }
      );
    }, 200);
  }

  // reactive statments that trigger file system operations
  $: if ($deleteFileStore) {
    if (params) {
      handleDeleteFile(
        params.id,
        $_("fileSystemToastNotifications.errorDeletingFile"),
        $deleteFileStore
      ).then(() => {
        cleanupAfterFileDeletion();
        $stompClientStore.send(
          `/app/chat.structureChange/${params.id}`,
          {},
          {}
        );
        loadProjectStructure();
      });
    } else {
      deleteFileForArray(projectStructure, $deleteFileStore);
      localStorage.removeItem($deleteFileStore);
      cleanupAfterFileDeletion();
      saveAndRefreshStructure();
    }
  }
  $: if ($currentFileStore) {
    handleSelectFile();
  }
  $: if ($deleteFolderStore) {
    initializeDeleteFolder();
  }
  $: if ($renameFileStore) {
    let [id, newName] = $renameFileStore;
    if (params) {
      renameFile(
        params.id,
        $_("fileSystemToastNotifications.errorRenamingFile"),
        id,
        newName
      ).then(() => {
        renameFileStore.set(null);
        $stompClientStore.send(
          `/app/chat.structureChange/${params.id}`,
          {},
          {}
        );
      });
    } else {
      renameFileForArray(projectStructure, id, newName);
      renameFileStore.set(null);
      saveAndRefreshStructure();
    }
  }
  $: if ($renameFolderStore) {
    let [id, newName] = $renameFolderStore;
    if (params) {
      renameFolder(
        params.id,
        $_("fileSystemToastNotifications.errorRenamingFolder"),
        id,
        newName
      ).then(() => {
        renameFolderStore.set(null);
        $stompClientStore.send(
          `/app/chat.structureChange/${params.id}`,
          {},
          {}
        );
      });
    } else {
      renameFolderForArray(projectStructure, id, newName);
      renameFolderStore.set(null);
      saveAndRefreshStructure();
    }
  }
  $: if ($createFileStore) {
    if (params) {
      createFile(
        params.id,
        $_("fileSystemToastNotifications.errorCreatingFile"),
        $createFileStore,
        $currentFolderStore
      ).then((details) => {
        createFileStore.set(null);
        $stompClientStore.send(
          `/app/chat.structureChange/${params.id}`,
          {},
          {}
        );
        currentFileStore.set(details.id);
        currentFileNameStore.set(details.name);
        textarea.innerText = "";
        currentHighlightedItemStore.set("file-" + details.id);
        dispatch("fileInteraction");
      });
    } else {
      let newFileId = createFileForArray(
        projectStructure,
        $createFileStore,
        $currentFolderStore
      );
      createFileStore.set(null);
      saveAndRefreshStructure();
      currentFileStore.set(newFileId);
      textarea.innerText = "";
      textareaValueStore.set("");
      currentHighlightedItemStore.set("file-" + newFileId);
      dispatch("fileInteraction");
    }
  }
  $: if ($createFolderStore) {
    if (params) {
      createFolder(
        params.id,
        $_("fileSystemToastNotifications.errorCreatingFolder"),
        $createFolderStore,
        $currentFolderStore
      ).then((details) => {
        createFolderStore.set(null);
        $stompClientStore.send(
          `/app/chat.structureChange/${params.id}`,
          {},
          {}
        );
        currentFolderStore.set(details.id);
        currentHighlightedItemStore.set("folder-" + details.id);
        currentlyOpenedFolderStore.update((store) => {
          store.push("folder-" + details.id);
          return store;
        });
      });
    } else {
      let newFolderid = createFolderForArray(
        projectStructure,
        $createFolderStore,
        $currentFolderStore
      );
      createFolderStore.set(null);
      saveAndRefreshStructure();
      currentFolderStore.set(newFolderid);
      currentHighlightedItemStore.set("folder-" + newFolderid);
    }
  }
  $: if ($moveToItemIdStore) {
    handleMoveItem();
  }

  async function loadProjectStructure() {
    projectStructure = [];
    isLoading = true;
    const fetchedProjectStructure = isTemplate
      ? await fetchTemplateStructure(
          params.id,
          $_("fileSystemToastNotifications.errorFetchingProjectStructure")
        )
      : await fetchProjectStructure(
          params.id,
          $_("fileSystemToastNotifications.errorFetchingProjectStructure")
        );
    if (fetchedProjectStructure === null) {
      isLoading = false;
      errorLoadingStructure = true;
      return;
    }
    projectStructure = fetchedProjectStructure;
    projectNameStore.set(projectStructure.name);
    isLoading = false;
  }

  function cleanupAfterFileDeletion() {
    if ($currentFileStore === $deleteFileStore) {
      currentFileStore.set(null);
    }
    deleteFileStore.set(null);
  }

  function saveAndRefreshStructure() {
    projectStructure = { ...projectStructure };
    localStorage.setItem("projectStructure", JSON.stringify(projectStructure));
  }

  // initalizes file or folder creation input fields inside root(here) or folder component
  function showFileCreationField() {
    showCreateFileStore.set(true);
  }

  function showFolderCreationField() {
    showCreateFolderStore.set(true);
  }

  function initializeDeleteFolder() {
    let [, promptDeletion, folderName] = $deleteFolderStore;
    if (promptDeletion) {
      deletionProcessFolderName = folderName;
      isFolderDeleteModalOpen = true;
    } else {
      handleDeleteFolder();
    }
  }
  function handleDeleteFolder() {
    if (params) {
      deleteFolder(
        params.id,
        $_("fileSystemToastNotifications.errorDeletingFolder"),
        $deleteFolderStore[0]
      ).then(() => {
        cleanupAfterFolderDeletion();
        $stompClientStore.send(
          `/app/chat.structureChange/${params.id}`,
          {},
          {}
        );
      });
    } else {
      removeFilesFromLocalStorage(projectStructure, $deleteFolderStore[0]);
      deleteFolderForArray(projectStructure, $deleteFolderStore[0]);
      cleanupAfterFolderDeletion();
      saveAndRefreshStructure();
    }
  }

  function cleanupAfterFolderDeletion() {
    closeFolderDeleteModal(); // also resets the deleteFolderStore
    if ($deleteFolderFileIdStore) {
      if ($deleteFolderFileIdStore.includes($currentFileStore)) {
        currentFileStore.set(null);
      }
    }
    if ($deleteFolderFolderIdStore.includes($currentFolderStore)) {
      currentFolderStore.set(projectStructure.id);
    }
    deleteFolderFileIdStore.set(null);
    deleteFolderFolderIdStore.set(null);
  }

  function handleMoveItem() {
    let parts = $currentlyDraggedItemId.split("-");
    let type = parts[0];
    let itemId = parts[1];
    if (type === "folder") {
      if (params) {
        moveFolder(
          params.id,
          $_("fileSystemToastNotifications.errorMovingFolder"),
          itemId,
          $moveToItemIdStore
        ).then(() => {
          currentlyDraggedItemId.set(null);
          moveToItemIdStore.set(null);
          $stompClientStore.send(
            `/app/chat.structureChange/${params.id}`,
            {},
            {}
          );
        });
      } else {
        moveFolderForArray(
          projectStructure,
          parseInt(itemId),
          $moveToItemIdStore
        );
        if ($currentHighlightedItemStore == "folder-" + itemId) {
          currentHighlightedItemStore.set(null);
        }
        currentlyDraggedItemId.set(null);
        moveToItemIdStore.set(null);
        saveAndRefreshStructure();
      }
    } else if (type === "file") {
      if (params) {
        moveFile(
          params.id,
          $_("fileSystemToastNotifications.errorMovingFile"),
          itemId,
          $moveToItemIdStore
        ).then(() => {
          currentlyDraggedItemId.set(null);
          moveToItemIdStore.set(null);
          $stompClientStore.send(
            `/app/chat.structureChange/${params.id}`,
            {},
            {}
          );
        });
      } else {
        moveFileForArray(projectStructure, itemId, $moveToItemIdStore);
        currentlyDraggedItemId.set(null);
        moveToItemIdStore.set(null);
        saveAndRefreshStructure();
      }
    }
  }

  async function handleSelectFile() {
    if (params) {
      let fileData = await fetchFileData(
        params.id,
        $_("fileSystemToastNotifications.errorFetchingFileData"),
        $currentFileStore,
        isTemplate
      );
      openedRemoteFilesStore.set($currentFileStore);
      textarea.innerText = fileData;
      textareaValueStore.set(fileData);
    } else if (localStorage.getItem($currentFileStore)) {
      let fileData = localStorage.getItem($currentFileStore);
      textarea.innerText = fileData;
      textareaValueStore.set(fileData);
    } else {
      textarea.innerText = "";
      textareaValueStore.set("");
    }
    dispatch("fileInteraction");
  }

  function handleFileUpload() {
    const input = document.createElement("input");
    input.type = "file";
    input.multiple = true;

    input.onchange = (event) => {
      const files = event.target.files;
      // TODO: check if file amount does not exceed file all file upload LIMIT and add toast if it does
      if (files.length > 0) {
        const fileNames = Array.from(files).map((file) => file.name);

        const existingRootFileNames = projectStructure.files.map(
          (file) => file.name
        );

        const duplicates = fileNames.filter((name) =>
          existingRootFileNames.includes(name)
        );

        if (duplicates.length > 0) {
          addToast({
            type: "error",
            message:
              $_("fileSystemToastNotifications.errorDuplicatesExistOnUpload") +
              " " +
              duplicates,
          });
        } else {
          createUploadedFiles(files);
        }
      }
    };
    input.click();
  }

  async function createUploadedFiles(files) {
    let lastFileid = null;
    let lastFileName = "";
    let errorIndex = -1;

    const filesArray = Array.from(files);

    try {
      for (let i = 0; i < filesArray.length; i++) {
        let file = filesArray[i];
        if (params) {
          try {
            let content = await getFileContent(filesArray[i]);
            let details = await createFile(
              params.id,
              $_("fileSystemToastNotifications.errorCreatingFile"),
              file.name,
              projectStructure.id,
              content
            );
            lastFileid = details.id;
            lastFileName = details.name;
          } catch (fileName) {
            addToast({
              type: "error",
              message:
                $_("fileSystemToastNotifications.errorReadingFile") +
                " " +
                fileName,
            });
            errorIndex = i;
            throw new Error(); // throw error to stop the loop
          }
        } else {
          try {
            let content = await getFileContent(filesArray[i]);
            let newFileId = createFileForArray(projectStructure, file.name, 1);
            localStorage.setItem(newFileId, content);
            lastFileid = newFileId;
            lastFileName = file.name;
          } catch (fileName) {
            addToast({
              type: "error",
              message:
                $_("fileSystemToastNotifications.errorReadingFile") +
                " " +
                fileName,
            });
            errorIndex = i;
            throw new Error();
          }
        }
      }
    } catch {} // do nothing since error is already handled using errorIndex

    if (errorIndex !== 0) {
      if (params) {
        $stompClientStore.send(
          `/app/chat.structureChange/${params.id}`,
          {},
          {}
        );
        currentFileStore.set(lastFileid);
        currentFileNameStore.set(lastFileName);
        currentHighlightedItemStore.set("file-" + lastFileid);
        dispatch("fileInteraction");
      } else {
        saveAndRefreshStructure();
        currentFileStore.set(lastFileid);
        currentFileNameStore.set(lastFileName);
        textareaValueStore.set("");
        currentHighlightedItemStore.set("file-" + lastFileid);
        dispatch("fileInteraction");
      }
    }
  }

  async function handleAddToProjects() {
    return fetch(
      backendUrl + `/api/template/${params.id}/project/${$projectNameStore}`,
      {
        method: "POST",
        headers: {
          Accept: "application/json",
        },
        credentials: "include",
      }
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(() => {
        addToast({
          message: $_("templates.templateAddedToProjects"),
          type: "success",
        });
      })
      .catch(() => {
        addToast({
          message: $_("templates.errorAddingTemplateToProjects"),
          type: "error",
        });
      });
  }

  async function getFileContent(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (e) => {
        resolve(e.target.result);
      };

      reader.onerror = () => {
        reject(file.name);
      };

      reader.readAsText(file);
    });
  }

  function closeFolderDeleteModal() {
    isFolderDeleteModalOpen = false;
    deleteFolderStore.set(null);
  }
  function hideContextMenu(event) {
    if (!event.target.closest(".context-menu")) {
      currentContextMenuStore.set(null);
    }
  }

  function handleDropOnRootFolder() {
    isRootFolderDraggedOver = false;
    itemIsDraggedOver_ParentIdStore.set(null);
    if (!isNameForbidden()) {
      if (
        !$forbbidenDragIdArrayStore.includes(projectStructure.id) &&
        $currentlyDraggedItemId
      ) {
        moveToItemIdStore.set(projectStructure.id); // calls API
      } else {
        currentlyDraggedItemId.set(null);
      }
    } else {
      currentlyDraggedItemId.set(null);
    }
  }
  function handleDragOver(event) {
    event.preventDefault();
    if ($currentlyDraggedItemId === null) {
      return;
    }
    if (!$forbbidenDragIdArrayStore.includes(projectStructure.id)) {
      isRootFolderDraggedOver = true;
    }
  }
  function handleDragLeave() {
    isRootFolderDraggedOver = false;
  }

  function isNameForbidden() {
    if (!$currentlyDraggedItemId) return false;
    let parts = $currentlyDraggedItemId.split("-");
    let type = parts[0];
    if (type === "folder") {
      const forbiddenFolderNames = projectStructure.childFolders.map(
        (folder) => folder.name
      );
      if (forbiddenFolderNames.includes($currentlyDraggedItemNameStore)) {
        addToast({
          message: $_(
            "fileSystemToastNotifications.errrorFolderWithSameNameExists"
          ),
          type: "error",
        });
        return true;
      }
      return false;
    } else if (type === "file") {
      const forbiddenFileNames = projectStructure.files.map(
        (file) => file.name
      );
      if (forbiddenFileNames.includes($currentlyDraggedItemNameStore)) {
        addToast({
          message: $_(
            "fileSystemToastNotifications.errrorFileWithSameNameExists"
          ),
          type: "error",
        });
        return true;
      }
      return false;
    }
  }

  function setCurrentFolderToRoot(event) {
    event.stopPropagation();
    currentHighlightedItemStore.set(null);
    currentFolderStore.set(projectStructure.id);
  }

  async function handleProjectDownload() {
    let projectZipper = new ProjectZipper();

    if (params && isTemplate) {
      projectStructure = await fetchTemplateStructure(
        params.id,
        $_("fileSystemToastNotifications.errorFetchingProjectStructure"),
        true
      );
      if (projectStructure) {
        projectZipper.addItemsToZip(projectStructure);
        projectZipper.setZipName(projectStructure.name);
        projectZipper.downloadProject();
      }
    } else if (params && !isTemplate) {
      projectStructure = await fetchProjectStructure(
        params.id,
        $_("fileSystemToastNotifications.errorFetchingProjectStructure"),
        true
      );
      if (projectStructure) {
        projectZipper.addItemsToZip(projectStructure);
        projectZipper.setZipName(projectStructure.name);
        projectZipper.downloadProject();
      }
    } else {
      projectStructure =
        projectZipper.updateFileContentFromLocalStorage(projectStructure);
      projectZipper.addItemsToZip(projectStructure);
      projectZipper.setZipName($_("editor.fileNavigator.defaultProjectName"));
      projectZipper.downloadProject();
    }
  }
</script>

<svelte:window on:click={hideContextMenu} />

{#if isFolderDeleteModalOpen}
  <Modal
    panelName={$_("modalPromptFolderDelete.confirmFolderDeletion")}
    width="500px"
    on:closeModal={closeFolderDeleteModal}
    ><RepromptFolderDelete
      folderName={deletionProcessFolderName}
      on:closeModal={closeFolderDeleteModal}
      on:confirmDelete={handleDeleteFolder}
    /></Modal
  >
{/if}
<div id="file-navigator">
  <div id="project-toolbar">
    {#if $projectNameStore}
      <input disabled id="project-name" type="text" value={$projectNameStore} />
    {/if}
    {#if !$isViewerOrTemplateStore}
      <button
        title={$_("editor.projectToolbar.createFile")}
        class="project-toolbar-btn"
        id="create-new-file"
        on:click={showFileCreationField}
      >
        <img
          src={AddFileIcon}
          alt="Create new file"
          width="22px"
          height="22px"
        />
      </button>
      <button
        title={$_("editor.projectToolbar.createFolder")}
        class="project-toolbar-btn"
        id="create-new-folder"
        on:click={showFolderCreationField}
      >
        <img
          src={AddFolderIcon}
          alt="Create new folder"
          width="22px"
          height="22px"
          background="transparent"
        />
      </button>
    {/if}
  </div>
  <div id="file-system-container">
    <ul
      id="file-selector"
      on:dragover={handleDragOver}
      on:dragleave={handleDragLeave}
      on:drop={handleDropOnRootFolder}
      on:click={setCurrentFolderToRoot}
      on:keydown={setCurrentFolderToRoot}
      class:drag-over={isRootFolderDraggedOver}
    >
      {#if isLoading && params}
        <!-- Do not show anything to prevent flickering -->
      {:else if errorLoadingStructure}
        <h4 class="text-center">
          {$_("editor.fileNavigator.errorLoadingProjectStructure")}
        </h4>
      {:else}
        {#if showFolderCreateOption}
          <CreateFileOrFolder
            isNested={false}
            isForFile={false}
            isCreate={true}
            currentContainerFolderNames={projectStructure.childFolders.map(
              (folder) => folder.name
            )}
          />
        {/if}
        {#if projectStructure.childFolders && projectStructure.childFolders.length > 0}
          {#each projectStructure.childFolders as folder}
            <Folder
              {folder}
              isNested={false}
              folderName={folder.name}
              id={folder.id}
              parentFolderId={folder.parentFolderId}
            />
          {/each}
        {/if}
        {#if showFileCreateOption}
          <CreateFileOrFolder
            isNested={false}
            isForFile={true}
            isCreate={true}
            currentContainerFileNames={projectStructure.files.map(
              (file) => file.name
            )}
          />
        {/if}
        {#if projectStructure.files && projectStructure.files.length > 0}
          {#each projectStructure.files as file}
            <File
              name={file.name}
              id={file.id}
              isNested={false}
              parentFolderId={file.parentFolderId}
              on:itemDropped={handleDropOnRootFolder}
            />
          {/each}
        {/if}
      {/if}
    </ul>
  </div>
</div>

<style>
  .text-center {
    text-align: center;
  }
  #file-navigator {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 73%;
    background-color: var(--white);
    border-bottom: 1px solid var(--silver);
  }
  #project-toolbar {
    position: relative;
    display: flex;
    justify-content: flex-end;
    height: 40px;
    border-bottom: 1px solid var(--silver);
    z-index: 2;
    background-color: var(--white);
  }
  #project-name {
    position: absolute;
    left: 4px;
    background-color: transparent;
    border: none;
    font-family: Montserrat, sans-serif;
    width: 60%;
    height: 100%;
    font-size: 16px;
  }
  .project-toolbar-btn {
    display: flex;
    justify-content: center;
    align-items: center;
    background: transparent;
    border: none;
  }
  .project-toolbar-btn:hover {
    cursor: pointer;
  }
  #file-system-container {
    height: 100%;
    width: 98.5%;
    display: flex;
    flex-direction: column;
    z-index: 1;
    background-color: transparent;
    border-bottom: none;
    overflow-y: auto;
    overflow-x: auto;
  }
  #file-selector {
    padding: 0;
    margin: 0;
    width: 100%;
    max-width: 100%;
    white-space: nowrap;
    list-style-type: none;
    border: none;
    margin-top: 5px;
    min-width: 200px;
    padding-bottom: 100px;
  }

  .drag-over {
    background-color: var(--blue-bell);
  }

  ::-webkit-scrollbar {
    width: 14px;
    height: 13px;
  }

  ::-webkit-scrollbar-track {
    box-shadow: inset 0 0 3px grey;
    border-radius: 10px;
  }

  ::-webkit-scrollbar-thumb {
    background: var(--grey85);
    border: 1px solid var(--grey56);
    border-radius: 10px;
  }
  ::-webkit-scrollbar-thumb:hover {
    background: var(--grey85);
  }
  ::-webkit-scrollbar-corner {
    background: transparent;
  }
  @media (max-width: 800px) {
    #project-name {
      width: 80px;
    }
  }
</style>
