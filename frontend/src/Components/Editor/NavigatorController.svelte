<script>
  import Projects from "../../assets/svg/Nav-controller/explorerw-icon.svg";
  import Assistant from "../../assets/svg/Nav-controller/chatw.svg";
  import { projectAssistant, projectExplorer } from "./scripts/store.js";
  import { _ } from "svelte-i18n";

  function handleExplorerBtn() {
    projectExplorer.update((value) => !value);
    projectAssistant.set(false);
  }

  function handleAssistantBtn() {
    projectExplorer.set(false);
    projectAssistant.update((value) => !value);
  }
</script>

<div id="nav-controller">
  <button
    title={$_("editor.navigatorController.explorer")}
    id="projectExpand-btn"
    class="nav-buttons"
    on:click={handleExplorerBtn}
  >
    <img src={Projects} alt="Projects" width="30px" height="30px" />
  </button>
  <button
    title={$_("editor.navigatorController.assistant")}
    id="projectAssistant-btn"
    class="nav-buttons"
    on:click={handleAssistantBtn}
  >
    <img src={Assistant} alt="Projects" width="30px" height="30px" />
  </button>
</div>

<style>
  #nav-controller {
    position: relative;
    z-index: 3;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    width: 50px;
    background: #2f80ed;
    /* border-right: 2px solid var(--silver); */
  }
  .nav-buttons {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    align-items: center;
    background: transparent;
    border: none;
    width: 50px;
    height: 50px;
    cursor: pointer;
  }
  .nav-buttons:hover {
    background-color: #1366d6;
    box-shadow:
      rgba(0, 0, 0, 0.05) 0 5px 30px,
      rgba(0, 0, 0, 0.05) 0 1px 4px;
    opacity: 1;
    transform: translateY(0);
    transition-duration: 0.35s;
  }
</style>
