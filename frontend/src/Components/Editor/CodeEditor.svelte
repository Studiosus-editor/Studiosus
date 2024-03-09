<script>
  import { onMount } from "svelte";
  import { writable } from "svelte/store";
  import FileNavigator from "./FileNavigator.svelte";
  import LineNumbers from "./LineNumbers.svelte";
  import CodeEditor from "./scripts/CodeEditor.js";
  import FileManager from "./scripts/FileManager.js";

  let fileManager = new FileManager();
  let codeEditor;

  // Create a Svelte store to hold the textarea value
  let textareaValue = writable("");

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
  });
</script>

<div id="editor">
  <FileNavigator {codeEditor} {fileManager} {textareaValue} />
  <div id="editor-content">
    <LineNumbers {textareaValue} />
    <div class="textarea-container">
      <textarea
        id="editor-field"
        cols="75"
        rows="30"
        placeholder="Start typing here..."
        spellcheck="false"
        bind:value={$textareaValue}
      ></textarea>
      <div id="overlay" class="hidden">Drop your file here...</div>
    </div>
  </div>
</div>

<style>
  .textarea-container {
    width: 100%;
    position: relative;
  }

  /* Container containing line-numbering, textarea, list of files */
  #editor {
    margin-top: 20px;
    font-family: monospace;
    display: flex;
    flex-direction: column;
    max-height: 400px;
    max-width: 780px;
    line-height: 21px;
  }

  /* Textarea field responsible for typing the text */
  #editor-content {
    display: flex;
    flex-direction: row;
    overflow-y: auto;
  }

  #editor-field {
    line-height: 21px;
    font-size: 1rem;
    padding-left: 5px;
    border: 0;
    background: #f1f1f1;
    outline: none;
    resize: none;
    white-space: pre-wrap;
    overflow-wrap: break-word;
  }

  #editor-field.dragover {
    background: #eaeaea;
    filter: blur(3px);
  }

  #editor-field::placeholder {
    color: #000;
    opacity: 0.5;
  }

  /* Container that appears when a file is dragged over the textarea. */
  #overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.2);
    color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 32px;
    pointer-events: none;
  }
  #overlay.hidden {
    display: none;
  }
</style>
