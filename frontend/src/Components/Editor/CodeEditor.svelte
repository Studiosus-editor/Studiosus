<script>
  import { onMount } from "svelte";
  import { writable } from "svelte/store";
  import Problems from "./Problems.svelte";
  import Dashboard from "./Dashboard.svelte";
  import FileNavigator from "./FileNavigator.svelte";
  import LineNumbers from "./LineNumbers.svelte";
  import CodeEditor from "./scripts/CodeEditor.js";
  import FileManager from "./scripts/FileManager.js";
  import YamlChecker from "./scripts/YamlChecker.js";
  import GeneralToolbar from "./GeneralToolbar.svelte";
  import NavController from "./NavigatorController.svelte";
  import { errorStore } from "./scripts/store.js";
  import { projectExpanded } from "./scripts/store.js";
  import { editorWrapperHeightStore } from "./scripts/store.js";
  import interact from "interactjs";
  let fileManager = new FileManager();
  let codeEditor;
  let yamlChecker = new YamlChecker();
  let textareaValue = writable("");
  let textarea;
  let defaultWidth;

  let editorWrapperHeight;

  editorWrapperHeightStore.subscribe((value) => {
    editorWrapperHeight = value;
  });

  let projectExpandedValue;
  projectExpanded.subscribe((value) => {
    projectExpandedValue = value;
  });

  const handleInput = () => {
    textarea.style.height = "auto";
    textarea.style.height = textarea.scrollHeight + "px";

    // Store the new height in localStorage
    localStorage.setItem("textareaHeight", textarea.scrollHeight);
  };

  const adjustTextareaWidth = () => {
    const lines = textarea.value.split("\n");
    let longestLine = lines[0];

    // If textarea is empty, reset to original width
    if (textarea.value === "") {
      textarea.style.width = `${defaultWidth}px`;
      return;
    }

    // Find the line with the most characters
    for (let line of lines) {
      if (line.length > longestLine.length) {
        longestLine = line;
      }
    }

    // Calculate the width of the longest line
    const newWidth = longestLine.length * 9; // 9px per character

    // Set the width of the textarea to match the width of the longest line
    // only if the new width exceeds the default width
    if (newWidth > defaultWidth) {
      textarea.style.width = `${newWidth}px`;
      //saves the width into local storage
      localStorage.setItem("textareaWidth", textarea.scrollWidth);
    } else {
      textarea.style.width = `${defaultWidth}px`;
    }
  };
  onMount(() => {
    codeEditor = new CodeEditor(
      "editor",
      "editor-field",
      "line-numbers",
      "overlay",
      fileManager
    );
    textareaValue.set(codeEditor.textarea.value);
    const editorWrapperElement = document.querySelector("#editor-wrapper");
    editorWrapperHeightStore.set(editorWrapperElement.offsetHeight);

    // Trigger an update to textareaValue

    defaultWidth = textarea.offsetWidth;
    adjustTextareaWidth(); // Adjust the width on mount

    textarea.addEventListener("input", adjustTextareaWidth); // Adjust the width on input
    textarea.addEventListener("input", handleInput); // Adjust the height on input
    textarea.addEventListener("input", () => {
      yamlChecker.yamlCode = textarea.value;
      let error = yamlChecker.validateYAML();
      if (error) {
        errorStore.set(error);
      }
    });
  });

  function handleFormat() {
    yamlChecker.yamlCode = textarea.value;
    textarea.value = yamlChecker.formatYAML();
  }
  function handleSaveLocal() {
    fileManager.func_savedata(textarea.value);
  }

  //interact js for resizing the navigator-dashboard
  interact("#navigator-dashboard")
    .resizable({
      edges: { top: false, left: false, bottom: false, right: true },
      modifiers: [
        interact.modifiers.restrictSize({
          min: { width: 100, height: Infinity },
          max: { width: 600, height: Infinity },
        }),
      ],
    })
    .on("resizemove", function (event) {
      var target = event.target;
      var flexColumn = document.querySelector(".flex-column2");

      var newWidth = event.rect.width;
      var totalWidth = target.parentNode.offsetWidth;

      target.style.width = newWidth + "px";
      flexColumn.style.width = totalWidth - newWidth + "px";
    })
    .on("resizestart", function (event) {
      var target = event.target;
      target.style.borderRight = "2px solid blue";

      var editorWrapper = document.querySelector("#editor-wrapper");
      if (editorWrapper) {
        editorWrapper.style.pointerEvents = "none";
      }
    })
    .on("resizeend", function (event) {
      var target = event.target;
      target.style.borderRight = "";

      var editorWrapper = document.querySelector("#editor-wrapper");
      if (editorWrapper) {
        editorWrapper.style.pointerEvents = "auto";
      }
    });
