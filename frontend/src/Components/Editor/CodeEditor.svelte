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

<div class="main-component">
  <h1>Studiosus</h1>
  <p>Universal Ansible Editor</p>
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
</div>

<style>
  .main-component {
    transition: all 0.3s ease;
    position: relative;
    padding: 15px;
    border-radius: 5%;
    border: 3px dashed #000;
  }

  .main-component:hover {
    transform: translateY(-2px) scale(1.01);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .main-component p {
    margin-top: 5px;
    max-width: max-content;
    white-space: nowrap;
    overflow: hidden;
    border-right: 0.15em solid #000; /* The typwriter cursor */
    /* add typing & typewriter cursor effect */
    animation:
      typing 2s steps(40, end) forwards,
      blink-caret 0.75s step-end 2s infinite;
  }

  /* The typing effect */
  @keyframes typing {
    from {
      width: 0;
    }
    to {
      width: 100%;
    }
  }

  /* The typewriter cursor effect */
  @keyframes blink-caret {
    from,
    to {
      border-color: transparent;
    }
    50% {
      border-color: #000;
    }
  }

  /* On smaller devices, set main component width to 90% */
  @media (max-width: 900px) {
    .main-component {
      width: 90%;
    }
  }

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
