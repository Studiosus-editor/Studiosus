<script>
  import { parse } from "yaml";
  import { onMount, tick } from "svelte";
  import Problems from "./Problems.svelte";
  import Dashboard from "./Dashboard.svelte";
  import FileNavigator from "./FileNavigator/FileNavigator.svelte";
  import LineNumbers from "./LineNumbers.svelte";
  import GeneralToolbar from "./GeneralToolbar.svelte";
  import NavController from "./NavigatorController.svelte";
  import {
    currentFileStore,
    textareaValueStore,
    openedRemoteFilesStore,
    connectedUsersStore,
    stompClientStore,
    activeProjectFilesStores,
    currentFileNameStore,
    projectExplorer,
    projectAssistant,
    editorWrapperHeightStore,
    stopClientConnectStore,
    errorStore,
    overseerStore,
  } from "./scripts/store.js";
  import Overseer from "./AssistantOverseer/AIOverseer.svelte";
  import closeIcon from "../../assets/svg/General-toolbar/close-iconw.svg";
  import Assistant from "./AssistantChat/Assistant.svelte";
  import { _ } from "svelte-i18n";
  import interact from "interactjs";
  import hljs from "highlight.js/lib/core";
  import yaml from "highlight.js/lib/languages/yaml";
  import "highlight.js/styles/atom-one-light.css";
  import SockJS from "sockjs-client";
  import { Stomp } from "@stomp/stompjs";
  import { validateYAML } from "./scripts/YamlChecker.js";
  import { addToast } from "../Modal/ToastNotification/toastStore.js";
  import {
    markAndCheckFound,
    handleMouseOver,
    handleMouseOut,
  } from "./Documentation/documentationManager.js";

  export let params = null;
  export let isTemplate = false;

  const backendUrl = __BACKEND_URL__;

  let textarea;
  let defaultWidth;
  let editorElement;
  let controllerColumn;
  let editorField;
  let isOverseerOpen = false;

  function openOverseer() {
    if (isOverseerOpen) {
      document.dispatchEvent(new CustomEvent("refresh-overseer"));
    }
    isOverseerOpen = true;
    overseerStore.set(true);
  }

  function closeOverseer() {
    isOverseerOpen = false;
    overseerStore.set(false);
  }

  let caretOffset = 0;
  let editorWrapperHeight;
  let structure = [];

  const handleCopyToClipboard = () => {
    addToast({ message: $_("toastNotifications.copyToClipboard") });
    navigator.clipboard.writeText(textarea.innerText);
  };

  editorWrapperHeightStore.subscribe((value) => {
    editorWrapperHeight = value;
  });

  const handleTextareaStyle = () => {
    textarea.style.height = "auto";
    textarea.style.height = textarea.scrollHeight + "px";

    if (textarea.innerText !== "") {
      try {
        structure = parse(textarea.innerText);
      } catch (e) {
        // Silence any YAML parsing errors
      }
    }
  };

  //Setting the width of the textarea to the longest line
  const adjustTextareaWidth = () => {
    const lines = textarea.innerText.split("\n");
    let longestLine = lines[0];

    // If textarea is empty, reset to original width
    if (textarea.innerText === "") {
      textarea.style.width = `${defaultWidth}px`;
      return;
    }

    for (let line of lines) {
      if (line.length > longestLine.length) {
        longestLine = line;
      }
    }

    const newWidth = longestLine.length * 9;

    if (newWidth > defaultWidth) {
      textarea.style.width = `${newWidth}px`;
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
    tick().then(() => {
      if (
        !(
          $currentFileNameStore.endsWith(".yml") ||
          $currentFileNameStore.endsWith(".yaml")
        )
      ) {
        return;
      }
      let caretOffset = getCaretOffset();
      highlightSyntax();
      setCaretPosition(caretOffset);
    });
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
    if (textarea.innerText !== "") {
      try {
        structure = parse(textarea.innerText);
      } catch (e) {
        // Silence any YAML parsing errors
      }
    }
    addEventListeners();
  }

  // clears empty content to apply placeholder using css
  function clearEmptyContent() {
    if (editorField.textContent.trim() === "") {
      editorField.innerText = "";
      textareaValueStore.set("");
    }
  }

  function preserveBreaksAndHighlight() {
    editorField.innerHTML = editorField.innerHTML.replace(/<br>/g, "\n");
    tick().then(() => {
      if (
        !(
          $currentFileNameStore.endsWith(".yml") ||
          $currentFileNameStore.endsWith(".yaml")
        )
      ) {
        return;
      }
      highlightSyntax();
    });
  }

  function handleFileMount() {
    if (
      !params ||
      (params.role && (params.role === "OWNER" || params.role === "EDITOR"))
    )
      editorField.contentEditable = true;
    clearEmptyContent();
    preserveBreaksAndHighlight();
    tick().then(() => {
      if (
        !(
          $currentFileNameStore.endsWith(".yml") ||
          $currentFileNameStore.endsWith(".yaml")
        )
      ) {
        errorStore.set("");
        return;
      }
      validateYAML(textarea.innerText);
    });
  }

  let currentErrorSubscription = null;

  $: if (!$currentFileStore) {
    tick().then(() => {
      textarea.innerText = "";
      textarea.contentEditable = false;
      textareaValueStore.set("");
    });

    if (currentErrorSubscription) {
      currentErrorSubscription.unsubscribe();
    }
  } else if ($stopClientConnectStore) {
    if (currentErrorSubscription) {
      currentErrorSubscription.unsubscribe();
    }
    currentErrorSubscription = $stompClientStore.subscribe(
      `/topic/${params.id}/errors/${$currentFileStore}`,
      function (payload) {
        currentFileStore.set(null);
        addToast({
          type: "error",
          message: $_("fileSystemToastNotifications.fileDeleted"),
        });
      }
    );
  }

  onMount(() => {
    hljs.registerLanguage("yaml", yaml);
    editorField = document.getElementById("editor-field");

    // Set the default height of the editor wrapper
    const editorWrapperElement = document.querySelector("#editor-wrapper");
    editorWrapperHeightStore.set(editorWrapperElement.offsetHeight);
    editorElement = document.querySelector(".main-component");
    controllerColumn = document.querySelector("#controller-column");
    // Set the default width of the textarea
    defaultWidth = textarea.offsetWidth;
    adjustTextareaWidth();

    // adjust style on mount
    handleTextareaStyle();

    textarea.addEventListener("input", () => {
      // save to database if its remote project only
      if (params && $openedRemoteFilesStore) {
        updateContent();
      } else {
        localStorage.setItem($currentFileStore, textarea.innerText);
      }
      adjustTextareaWidth();
      handleTextareaStyle();
      if (!params) {
        highlightSyntaxOnUpdate();
      }
      if (!$openedRemoteFilesStore) {
        textareaValueStore.set(textarea.innerText);
      }
      tick().then(() => {
        if (
          !(
            $currentFileNameStore.endsWith(".yml") ||
            $currentFileNameStore.endsWith(".yaml")
          )
        ) {
          errorStore.set("");
          return;
        }
        validateYAML(textarea.innerText);
      });
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
      else if (event.ctrlKey && event.key === "/") {
        // TODO: Add comment functionality with user fake cursor and selection,
        // also multiple lines should work
      }
    });
  });

  function handleSaveLocal() {
    if (textarea.innerText === "") {
      addToast({ message: $_("toastNotifications.emptyFile") });
      return;
    }

    var file = new File([textarea.innerText], {
      type: "text;charset=utf-8",
    });

    const anchor = document.createElement("a");
    anchor.setAttribute("href", window.URL.createObjectURL(file));
    anchor.setAttribute("download", $currentFileNameStore);
    anchor.click();
    URL.revokeObjectURL(anchor.href);
    addToast({ message: $_("toastNotifications.downloadFile") });
  }

  function handleExpand() {
    if (!document.fullscreenElement) {
      editorElement.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
  }
  //Draggable containers for the sidebar
  function setWidths(target, newWidth) {
    const flexColumn = document.querySelector(".flex-column2");
    const totalWidth = target.parentNode.offsetWidth;

    target.style.width = `${newWidth}px`;
    flexColumn.style.width = `${totalWidth - newWidth}px`;
  }

  function setPointerEvents(value) {
    const editorWrapper = document.querySelector("#editor-wrapper");
    if (editorWrapper) {
      editorWrapper.style.pointerEvents = value;
    }
  }

  interact("#controller-column")
    .resizable({
      margin: 20,
      edges: { top: false, left: false, bottom: false, right: true },
      modifiers: [
        interact.modifiers.restrictSize({
          min: { width: 100, height: Infinity },
          max: { width: 600, height: Infinity },
        }),
      ],
    })
    .on("resizemove", (event) => {
      setWidths(event.target, event.rect.width);
    })
    .on("resizestart", (event) => {
      event.target.style.borderRight = "2px solid blue";
      setPointerEvents("none");
    })
    .on("resizeend", (event) => {
      event.target.style.borderRight = "";
      setPointerEvents("auto");
    });

  // Websocket start

  $: if (
    params &&
    params.role &&
    (params.role === "OWNER" || params.role === "EDITOR")
  ) {
    connect();
  }

  let currentSubscription = null;

  $: if (
    params &&
    $openedRemoteFilesStore &&
    params.role &&
    (params.role === "OWNER" || params.role === "EDITOR")
  ) {
    // Unsubscribe from the previous file's updates
    if (currentSubscription) {
      currentSubscription.unsubscribe();
    }
    currentSubscription = $stompClientStore.subscribe(
      `/topic/${params.id}/file/${$openedRemoteFilesStore}`,
      onOnFileUpdated
    );
    $stompClientStore.send(
      `/app/file.getFileContent/${params.id}/file/${$openedRemoteFilesStore}`,
      {},
      {},
      {}
    );
  }

  function connect() {
    let socketFactory = () =>
      new SockJS(`${backendUrl}/ws`, {
        transports: ["websocket"],
        withCredentials: true,
      });
    stompClientStore.set(Stomp.over(socketFactory));

    // silence debug
    $stompClientStore.debug = () => {};

    $stompClientStore.connect({}, onConnected, onError, onDisconnect);

    function onDisconnect(frame) {
      stopClientConnectStore.set(false);
    }
  }

  function onError(error) {
    if (error.headers && error.headers["message"] === "Authorization failed") {
      alert("You need to login to use this feature");
    }
  }

  function onConnected() {
    stopClientConnectStore.set(true);

    try {
      $stompClientStore.subscribe(
        `/topic/${params.id}/users`,
        onUserListReceived
      );

      $stompClientStore.subscribe(
        `/topic/${params.id}/activeFiles`,
        function (message) {
          // Update the list of active files
          const activeFiles = JSON.parse(message.body);
          activeProjectFilesStores.set(activeFiles);
        }
      );

      // ping activeFile lists
      $stompClientStore.send(`/app/file.getActiveFiles/${params.id}`, {}, {});

      // Tell your username to the server
      $stompClientStore.send(`/app/chat.addUser/${params.id}`, {}, {});
    } catch (error) {}
  }

  function onUserListReceived(payload) {
    connectedUsersStore.set(JSON.parse(payload.body));
  }

  function onOnFileUpdated(payload) {
    tick().then(() => {
      if (
        !(
          $currentFileNameStore.endsWith(".yml") ||
          $currentFileNameStore.endsWith(".yaml")
        )
      ) {
        textarea.innerHTML = payload.body;
        textareaValueStore.set(payload.body);
        if (caretOffset !== 0) {
          setCaretPosition(caretOffset);
        }
        return;
      }
      textarea.innerHTML = payload.body;
      textareaValueStore.set(payload.body);
      highlightSyntax();
      if (caretOffset !== 0) {
        setCaretPosition(caretOffset);
      }
    });
  }

  function updateContent() {
    let textContent = textarea.innerText;
    caretOffset = getCaretOffset();
    $stompClientStore.send(
      "/app/file.updateFile/" + params.id + "/file/" + $openedRemoteFilesStore,
      {},
      JSON.stringify({ content: textContent || " " })
    );
  }

  // Websocket end
  //Sidebar toggle control
  $: {
    if (controllerColumn) {
      controllerColumn.style.display =
        $projectExplorer || $projectAssistant ? "flex" : "none";
    }
  }
  let tooltip;
  let spanElements = [];

  function addEventListeners() {
    // Remove old event listeners
    spanElements.forEach((span) => {
      span.removeEventListener("mouseover", handleMouseOver);
      span.removeEventListener("mouseout", handleMouseOut);
    });

    // Get the current span elements
    spanElements = textarea.querySelectorAll("span");

    spanElements.forEach((span) => {
      if (span.classList.contains("hljs-attr")) {
        if (!structure) {
          return;
        }
        const result = markAndCheckFound(
          structure[0],
          span.innerText.replace(/:/g, "")
        );
        if (result.found) {
          span.addEventListener("mouseenter", (event) =>
            handleMouseOver(
              span,
              result,
              span.innerText.replace(/:/g, ""),
              tooltip
            )
          );
          span.addEventListener("mouseleave", handleMouseOut);
        }
      }
    });
  }
</script>

<div class="main-component">
  <div id="editor">
    <GeneralToolbar
      {isTemplate}
      isViewer={params && params.role === "VIEWER"}
      on:saveLocal={handleSaveLocal}
      on:expand={handleExpand}
      on:openOverseer={openOverseer}
      on:copyToClipboard={handleCopyToClipboard}
    />
    <div class="flex-row">
      <NavController {params} />
      <div id="controller-column">
        <div class="navigator-dashboard {$projectExplorer ? '' : 'hidden'}">
          <FileNavigator
            {params}
            {textarea}
            {isTemplate}
            on:fileInteraction={handleFileMount}
          />
          {#if !isTemplate && params && params.role !== "VIEWER"}
            <Dashboard />
          {/if}
        </div>
        {#if $projectAssistant}
          <Assistant />
        {/if}
      </div>
      <div class="flex-column2">
        <div class="current-file">
          <h4>{$currentFileStore !== null ? $currentFileNameStore : ""}</h4>
          {#if isOverseerOpen}
            <button
              id="button--close-assistant"
              title={$_("editor.editorField.closeAssitant")}
              on:click={closeOverseer}
            >
              <img src={closeIcon} alt="..." width="15px" height="15px" />
            </button>
          {/if}
        </div>
        <div id="editor-row">
          <div id="editor-wrapper">
            {#if $currentFileStore}
              <LineNumbers />
            {/if}
            <!-- svelte-ignore a11y-click-events-have-key-events -->
            <div
              id="tooltip"
              style="position: absolute; display: none;"
              bind:this={tooltip}
            >
              Tooltip
            </div>
            <div class="textarea-container">
              <code
                class="language-yaml"
                id="editor-field"
                cols="75"
                rows="30"
                placeholder={$currentFileStore
                  ? isTemplate === false &&
                    params &&
                    (params.role === "EDITOR" || params.role === "OWNER")
                    ? $_("editor.editorField.startTypingHere")
                    : ""
                  : ""}
                spellcheck="false"
                bind:this={textarea}
                on:input={handleTextareaStyle}
                style="overflow: hidden; height: auto;"
              ></code>
              <div class="overlay {$currentFileStore ? 'hidden' : ''}">
                {#if isTemplate || (params && params.role === "VIEWER")}
                  {$_("editor.editorField.pleaseSelectAFileToView")}
                {:else}
                  {$_("editor.editorField.pleaseSelectAFile")}
                {/if}
              </div>
            </div>
          </div>
          {#if isOverseerOpen}
            <Overseer />
          {/if}
        </div>
        <Problems />
      </div>
    </div>
  </div>
</div>

<style>
  #button--close-assistant {
    right: 0;
    bottom: 0;
    position: absolute;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100px;
    height: 50%;
    border-radius: 5px;
    background: var(--dodger-blue);
    border: 1px solid var(--whisper);
    cursor: pointer;
  }
  .current-file {
    position: relative;
    height: 38px;
    width: 100%;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  h4 {
    margin: 0;
  }
  .overlay {
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
    text-align: center;
  }
  .overlay.hidden {
    display: none;
  }
  .navigator-dashboard.hidden {
    display: none;
  }
  .textarea-container {
    position: relative;
    width: 100%;
    padding-top: 3px;
  }
  .main-component {
    display: block;
    top: 0;
    position: relative;
    width: 100%;
    height: 92vh;
    overflow: hidden;
    background-color: var(--white);
  }
  #editor {
    display: block;
    position: relative;
    flex: 0 0 auto;
    width: 100%;
    height: 100%;
    overflow: hidden;
    background-color: transparent;
  }
  #controller-column {
    display: flex;
    flex-direction: column;
    min-width: 175px;
    max-width: 600px;

    height: 100%;
    width: 20%;
    border-right: 1px solid var(--silver);
  }
  .flex-row {
    display: flex;
    flex-direction: row;
    height: 96%;
    width: 100%;
  }

  .navigator-dashboard {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
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
  #editor-row {
    overflow-x: hidden;
    overflow-y: auto;
    display: flex;
    flex-direction: row;
    flex: 1;
    height: 70%;
    width: 100%;
  }
  #editor-wrapper {
    background-color: var(--ghost-white);
    display: flex;
    overflow-y: scroll;
    flex: 1;
    width: 100%;
    height: auto;
    border-top: 1px solid var(--silver);
  }

  #editor-field {
    flex: 0 0 auto;
    width: 100%;
    height: auto;
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

  #editor-field::placeholder {
    color: #000;
    opacity: 0.5;
  }

  #tooltip {
    font-family: "Montserrat", sans-serif;
    z-index: 10;
    padding: 5px 10px;
    border-radius: 25px;
    background-color: white;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
  }

  ::-webkit-scrollbar {
    width: 10px;
    height: 10px;
  }
  /* Track */
  ::-webkit-scrollbar-track {
    box-shadow: none;
    border-radius: 10px;
  }

  /* Handle */
  ::-webkit-scrollbar-thumb {
    background: var(--grey56);
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
</style>
