<script>
  import { writable } from "svelte/store";
  import FileNavigator from "./scripts/FileNavigator.js";
  import AddFileIcon from "../../assets/svg/add-file-icon.svg";

  export let codeEditor;
  export let fileManager;
  export let textareaValue; // Svelte store to hold the textarea value
  let files = [];
  let currentFile;
  let fileNavigator = writable(null);

  $: if (codeEditor && fileManager) {
    fileNavigator.set(new FileNavigator(fileManager, codeEditor));

    // Update the files variable whenever the file list changes
    files = fileManager.getAllFiles();

    // Update the current file whenever the code editor's current file changes
    currentFile = fileManager.getCurrentFile();
  }
  function selectFile(file) {
    // Update the current file
    currentFile = file;

    // Load the file in the code editor
    codeEditor.loadFile(file);

    // Update the textarea UI
    textareaValue.set(codeEditor.textarea.value);
  }

  function createNewFile() {
    fileNavigator.update((navigator) => {
      navigator.createNewPage();
      return navigator;
    });
    files = fileManager.getAllFiles();
  }

  function deleteFile(file) {
    fileNavigator.update((navigator) => {
      navigator.deleteFile(file);
      return navigator;
    });
    files = fileManager.getAllFiles();
  }
</script>

<div id="file-navigator">
  <div id="file-selector">
    <ul>
      {#each files as file (file)}
        <li
          class={file === currentFile ? "active" : ""}
          on:click={() => selectFile(file)}
          on:keydown={(event) => event.key === "Enter" && selectFile(file)}
        >
          {file}
          <button on:click={() => deleteFile(file)}>X</button>
        </li>
      {/each}
    </ul>
  </div>
  <button id="create-new-file" on:click={createNewFile}>
    <img src={AddFileIcon} alt="Create new file" />
  </button>
</div>

<style>
  #file-navigator {
    display: flex;
    flex-direction: row;
    width: 100%;
    margin-bottom: 5px;
    gap: 1%;
  }

  #file-selector {
    width: 92%;
  }

  #file-selector ul {
    max-width: 100%;
    display: flex;
    flex-direction: row;
    list-style-type: none;
    padding-bottom: 10px;
    overflow-x: auto; /* Enable horizontal scrolling */
    white-space: nowrap; /* Prevent line breaks */
  }

  #file-selector li {
    min-width: 150px;
    font-size: 1rem;
    position: relative;
    text-align: center;
    flex-grow: 1;
    padding: 10px 30px 10px 15px;
    border-right: 1px solid #ccc;
    background-color: #fff; /* white background for non-active tabs */
    color: #2b2b2b; /* black text for non-active tabs */
    transition:
      background-color 0.3s ease,
      color 0.3s ease;
    overflow: hidden; /* Ensure that the content is clipped */
    text-overflow: ellipsis; /* Use an ellipsis to indicate overflow */
    white-space: nowrap; /* Prevent text from wrapping onto the next line */
  }

  #file-selector li:hover {
    background-color: #e0e0e0;
    cursor: pointer;
  }

  #file-selector li.active {
    background-color: #2b2b2b; /* black background for active tab */
    color: #fff; /* white text for active tab */
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

  #create-new-file {
    border: none; /* Remove default border */
    border-left: 1px solid #2b2b2b;
    background: none;
    width: 7%;
    margin-bottom: 10px;
  }

  #create-new-file:hover {
    transform: scale(1.05);
    transition: transform 0.3s ease;
    cursor: pointer;
  }

  #create-new-file img {
    padding: 0;
    margin: 0;
    width: 80%;
  }
</style>
