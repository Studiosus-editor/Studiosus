<script>
  import Projects from "../../assets/svg/Nav-controller/explorerw-icon.svg";
  import Assistant from "../../assets/svg/Nav-controller/chatw.svg";
  import { projectAssistant, projectExplorer } from "./scripts/store.js";
  import CreateTemplateIcon from "../../assets/svg/create-template-icon-white.svg";
  import CreateTemplate from "../Modal/Components/CreateTemplate.svelte";
  import Modal from "../Modal/Modal.svelte";
  import { projectNameStore } from "./scripts/store.js";
  import { _ } from "svelte-i18n";

  export let params = null;

  let showTemplateModal = false;

  function handleExplorerBtn() {
    projectExplorer.update((value) => !value);
    projectAssistant.set(false);
  }

  function handleAssistantBtn() {
    projectExplorer.set(false);
    projectAssistant.update((value) => !value);
  }

  function toggleTemplateModal() {
    showTemplateModal = !showTemplateModal;
  }
</script>

{#if showTemplateModal}
  <Modal
    panelName={$_("modalCreateTemplate.createTemplate")}
    width="500px"
    on:closeModal={toggleTemplateModal}
    ><CreateTemplate
      projectName={$projectNameStore}
      projectId={params.id}
      on:closeTemplateModal={toggleTemplateModal}
    /></Modal
  >
{/if}
<div id="nav-controller">
  <button
    title={$_("editor.navigatorController.explorer")}
    id="projectExpand-btn"
    class="nav-buttons"
    on:click={handleExplorerBtn}
  >
    <img
      src={Projects}
      alt={$_("editor.navigatorController.explorer")}
      width="30px"
      height="30px"
    />
  </button>
  <button
    title={$_("editor.navigatorController.assistant")}
    id="projectAssistant-btn"
    class="nav-buttons"
    on:click={handleAssistantBtn}
  >
    <img
      src={Assistant}
      alt={$_("editor.navigatorController.assistant")}
      width="30px"
      height="30px"
    />
  </button>
  {#if params != null && params.role && params.role === "OWNER"}
    <button
      title={$_("editor.navigatorController.shareTemplate")}
      id="projectAssistant-btn"
      class="nav-buttons"
      on:click={toggleTemplateModal}
    >
      <img
        src={CreateTemplateIcon}
        alt={$_("editor.navigatorController.shareTemplate")}
        width="30px"
        height="30px"
      />
    </button>
  {/if}
</div>

<style>
  #nav-controller {
    position: relative;
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
