<script>
  import {
    currentHighlightedItemStore,
    currentFolderStore,
    showCreateFileStore,
    showCreateFolderStore,
    currentContextMenuStore,
    deleteFolderStore,
    renameFolderStore,
    showRenameStore,
    currentlyDraggedItemId,
    itemIsDraggedOver_ParentIdStore,
    forbbidenDragIdArrayStore,
    moveToItemIdStore,
    currentlyDraggedItemNameStore,
    deleteFolderFileIdStore,
    deleteFolderFolderIdStore,
    isViewerOrTemplateStore,
    currentlyOpenedFolderStore,
  } from "./../scripts/store.js";
  import File from "./File.svelte";
  import FolderList from "./FolderList.svelte";
  import ArrowDown from "../../../assets/svg/down-arrow-icon-black.svg";
  import CreateOrRenameFileOrFolder from "./CreateOrRenameFileOrFolder.svelte";
  import ContextMenu from "./ContextMenu.svelte";
  import { addToast } from "../../Modal/ToastNotification/toastStore.js";
  import { _ } from "svelte-i18n";

  export let folderName;
  export let folder;
  export let id;
  export let isNested = false;
  export let parentFolderId;

  let isFolderActive = false;
  let isHovered = false;
  let isBeingRenamed = false;
  let isBeingDraggedOver = false;

  let dragOverTimer;
  let timerActive = false;

  let contextMenuVisible = false;
  let contextMenuPosition = { x: 0, y: 0 };

  $: storeFolderId = "folder-" + id; // used for stores where files and folder ids need to be differenciated

  $: isFolderActive = $currentlyOpenedFolderStore.includes(storeFolderId);

  // initalizes creation input for folder
  $: showFolderCreateOption =
    $showCreateFolderStore === true && $currentFolderStore === id;

  // initalizes creation input for file
  $: showFileCreateOption =
    $showCreateFileStore === true && $currentFolderStore === id;

  // moves highlight from folder to input creation and sets in back after
  $: isActive =
    $currentHighlightedItemStore === storeFolderId &&
    showFileCreateOption === false &&
    showFolderCreateOption === false;

  // opens the folder when creation input is shown and keeps it open after creation
  $: isFolderActive = showFolderCreateOption ? true : isFolderActive;
  $: isFolderActive = showFileCreateOption ? true : isFolderActive;

  // responsbile for context menu visibility
  $: contextMenuVisible = $currentContextMenuStore === storeFolderId;

  // responsible for showing rename input
  $: isBeingRenamed = $showRenameStore === storeFolderId;

  // keep hover effect if context menu is visible
  $: isHovered = contextMenuVisible;

  // shows drop area (hover effect) if an item is dragged over one of the files of folder
  $: isBeingDraggedOver =
    $itemIsDraggedOver_ParentIdStore === id &&
    !$forbbidenDragIdArrayStore.includes(id);

  function handleFolderClick() {
    isFolderActive = !isFolderActive;
    if (isFolderActive) {
      currentlyOpenedFolderStore.update((store) => {
        if (!store.includes(storeFolderId)) {
          store.push(storeFolderId);
        }
        return store;
      });
    } else {
      currentlyOpenedFolderStore.update((store) =>
        store.filter((id) => id !== storeFolderId)
      );
    }
    currentHighlightedItemStore.set(storeFolderId);
    currentFolderStore.set(id);
  }

  function deleteFolder() {
    let promptDeletion = false;
    let nestedFolderIds = [];
    if (folder.childFolders.length > 0 || folder.files.length > 0) {
      promptDeletion = true;

      let nestedFileIds = [];
      nestedFileIds = collectItemIds(folder.childFolders, "files");
      deleteFolderFileIdStore.set(nestedFileIds);

      nestedFolderIds = collectItemIds(folder.childFolders, "folders");
    }
    nestedFolderIds.push(id);
    deleteFolderFolderIdStore.set(nestedFolderIds);

    deleteFolderStore.set([id, promptDeletion, folderName]);
    if (currentlyOpenedFolderStore.includes(storeFolderId)) {
      currentlyOpenedFolderStore.update((store) => {
        return store.filter((id) => id !== storeFolderId);
      });
    }
  }

  function initalizeRename() {
    showRenameStore.set(storeFolderId);
  }

  function renameFolder(event) {
    const { detail } = event;
    renameFolderStore.set([id, detail.newName]);
    showRenameStore.set(null);
  }

  function handleRightClick(event) {
    if ($isViewerOrTemplateStore) return;
    event.preventDefault();
    event.stopPropagation();
    contextMenuPosition = { x: event.clientX, y: event.clientY };
    currentContextMenuStore.set(storeFolderId);
  }

  function closeContextMenu() {
    if ($currentContextMenuStore === storeFolderId) {
      currentContextMenuStore.set(null);
    }
  }

  // initalizes drag over by setting array of ids where drag/drop is forbidden
  // then disables hover effect globally by setting currently dragged item id (also used for API calls),
  // also initalizes the name of the dragged item to check for duplicate names and closes the context menu
  function handleDragStart() {
    if ($isViewerOrTemplateStore) return;
    currentContextMenuStore.set(null);
    let forbiddenDragIds = [];

    forbiddenDragIds = collectItemIds(folder.childFolders, "folders");
    forbiddenDragIds.push(parentFolderId);
    forbiddenDragIds.push(id);
    forbbidenDragIdArrayStore.set(forbiddenDragIds);

    currentlyDraggedItemNameStore.set(folderName);
    currentlyDraggedItemId.set(storeFolderId);
  }

  function collectItemIds(folders, type) {
    let folderIds = [];
    let fileIds = [];

    if (type === "files") {
      fileIds = folder.files.map((file) => file.id);
    }

    function traverseFolders(folders) {
      for (const folder of folders) {
        if (type === "folders") {
          folderIds.push(folder.id);
        }

        if (folder.files && folder.files.length > 0) {
          if (type === "files") {
            fileIds.push(...folder.files.map((file) => file.id));
          }
        }
        if (folder.childFolders && folder.childFolders.length > 0) {
          traverseFolders(folder.childFolders);
        }
      }
    }

    traverseFolders(folders);

    return type === "folders" ? folderIds : fileIds;
  }

  // highlights current dragged over folder as well as opens it after 0.5s
  function handleDragOver(event) {
    if ($isViewerOrTemplateStore) return;
    event.preventDefault();
    event.stopPropagation(); // prevents parent folder from being highlighted
    if (!$forbbidenDragIdArrayStore.includes(id) && $currentlyDraggedItemId) {
      isBeingDraggedOver = true;
      if (!timerActive) {
        timerActive = true;
        dragOverTimer = setTimeout(() => {
          isFolderActive = true;
          timerActive = false;
        }, 500);
      }
    }
  }

  function handleDragLeave() {
    if ($isViewerOrTemplateStore) return;
    clearTimeout(dragOverTimer);
    timerActive = false;
    isBeingDraggedOver = false;
  }

  // calls move API
  function moveItemToFolder(event) {
    if ($isViewerOrTemplateStore) return;
    event.stopPropagation();
    isBeingDraggedOver = false;
    if (!isNameForbidden()) {
      if (!$forbbidenDragIdArrayStore.includes(id) && $currentlyDraggedItemId) {
        moveToItemIdStore.set(id);
      } else {
        currentlyDraggedItemId.set(null);
      }
    } else {
      currentlyDraggedItemId.set(null);
    }
  }

  function isNameForbidden() {
    if (!$currentlyDraggedItemId) return false;
    let parts = $currentlyDraggedItemId.split("-");
    let type = parts[0];
    if (type === "folder") {
      const forbiddenFolderNames = folder.childFolders.map(
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
      const forbiddenFileNames = folder.files.map((file) => file.name);
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
</script>

<li
  class="folder"
  class:root-style={isNested}
  class:hovered={isHovered}
  class:drag-over={isBeingDraggedOver}
>
  {#if isBeingRenamed}
    <CreateOrRenameFileOrFolder
      isForFile={false}
      isCreate={false}
      on:rename={renameFolder}
      currentName={folderName}
      currentContainerFolderNames={folder.childFolders.map(
        (folder) => folder.name
      )}
    />
  {:else}
    <div
      class="folder__name-container"
      class:active={isActive}
      on:click={handleFolderClick}
      on:keydown={handleFolderClick}
      on:contextmenu={handleRightClick}
      on:mouseenter={() => {
        if ($currentContextMenuStore || $currentlyDraggedItemId) {
          return;
        } else {
          isHovered = true;
        }
      }}
      on:mouseleave={() => {
        if (!contextMenuVisible) {
          isHovered = false;
        }
      }}
      on:dragover={handleDragOver}
      on:dragleave={handleDragLeave}
      on:drop={moveItemToFolder}
      on:dragstart={handleDragStart}
      draggable={!$isViewerOrTemplateStore}
    >
      <img
        src={ArrowDown}
        class="folder__expand-icon"
        alt={$_("folder.altExpandFolder")}
        class:active={isFolderActive}
      />
      <h4>{folderName}</h4>
    </div>
  {/if}
  <ul
    class={isFolderActive ? "active" : "hidden"}
    class:remove-parent-hover={isHovered}
  >
    {#if showFolderCreateOption}
      <CreateOrRenameFileOrFolder
        isNested={true}
        isForFile={false}
        isCreate={true}
        currentContainerFolderNames={folder.childFolders.map(
          (folder) => folder.name
        )}
      />
    {/if}
    <FolderList childFolders={folder.childFolders} />
    {#if showFileCreateOption}
      <CreateOrRenameFileOrFolder
        isNested={true}
        isForFile={true}
        isCreate={true}
        currentContainerFileNames={folder.files.map((file) => file.name)}
      />
    {/if}
    {#each folder.files as file}
      <File
        name={file.name}
        id={file.id}
        isNested={true}
        parentFolderId={file.parentFolderId}
        on:itemDropped={moveItemToFolder}
      />
    {/each}
  </ul>
  <ContextMenu
    x={contextMenuPosition.x}
    y={contextMenuPosition.y}
    visible={contextMenuVisible}
    close={closeContextMenu}
    on:delete={deleteFolder}
    on:rename={initalizeRename}
  />
</li>

<style lang="scss">
  .folder {
    padding: 0;
    display: flex;
    flex-direction: column;

    &.drag-over {
      background-color: var(--blue-bell);
    }

    &.hovered {
      background-color: var(--pale-navy);
      cursor: pointer;
    }

    &__name-container {
      display: flex;
      align-items: center;

      &.active {
        padding: 2px 0;
        background-color: var(--jordy-blue);
        transition:
          background-color 0.3s ease,
          color 0.3s ease;
        border: none;
      }
    }

    &.root-style {
      position: relative;

      &:before {
        content: "";
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 1px;
        background-color: var(--jordy-blue);
      }
    }

    &__expand-icon {
      width: 30px;
      height: 30px;
      transform: rotate(-90deg);
      transition: transform 0.3s ease;

      &.active {
        transform: rotate(0deg);
      }
    }
  }

  h4 {
    margin: 0;
    font-size: 16px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  ul {
    list-style: none;
    padding-left: 14px;

    &.remove-parent-hover {
      background-color: white;
    }

    &.active {
      display: block;
    }

    &.hidden {
      display: none;
    }
  }
</style>