</script>

<div class="main-component">
  <div id="editor">
    <GeneralToolbar on:format={handleFormat} on:saveLocal={handleSaveLocal} />
    <div class="flex-row">
      <NavController />
      {#if $projectExpanded}
        <div id="navigator-dashboard">
          <FileNavigator {codeEditor} {fileManager} {textareaValue} />
          <Dashboard />
        </div>
      {/if}
      <div class="flex-column2">
        <div id="editor-wrapper">
          <LineNumbers {textareaValue} />
          <div class="textarea-container">
            <textarea
              id="editor-field"
              cols="75"
              rows="30"
              placeholder="Start typing here..."
              spellcheck="false"
              bind:value={$textareaValue}
              bind:this={textarea}
              on:input={handleInput}
              style="overflow: hidden; height: auto;"
            ></textarea>
            <div id="overlay" class="hidden">Drop your file here...</div>
          </div>
        </div>
        <Problems />
      </div>
    </div>
  </div>
</div>

<style>
  .main-component {
    margin-top: 200px;
    margin-bottom: 200px;
    display: block;
    position: relative;
    width: 100%;
    height: 799px;
    overflow: hidden;
    background-color: var(--white);
    border: 1px solid var(--silver);
  }
  #editor {
    display: block;
    position: relative;
    flex: 0 0 auto;
    width: 100%;
    height: 100%;
    overflow: hidden;
    background-color: var(--white);
    border: 1px solid var(--silver);
  }
  .flex-row {
    display: flex;
    flex-direction: row;
    height: 96%;
    width: 100%;
  }
  #navigator-dashboard {
    display: flex;
    flex-direction: column;
    min-width: 175px;
    max-width: 600px;
    width: 24%;
    height: 100%;
    border-right: 1px solid var(--silver);
  }
  .flex-column2 {
    display: flex;
    width: 100%;
    min-width: 100px;
    max-width: 100%;
    flex: 1;
    flex-direction: column;
    height: 100%;
  }
  #editor-wrapper {
    background-color: var(--whisper);
    display: flex;
    overflow-y: scroll;
    flex: 1; /* this will make it take up the remaining space */
    width: 100%;
    margin-top: 38px;
    border: 1px solid var(--silver);
  }

  #editor-field {
    flex: 0 0 auto;
    width: 600px;
    line-height: 23px;
    font-size: 1rem;
    padding-left: 5px;
    font-size: 18px;
    overflow-x: scroll;
    white-space: nowrap;
    background: transparent;
    border: none;
    outline: none;
    resize: none;
    font-family: var(--ubuntu-mono);
  }

  #editor-wrapper.dragover {
    background: #eaeaea;
    filter: blur(3px);
  }
  #editor-field::placeholder {
    color: #000;
    opacity: 0.5;
  }

  /*Container that appears when a file is dragged over the textarea. */
  #overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: transparent;
    color: #222222;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 32px;
    pointer-events: none;
  }
  #overlay.hidden {
    display: none;
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
  @media (min-width: 1100px) {
    .main-component {
      width: 1000px;
    }
  }
</style>
