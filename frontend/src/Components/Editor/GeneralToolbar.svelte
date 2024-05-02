<script>
  import RunProjectIcon from "../../assets/svg/General-toolbar/run-project-icon.svg";
  import SaveFileIcon from "../../assets/svg/General-toolbar/save-file-icon.svg";
  import FormatCodeIcon from "../../assets/svg/General-toolbar/format-code-icon.svg";
  import CopyToClipboardIcon from "../../assets/svg/General-toolbar/copy-to-clipboard-icon.svg";
  import SaveAsIcon from "../../assets/svg/General-toolbar/save-as-icon.svg";
  import ExpandIcon from "../../assets/svg/General-toolbar/expand-icon.svg";
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";

  const dispatch = createEventDispatcher();

  // function handleClick() {
  //   dispatch("format");
  // }
  function handleSaveLocal() {
    dispatch("saveLocal");
  }
</script>

<div id="top-toolbar">
  <button
    title={$_("editor.generalToolbar.runScript")}
    id="run-button"
    class="top-button"
  >
    <img src={RunProjectIcon} alt="Run Script" width="20px" height="20px" />
  </button>
  <button
    title={$_("editor.generalToolbar.saveProject")}
    id="save-button"
    class="top-button"
  >
    <img src={SaveFileIcon} alt="Save File" width="20px" height="20px" />
  </button>
  <button
    title={$_("editor.generalToolbar.format")}
    id="format-button"
    class="top-button"
  >
    <img src={FormatCodeIcon} alt="Format Code" width="20px" height="20px" />
  </button>
  <button
    title={$_("editor.generalToolbar.copyToClipboard")}
    id="clipboard-button"
    class="top-button"
  >
    <img
      src={CopyToClipboardIcon}
      alt="Copy to Clipboard"
      width="20px"
      height="20px"
    />
  </button>
  <button
    title={$_("editor.generalToolbar.saveAs")}
    id="save-as-button"
    class="top-button"
    on:click={handleSaveLocal}
  >
    <img src={SaveAsIcon} alt="Save As" width="20px" height="20px" />
  </button>
  <button
    title={$_("editor.generalToolbar.expand")}
    id="expand-button"
    class="top-button"
  >
    <img src={ExpandIcon} alt="Expand" width="20px" height="20px" />
  </button>
</div>

<style>
  #top-toolbar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    position: relative;
    flex: 0 0 auto;
    width: 100%;
    height: 33px;
    box-shadow: 0px 0px 12px 0px #343434;
    z-index: 4;
    background-color: var(--grey85);
  }
  .top-button {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    width: 22px;
    height: 22px;
    margin: 3px;
    border: none;
    background-color: transparent;
  }
  .top-button:hover {
    transform: scale(1.05);
    transition: transform 0.3s ease;
    background: var(--silver);
    border-radius: 25%;
    cursor: pointer;
  }
</style>
