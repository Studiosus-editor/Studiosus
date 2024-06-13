<script>
  import { onMount, onDestroy, createEventDispatcher } from "svelte";
  import DocumentIcon from "../../../assets/svg/document-icon.svg";
  import ArrowDown from "../../../assets/svg/down-arrow-icon-black.svg";
  import {
    showCreateFileStore,
    showCreateFolderStore,
    createFileStore,
    createFolderStore,
    showRenameStore,
  } from "../scripts/store.js";
  import { addToast } from "../../Modal/ToastNotification/toastStore.js";
  import { _ } from "svelte-i18n";

  export let isNested = false;
  export let isForFile;
  export let isCreate;
  export let currentName = "";
  export let currentContainerFileNames = [];
  export let currentContainerFolderNames = [];

  const dispatch = createEventDispatcher();
  const showStore = isForFile ? showCreateFileStore : showCreateFolderStore;
  const createStore = isForFile ? createFileStore : createFolderStore;
  const currentForbiddenNames = isForFile
    ? currentContainerFileNames
    : currentContainerFolderNames;

  let inputElement;

  onMount(() => {
    document.addEventListener("click", handleOutsideClick, true);
    inputElement.focus();
  });

  function handleOutsideClick(event) {
    if (!inputElement.contains(event.target)) {
      isCreate ? handleNameCreate() : handleRename();
    }
  }

  function handleKeydown(event) {
    if (event.key === "Enter") {
      isCreate ? handleNameCreate() : handleRename();
    }
  }

  function handleNameCreate() {
    let name = inputElement.value.trim();
    if (name !== "") {
      if (!currentForbiddenNames.includes(name)) {
        createStore.set(name);
      } else {
        addToast({
          message: isForFile
            ? $_("fileSystemToastNotifications.errorFileAlreadyExists")
            : $_("fileSystemToastNotifications.errorFolderAlreadyExists"),
          type: "error",
        });
      }
    }
    showStore.set(false);
  }

  function handleRename() {
    let name = inputElement.value.trim();
    if (name !== "") {
      if (!currentForbiddenNames.includes(name)) {
        dispatch("rename", { newName: name });
      } else {
        addToast({
          message: isForFile
            ? $_("fileSystemToastNotifications.errorFileAlreadyExists")
            : $_("fileSystemToastNotifications.errorFolderAlreadyExists"),
          type: "error",
        });
      }
    }
    showRenameStore.set(null);
  }

  onDestroy(() => {
    document.removeEventListener("click", handleOutsideClick, true);
  });
</script>

<li class:root-style={isNested}>
  <img
    src={isForFile ? DocumentIcon : ArrowDown}
    alt="..."
    class={isForFile ? "file-img" : "folder-img"}
  />
  <input
    bind:this={inputElement}
    type="text"
    on:keydown|stopPropagation={handleKeydown}
    placeholder={currentName
      ? currentName
      : isForFile
        ? $_("createOrRenameFileOrFolder.yamlFile")
        : $_("createOrRenameFileOrFolder.folderName")}
  />
</li>

<style lang="scss">
  li {
    display: flex;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    align-items: center;
    height: 30px;
    background-color: var(--jordy-blue);

    & .file-img {
      width: 20px;
      height: 20px;
      margin-right: 5px;
      padding-left: 15px;
    }

    & .folder-img {
      width: 30px;
      height: 30px;
      transform: rotate(-90deg);
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
        background-color: #000;
      }
    }
    input {
      font-family: "Montserrat", sans-serif;
      font-weight: 500;
      font-size: 18px;
      width: 100%;
      border-right: none;
      border-color: var(--grey43);
      border-width: 2px;
      outline: none;
      background-color: var(--white);
      flex-grow: 1;
    }
  }
</style>
