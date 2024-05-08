<script>
  import { onMount } from "svelte";
  import { writable } from "svelte/store";
  import Problems from "./Problems.svelte";
  import Dashboard from "./Dashboard.svelte";
  import FileNavigator from "./FileNavigator.svelte";
  import LineNumbers from "./LineNumbers.svelte";
  import CodeEditor from "./scripts/CodeEditor";
  import FileManager from "./scripts/FileManager.js";
  // import YamlChecker from "./scripts/YamlChecker.js";
  import GeneralToolbar from "./GeneralToolbar.svelte";
  import NavController from "./NavigatorController.svelte";
  import { projectExpanded } from "./scripts/store.js";
  import { editorWrapperHeightStore } from "./scripts/store.js";
  import { _ } from "svelte-i18n";
  import interact from "interactjs";
  import hljs from "highlight.js/lib/core";
  import yaml from "highlight.js/lib/languages/yaml";
  import "highlight.js/styles/atom-one-light.css";

  let fileManager = new FileManager();
  let codeEditor;
  let textareaValue = writable("");
  let textarea;
  let defaultWidth;

  let editorWrapperHeight;

  let editorField;

  editorWrapperHeightStore.subscribe((value) => {
    editorWrapperHeight = value;
  });

  let projectExpandedValue;
  projectExpanded.subscribe((value) => {
    projectExpandedValue = value;
  });

  const handleTextareaStyle = () => {
    textarea.style.height = "auto";
    textarea.style.height = textarea.scrollHeight + "px";
  };

  const adjustTextareaWidth = () => {
    const lines = textarea.innerText.split("\n");
    let longestLine = lines[0];

    // If textarea is empty, reset to original width
    if (textarea.innerText === "") {
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

  function setCaretPosition(index) {
    const setPoint = (node, index) => {
      const range = document.createRange();
      const sel = window.getSelection();
      range.setStart(node, index);
      range.collapse(true);
      sel.removeAllRanges();
      sel.addRange(range);
    };

    const walker = document.createTreeWalker(
      editorField,
      NodeFilter.SHOW_TEXT,
      null,
      false
    );
    let current = walker.nextNode();
    let length = 0;

    while (current) {
      if (length + current.textContent.length >= index) {
        setPoint(current, index - length);
        break;
      }
      length += current.textContent.length;
      current = walker.nextNode();
    }
  }

  function getCaretOffset() {
    const selection = window.getSelection();

    const range = selection.getRangeAt(0);
    // clone to avoid modifying users selection
    const preCaretRange = range.cloneRange();

    // in case editorField is not set do nothing
    try {
      preCaretRange.selectNodeContents(editorField);
    } catch (error) {
      return;
    }

    preCaretRange.setEnd(range.endContainer, range.endOffset);
    return preCaretRange.toString().length;
  }

  function highlightSyntaxOnUpdate() {
    let caretOffset = getCaretOffset();
    highlightSyntax();
    setCaretPosition(caretOffset);
  }

  function highlightSyntax() {
    document
      .querySelector("code.language-yaml")
      .removeAttribute("data-highlighted");
    const codeElements = document.querySelectorAll("code.language-yaml");
    codeElements.forEach((element) => {
      let highlightedHtml = hljs.highlight(element.textContent, {
        language: "yaml",
      }).value;
      element.innerHTML = highlightedHtml;
    });
  }

  // clears empty content to apply placeholder using css
  function clearEmptyContent() {
    if (editorField.textContent.trim() === "") {
      editorField.innerHTML = "";
    }
  }

  function preserveBreaksAndHighlight() {
    editorField.innerHTML = editorField.innerHTML.replace(/<br>/g, "\n");
    highlightSyntax();
  }

  onMount(() => {
    hljs.registerLanguage("yaml", yaml);
    editorField = document.getElementById("editor-field");
    editorField.contentEditable = true; // only set on mount for security reasons

    codeEditor = new CodeEditor(
      "editor",
      "editor-field",
      "line-numbers",
      "overlay",
      fileManager
    );

    textareaValue.set(codeEditor.textarea.innerText);
    preserveBreaksAndHighlight();

    // clear empty content to apply placeholder using css
    clearEmptyContent();

    // Set the default height of the editor wrapper
    const editorWrapperElement = document.querySelector("#editor-wrapper");
    editorWrapperHeightStore.set(editorWrapperElement.offsetHeight);

    // Set the default width of the textarea
    defaultWidth = textarea.offsetWidth;
    adjustTextareaWidth();

    // adjust style on mount
    handleTextareaStyle();

    // Check the syntax on mount
    codeEditor.checkYamlSyntax(codeEditor.textarea.innerText);

    textarea.addEventListener("input", () => {
      clearEmptyContent();
      adjustTextareaWidth();
      handleTextareaStyle();
      highlightSyntaxOnUpdate();
      textareaValue.set(textarea.innerText);
      codeEditor.checkYamlSyntax(textarea.innerText);
    });
    textarea.addEventListener("focus", () => {
      clearEmptyContent();
    });
    // Handles both tab and enter functionalities
    textarea.addEventListener("keydown", (event) => {
      if (event.key === "Tab") {
        // event.preventDefault();
        // const { selectionStart, selectionEnd } = textarea;
        // const value = textarea.innerText;
        // textarea.innerText =
        //   value.substring(0, selectionStart) +
        //   "  " +
        //   value.substring(selectionEnd);
        // textarea.selectionStart = textarea.selectionEnd = selectionStart + 2;
      }
      // } else if (event.key === "Enter") {
      //   event.preventDefault();
      //   const { selectionStart, selectionEnd } = textarea;
      //   const value = textarea.innerText;
      //   const lineStart = value.lastIndexOf("\n", selectionStart - 1) + 1;
      //   let spaceLength = value.substring(lineStart).search(/\S/);
      //   spaceLength = spaceLength === -1 ? 0 : spaceLength;
      //   const spaces = " ".repeat(spaceLength);
      //   textarea.innerText = value.substring(0, selectionEnd + 2) + "\n" + spaces;
      //   textareaValue.set(codeEditor.textarea.innerText);
      else if (event.ctrlKey && event.key === "/") {
        // TODO: Add comment functionality with user fake cursor and selection,
        // also multiple lines should work
      }
    });
  });
  // function handleFormat() {
  //   yamlChecker.yamlCode = textarea.innerText;
  //   textarea.innerText = yamlChecker.formatYAML();
  // }
  function handleSaveLocal() {
    var file = new File([textarea.innerText], fileManager.getCurrentFile(), {
      type: "text;charset=utf-8",
    });

    // Create a link to download the file
    const anchor = document.createElement("a");
    anchor.setAttribute("href", window.URL.createObjectURL(file));
    anchor.setAttribute("download", fileManager.getCurrentFile());
    anchor.click();
    URL.revokeObjectURL(anchor.href);
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
    <GeneralToolbar on:saveLocal={handleSaveLocal} />
    <div class="flex-row">
      <NavController />
      {#if $projectExpanded}
        <div id="navigator-dashboard">
          <FileNavigator
            {codeEditor}
            {fileManager}
            {textareaValue}
            on:fileInteraction={preserveBreaksAndHighlight}
          />
          <Dashboard />
        </div>
      {/if}
      <div class="flex-column2">
        <div id="editor-wrapper">
          <LineNumbers {textareaValue} />
          <!-- svelte-ignore a11y-click-events-have-key-events -->
          <div class="textarea-container">
            <code
              class="language-yaml"
              id="editor-field"
              cols="75"
              rows="30"
              placeholder={$_("editor.editorField.startTypingHere")}
              spellcheck="false"
              bind:this={textarea}
              on:input={handleTextareaStyle}
              style="overflow: hidden; height: auto;"
            ></code>
            <div id="overlay" class="hidden">
              {$_("editor.editorField.dropFileHere")}
            </div>
          </div>
        </div>
        <Problems />
      </div>
    </div>
  </div>
</div>

<style>
  .textarea-container {
    width: 100%;
    padding-top: 3px;
  }
  .main-component {
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
    min-width: 140px;
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
    flex: 1;
    width: 100%;
    margin-top: 38px;
    border: 1px solid var(--silver);
  }

  #editor-field {
    flex: 0 0 auto;
    width: 100%;
    height: 100%;
    line-height: 23px;
    font-size: 1rem;
    font-size: 18px;
    overflow-x: scroll;
    white-space: pre;
    background: transparent;
    border: none;
    outline: none;
    resize: none;
    font-family: var(--ubuntu-mono);
    padding-right: 30px;
  }

  #editor-field:empty:before {
    content: attr(placeholder);
    pointer-events: none;
    display: block;
    height: 95%;
  }

  #editor-field:focus:before {
    content: none;
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
  @media (min-width: 1220px) {
    .main-component {
      width: 1200px;
    }
  }
</style>
