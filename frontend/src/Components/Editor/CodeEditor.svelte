<script>
  import { onMount } from "svelte";
  import { writable } from "svelte/store";
  import Problems from "./Problems.svelte";
  import Dashboard from "./Dashboard.svelte";
  import FileNavigator from "./FileNavigator.svelte";
  import LineNumbers from "./LineNumbers.svelte";
  import CodeEditor from "./scripts/CodeEditor.js";
  import FileManager from "./scripts/FileManager.js";
  import GeneralToolbar from "./GeneralToolbar.svelte";

  let fileManager = new FileManager();
  let codeEditor;

  // Create a Svelte store to hold the textarea value
  let textareaValue = writable("");

  let textarea;
  let defaultWidth;

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
  //A single onMount statement for clarification
  onMount(() => {
    codeEditor = new CodeEditor(
      "editor",
      "editor-field",
      "line-numbers",
      "overlay",
      fileManager
    );

    // Trigger an update to textareaValue
    textareaValue.set(codeEditor.textarea.value);

    defaultWidth = textarea.offsetWidth;
    adjustTextareaWidth(); // Adjust the width on mount

    textarea.addEventListener("input", adjustTextareaWidth); // Adjust the width on input
    textarea.addEventListener("input", handleInput); // Adjust the height on input
  });
</script>

<div class="main-component">
  <div id="editor">
    <GeneralToolbar />
    <div class="flex-row">
      <FileNavigator {codeEditor} {fileManager} {textareaValue} />
      <div class="editor-wrapper">
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
    </div>
    <div class="bottom-toolbars">
      <Dashboard />
      <Problems />
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
  }
  .editor-wrapper {
    background-color: var(--whisper);
    display: flex;
    overflow-y: scroll;
    height: 544px;
    width: 75%;
    margin-top: 31px;
    border: 1px solid var(--silver);
  }

  #editor-field {
    flex: 0 0 auto;
    width: 680px;
    line-height: 21px;
    font-size: 1rem;
    padding-left: 5px;
    font-size: 14px;
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
  .bottom-toolbars {
    display: flex;
    flex-direction: row;
    width: 100%;
    height: 189px;
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
