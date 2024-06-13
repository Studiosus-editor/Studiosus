<script>
  import { onMount } from "svelte";
  import { createEventDispatcher } from "svelte";
  import {
    currentFileStore,
    currentFolderStore,
    currentHighlightedItemStore,
    currentContextMenuStore,
    deleteFileStore,
    renameFileStore,
    showRenameStore,
    currentlyDraggedItemId,
    itemIsDraggedOver_ParentIdStore,
    forbbidenDragIdArrayStore,
    currentlyDraggedItemNameStore,
    activeProjectFilesStores,
    currentFileNameStore,
    isViewerOrTemplateStore,
  } from "./../scripts/store.js";
  import DocumentIcon from "../../../assets/svg/document-icon.svg";
  import AnsibleIcon from "../../../assets/svg/ansible-icon.svg";
  import GearIcon from "../../../assets/svg/gear-icon.svg";
  import ContextMenu from "./ContextMenu.svelte";
  import RenameFile from "./CreateOrRenameFileOrFolder.svelte";

  export let name;
  export let id;
  export let isNested = false;
  export let parentFolderId;

  const dispatch = createEventDispatcher();

  let isBeingRenamed = false;
  let isHovered = false;

  let contextMenuVisible = false;
  let contextMenuPosition = { x: 0, y: 0 };

  let isActiveProjectFile = false;

  // used check whenever a file selection changes to update the current file name displayed at the top of code editor
  $: if ($currentFileStore === id) {
    currentFileNameStore.set(name);
  }

  $: storeFileId = "file-" + id; // used for stores where files and folder ids need to be differenciated

  $: isActive = $currentHighlightedItemStore === storeFileId;

  // responsbile for context menu visibility
  $: contextMenuVisible = $currentContextMenuStore === storeFileId;

  // responsible for showing rename input
  $: isBeingRenamed = $showRenameStore === storeFileId;

  // keep hover effect if context menu is visible
  $: isHovered = contextMenuVisible;

  function selectFile(event) {
    event.stopPropagation();
    currentFileNameStore.set(name);
    currentFileStore.set(id);
    currentFolderStore.set(parentFolderId);
    currentHighlightedItemStore.set(storeFileId);
  }

  function deleteFile() {
    deleteFileStore.set(id);
  }

  function initalizeRename() {
    showRenameStore.set(storeFileId);
  }

  function renameFile(event) {
    const { detail } = event;
    renameFileStore.set([id, detail.newName]);
    showRenameStore.set(null);
  }

  function handleRightClick(event) {
    if ($isViewerOrTemplateStore) return;
    event.preventDefault();
    event.stopPropagation();
    contextMenuPosition = { x: event.clientX, y: event.clientY };
    currentContextMenuStore.set(storeFileId);
  }

  function closeContextMenu() {
    if ($currentContextMenuStore === storeFileId) {
      currentContextMenuStore.set(null);
    }
  }
  // initializes drag over by getting current file id and its name(used for checking if duplicate names exist)
  // & disables hover effect globally also sets parent folder id as forbbiden to drag to,
  // also closes the context menu
  function handleDragStart() {
    if ($isViewerOrTemplateStore) return;
    currentContextMenuStore.set(null);
    currentlyDraggedItemId.set(storeFileId);
    currentlyDraggedItemNameStore.set(name);
    forbbidenDragIdArrayStore.set([parentFolderId]);
  }

  // highlights parent folder on drag over
  function handleDragOver(event) {
    if ($isViewerOrTemplateStore) return;
    event.preventDefault();
    event.stopPropagation(); // to prevent parent folder from being highlighted
    if (
      parentFolderId != $itemIsDraggedOver_ParentIdStore &&
      $currentlyDraggedItemId
    ) {
      itemIsDraggedOver_ParentIdStore.set(parentFolderId);
    }
  }

  function handleDragLeave() {
    if ($isViewerOrTemplateStore) return;
    itemIsDraggedOver_ParentIdStore.set(null);
  }

  function handleDrop(event) {
    if ($isViewerOrTemplateStore) return;
    event.preventDefault();
    event.stopPropagation(); // prevents dropping on root folder if dropped inside folder
    dispatch("itemDropped");
  }

  onMount(() => {
    activeProjectFilesStores.subscribe(() => {
      isActiveProjectFile = $activeProjectFilesStores.includes(id.toString());
    });
  });
</script>

{#if isBeingRenamed}
  <RenameFile
    isForFile={true}
    isCreate={false}
    {isNested}
    on:rename={renameFile}
    currentName={name}
  />
{:else}
  <li
    class:active={isActive}
    class:root-style={isNested}
    class:hovered={isHovered}
    on:contextmenu={handleRightClick}
    on:click={selectFile}
    on:keydown={selectFile}
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
    draggable={!$isViewerOrTemplateStore}
    on:dragstart={handleDragStart}
    on:dragover={handleDragOver}
    on:dragleave={handleDragLeave}
    on:drop={handleDrop}
  >
    <span class:online={isActiveProjectFile}></span>
    {#if name.endsWith(".yml") || name.endsWith(".yaml")}
      <img src={AnsibleIcon} alt="Ansible File icon" />
    {:else if name.endsWith(".ini")}
      <img
        src={GearIcon}
        alt="Config File Icon"
        style="transform: scale(1.5)"
      />
    {:else}
      <img src={DocumentIcon} alt="Document File icon" />
    {/if}
    <h4>{name}</h4>
    <ContextMenu
      x={contextMenuPosition.x}
      y={contextMenuPosition.y}
      visible={contextMenuVisible}
      close={closeContextMenu}
      on:delete={deleteFile}
      on:rename={initalizeRename}
    />
  </li>
{/if}

<style lang="scss">
  li {
    display: flex;
    position: relative;
    padding: 6px 15px 6px 7px;
    transition:
      background-color 0.3s ease,
      color 0.3s ease;
    align-items: center;

    & h4 {
      margin: 0;
      font-size: 15px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    & img {
      width: 20px;
      height: 20px;
      margin-right: 5px;
    }

    &.hovered {
      background-color: var(--pale-navy);
      cursor: pointer;
    }
    &.active {
      background-color: var(--jordy-blue);
      transition:
        background-color 0.3s ease,
        color 0.3s ease;
      border: none;
    }

    @keyframes bubbleFirst {
      0% {
        transform: translateY(-50%) scale(0);
      }
      100% {
        transform: translateY(-50%) scale(1);
      }
    }

    @keyframes bubble {
      0% {
        transform: translateY(-50%) scale(1);
      }
      25% {
        transform: translateY(-50%) scale(1.1);
      }
      50% {
        transform: translateY(-50%) scale(1.2);
      }
      75% {
        transform: translateY(-50%) scale(1.1);
      }
      100% {
        transform: translateY(-50%) scale(1);
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
        background-color: var(--silver-dark);
      }
    }
    & span.online {
      content: "";
      position: relative;
      top: 5px;
      right: 0;
      width: 10px;
      height: 10px;
      border-radius: 50%;
      border: 1px solid var(--black);
      background-color: var(--medium-sea-green);
      animation:
        bubbleFirst 0.5s ease-out forwards,
        bubble 2s infinite 0.5s;
      flex-shrink: 0;
    }
  }
</style>
