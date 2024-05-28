<script>
  import { writable } from "svelte/store";
  import FileNavigator from "./scripts/FileNavigator.js";
  import AddFileIcon from "../../assets/svg/add-file-icon.svg";
  import AddFolderIcon from "../../assets/svg/add-folder-icon.svg";
  import { _ } from "svelte-i18n";
  import { text } from "svelte/internal";
  import { tick, createEventDispatcher } from "svelte";

  export let codeEditor;
  export let fileManager;
  export let textareaValue; // Svelte store to hold the textarea value

  let files = [];
  let currentFile = writable(null);
  let fileNavigator;
  const MAX_LENGTH_PROJECT_NAME = 22;
  const dispatch = createEventDispatcher();

  let unsubscribe = () => {};

  $: if (codeEditor && fileManager) {
    fileNavigator = new FileNavigator(fileManager, codeEditor, currentFile);

    // Update the files variable whenever the file list changes
    files = fileManager.getAllFiles();

    // Update the current file whenever the code editor's current file changes
    tick().then(() => {
      const savedCurrentFile = localStorage.getItem("currentFile");
      if (savedCurrentFile) {
        currentFile.set(savedCurrentFile);
      }
    });

    unsubscribe();
    unsubscribe = fileNavigator.currentPageName.subscribe((value) => {
      currentFile.set(value);
    });
  }
  function selectFile(file) {
    codeEditor.loadFile(file);
    textareaValue.set(codeEditor.textarea.innerText);
    dispatch("fileInteraction");
    currentFile.set(file);
    fileManager.saveCurrentFile(file);
  }

  function createNewFile() {
    fileNavigator.createNewPage();
    files = fileManager.getAllFiles();
    textareaValue.set(codeEditor.textarea.innerText);
    dispatch("fileInteraction");
  }

  function deleteFile(file) {
    fileNavigator.deleteFile(file);
    files = fileManager.getAllFiles();
    textareaValue.set(codeEditor.textarea.innerText);
    dispatch("fileInteraction");
  }
</script>

<div id="nav-placeholder"></div>
<div id="file-navigator">
  <div id="project-toolbar">
    <input
      id="project-name"
      type="text"
      maxlength={MAX_LENGTH_PROJECT_NAME}
      placeholder={$_("editor.projectToolbar.nameProject")}
    />
    <button
      title={$_("editor.projectToolbar.createFile")}
      class="project-toolbar-btn"
      id="create-new-file"
      on:click={createNewFile}
    >
      <img src={AddFileIcon} alt="Create new file" width="22px" height="22px" />
    </button>

    <!-- on:click={createNewFolder}  -->
    <button
      title={$_("editor.projectToolbar.createFolder")}
      class="project-toolbar-btn"
      id="create-new-folder"
    >
      <img
        src={AddFolderIcon}
        alt="Create new folder"
        width="22px"
        height="22px"
        background="transparent"
      />
    </button>
  </div>
  <div class="flex-column">
    <div id="file-system-container">
      <div id="file-selector">
        <ul>
          {#each files as file (file)}
            <li
              class={$currentFile === file ? "active" : ""}
              on:click={() => selectFile(file)}
              on:keydown={(event) => event.key === "Enter" && selectFile(file)}
            >
              {file}
              <button on:click={() => deleteFile(file)}>X</button>
            </li>
          {/each}
        </ul>
      </div>
    </div>
  </div>
</div>

<style>
  #nav-placeholder {
    display: none;
    height: 100%;
    background: var(--grey85);
    width: 50px;
  }
  #file-navigator {
    display: flex;
    flex-direction: column;
    background: transparent;
    width: 100%;
    height: 73%;
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
    position: relative;
    background: transparent;
    border: none;
    cursor: pointer;
  }
  .flex-column {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  #file-selector {
    width: 100%;
    height: 100%;
  }
  #file-system-container {
    height: 100%;
    margin-top: 1px;
    overflow-y: auto;
    position: relative;
    display: flex;
    width: 100%;
    z-index: 1;
    background-color: transparent;
    border-bottom: 1px solid var(--silver);
  }
  #file-selector ul {
    max-width: 100%;
    display: flex;
    flex-direction: row;
    flex: 0 0 auto;
    list-style-type: none;
    overflow-x: auto;
    white-space: nowrap;
    border: none;
  }
  #file-selector ul {
    padding: 0;
    margin: 0;
    width: 100%;
    display: flex;
    font-family: Montserrat, sans-serif;
    flex-direction: column;
    overflow-x: auto; /* Enable horizontal scrolling */
    white-space: nowrap; /*Prevent line breaks*/
  }

  #file-selector li {
    height: 100%;
    font-size: 1rem;
    position: relative;
    text-align: center;
    padding: 10px 30px 10px 15px;
    border-right: 1px solid var(--silver);
    background-color: var(--white); /* white background for non-active tabs */
    color: var(--black); /* black text for non-active tabs */
    transition:
      background-color 0.3s ease,
      color 0.3s ease;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    border: none;
  }

  #file-selector li:hover {
    background-color: #afbeff;
    cursor: pointer;
  }

  #file-selector li.active {
    background-color: rgb(71, 112, 226);
    color: var(--white); /* white text for active tab */
    transition:
      background-color 0.3s ease,
      color 0.3s ease;
  }

  #file-selector li button {
    padding: 5px;
    font-size: 1.2rem;
    position: absolute;
    right: 5px;
    bottom: 5px;
    background: none;
    color: inherit;
    border: none;
  }

  /* Delete tab button*/
  #file-selector li button:hover {
    transform: scale(1.3);
    color: rgb(203, 35, 35);
    transition: transform 0.3s ease color 0.3s ease;
    cursor: pointer;
  }
  ::-webkit-scrollbar {
    width: 14px;
    height: 13px;
  }

  /* Track */

  ::-webkit-scrollbar-track {
    box-shadow: inset 0 0 3px grey;
    border-radius: 10px;
  }

  /* Handle */
  ::-webkit-scrollbar-thumb {
    background: var(--grey85);
    border: 1px solid var(--grey56);
    border-radius: 10px;
  }

  /* Handle on hover */
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
  @media (max-width: 600px) {
    #project-name {
      display: none;
    }
  }
</style>
