<script>
  import interact from "interactjs";
  import { errorStore } from "./scripts/store.js";
  import MinimizeIcon from "../../assets/svg/Problem-toolbar/minimize-icon.svg";
  import ExpandIcon from "../../assets/svg/Problem-toolbar/expand-icon.svg";
  import { _ } from "svelte-i18n";
  import { writable } from "svelte/store";
  let isMinimized = false;
  let originalHeight = "27%";

  $: icon = isMinimized ? ExpandIcon : MinimizeIcon;

  // Make the terminal frame resizable
  interact("#terminal-frame")
    .resizable({
      edges: { top: true, left: false, bottom: false, right: false },
      modifiers: [
        interact.modifiers.restrictSize({
          min: { width: 100, height: 46 },
          max: { width: Infinity, height: 500 },
        }),
      ],
    })
    .on("resizemove", function (event) {
      var target = event.target;

      target.style.width = "100%";
      target.style.height = event.rect.height + "px";
    })
    .on("resizestart", function (event) {
      var target = event.target;
      target.style.borderTop = "2px solid blue";

      // Make #editor-wrapper unclickable
      var editorWrapper = document.querySelector("#editor-wrapper");
      if (editorWrapper) {
        editorWrapper.style.pointerEvents = "none";
      }
    })
    .on("resizeend", function (event) {
      var target = event.target;
      target.style.borderTop = "";

      // Make #editor-wrapper clickable again
      var editorWrapper = document.querySelector("#editor-wrapper");
      if (editorWrapper) {
        editorWrapper.style.pointerEvents = "auto";
      }
    });
  function handleMinimizing() {
    let terminalFrame = document.getElementById("terminal-frame");
    let minimizeProblem = document.getElementById("minimize-problem");
    if (isMinimized) {
      terminalFrame.style.height = originalHeight;
      isMinimized = false;
      minimizeProblem.title = "Minimize Terminal";
    } else {
      terminalFrame.style.height = "42px";
      isMinimized = true;
      minimizeProblem.title = "Expand Terminal";
    }
  }
</script>

<div id="terminal-frame">
  <div id="navFrame">
    <div id="headerFrame">
      <h4 class="terminal-header">{$_("editor.terminal.terminal")}</h4>
    </div>
    <div class="minimize-terminal">
      <button
        title={$_("editor.terminal.minimizeTerminal")}
        id="minimize-problem"
        class="minimize-button"
        on:click={handleMinimizing}
      >
        <img src={icon} alt="Minimize Terminal" width="20px" height="20px" />
      </button>
    </div>
  </div>
  <div id="problem-container">
    <p class="problems">
      {@html $errorStore ? $errorStore.replace(/\n/g, "<br>") : ""}
    </p>
  </div>
</div>

<style>
  #terminal-frame {
    display: flex;
    height: 27%;
    width: 100%;
    background-color: var(--grey85);
    border-right: 1px solid var(--silver);
    border-left: none;
    text-align: left;
    flex-direction: column;
  }
  #navFrame {
    display: flex;
    justify-content: space-between;
    position: relative;
    width: 100%;
    height: 40px;
    border-bottom: 1px solid var(--silver);
  }
  #headerFrame {
    display: flex;
    align-items: center;
    height: 40px;
    background-color: var(--grey85);
  }
  .terminal-header {
    display: flex;
    padding-bottom: 10px;
    font-size: 18px;
    margin: 10px 0 0 10px;
  }
  .minimize-terminal {
    width: 22px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
  }
  .minimize-button {
    display: flex;
    position: relative;
    justify-content: center;
    align-items: center;
    background: transparent;
    border: none;
    margin-right: 4px;
    width: 22px;
    height: 22px;
  }
  .minimize-button:hover {
    transform: scale(1.05);
    transition: transform 0.3s ease;
    background: var(--silver);
    border-radius: 25%;
    cursor: pointer;
  }
  #problem-container {
    overflow-y: auto;
    position: relative;
    display: flex;
    flex-grow: 1;
    width: 100%;
    background-color: var(--grey85);
    text-align: left;
  }
  .problems {
    padding-top: 6px;
    margin: 0;
    padding-bottom: 0;
    display: flex;
    position: relative;
    height: 97%;
    width: 100%;
    padding-left: 10px;
    font-family: Arial, Helvetica, sans-serif;
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
    background: var(--whisper);
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
  @media (max-width: 900px) {
    #headerFrame {
      width: 70%;
    }
    .minimize-terminal {
      width: 30%;
    }
  }
</style>
