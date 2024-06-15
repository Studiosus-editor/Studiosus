<script>
  import CopyToClipboardIcon from "../../assets/svg/General-toolbar/copy-to-clipboard-iconw.svg";
  import AssistantIcon from "../../assets/svg/General-toolbar/Assistant-iconw.svg";
  import SaveAsIcon from "../../assets/svg/General-toolbar/save-as-iconw.svg";
  import ExpandIcon from "../../assets/svg/General-toolbar/expand-iconw.svg";
  import ToolsIcon from "../../assets/svg/General-toolbar/Tools-icon.svg";
  import UploadFileIcon from "../../assets/svg/General-toolbar/upload-file-icon.svg";
  import DownloadProjectIcon from "../../assets/svg/General-toolbar/download-project-icon.svg";
  import { currentFileNameStore } from "./scripts/store";
  import { createEventDispatcher } from "svelte";
  import { onMount } from "svelte";
  import { _ } from "svelte-i18n";

  export let isTemplate = false;
  export let isViewer = false;

  const dispatch = createEventDispatcher();
  let isLoggedIn = false;
  let isDropdownExtended = false;
  let isAssistantClickable = true;

  function isUserLoggedIn() {
    isLoggedIn = document.cookie.includes("JSESSIONID=");
  }
  function handleSaveLocal() {
    dispatch("saveLocal");
  }
  function handleExpand() {
    dispatch("expand");
  }
  function handleAssistant() {
    if (isAssistantClickable) {
      dispatch("openOverseer");
      isAssistantClickable = false;
      setTimeout(() => {
        isAssistantClickable = true;
      }, 10000);
    }
  }

  function handleCopyToClipboard() {
    dispatch("copyToClipboard");
  }

  function handleFileUpload() {
    document.dispatchEvent(new CustomEvent("file-upload"));
  }

  function handleSaveProject() {
    document.dispatchEvent(new CustomEvent("download-project"));
  }

  onMount(() => {
    isUserLoggedIn();
    const dropbtn = document.querySelector(".dropbtn");
    const dropdownContent = document.querySelector(".dropdown-content");

    dropbtn.addEventListener("click", () => {
      dropdownContent.style.display =
        dropdownContent.style.display === "block" ? "none" : "block";
      isDropdownExtended = dropdownContent.style.display === "block";
    });

    // Close the dropdown if the user clicks outside of it
    window.onclick = function (event) {
      if (!event.target.matches(".dropbtn")) {
        if (dropdownContent.style.display === "block") {
          dropdownContent.style.display = "none";
          isDropdownExtended = false;
        }
      }
    };
  });
</script>

<div id="top-toolbar">
  <div class="menu">
    {#if isLoggedIn && !isViewer && !isTemplate && $currentFileNameStore && ($currentFileNameStore.endsWith(".yml") || $currentFileNameStore.endsWith(".yaml"))}
      <button
        id="Assistant-button"
        class="top-button {isAssistantClickable ? 'enabled' : 'disabled'}"
        title={$_("editor.generalToolbar.assistant")}
        on:click={handleAssistant}
      >
        <div class="title">{$_("editor.generalToolbar.assistant")}</div>
        <img src={AssistantIcon} alt="Assistant" width="25px" height="25px" />
      </button>
    {/if}
    <button
      id="clipboard-button"
      class="top-button"
      title={$_("editor.generalToolbar.copyToClipboard")}
      on:click={handleCopyToClipboard}
    >
      <div class="title">{$_("editor.generalToolbar.copyToClipboard")}</div>
      <img
        src={CopyToClipboardIcon}
        alt="Copy to Clipboard"
        width="20px"
        height="20px"
      />
    </button>
    {#if !isViewer && !isTemplate}
      <button
        id="upload-file-button"
        class="top-button"
        title={$_("editor.generalToolbar.uploadFile")}
        on:click={handleFileUpload}
      >
        <div class="title">{$_("editor.generalToolbar.uploadFile")}</div>
        <img
          src={UploadFileIcon}
          alt={$_("editor.generalToolbar.uploadFile")}
          width="25px"
          height="25px"
        />
      </button>
    {/if}
    <button
      class="top-button"
      id="save-as-button"
      on:click={handleSaveLocal}
      title={$_("editor.generalToolbar.downloadFile")}
    >
      <div class="title">{$_("editor.generalToolbar.downloadFile")}</div>
      <img
        src={SaveAsIcon}
        alt={$_("editor.generalToolbar.downloadFile")}
        width="20px"
        height="20px"
      />
    </button>
    <button
      class="top-button"
      id="save-as-button"
      on:click={handleSaveProject}
      title={$_("editor.generalToolbar.downloadProject")}
    >
      <div class="title">{$_("editor.generalToolbar.downloadProject")}</div>
      <img
        src={DownloadProjectIcon}
        alt={$_("editor.generalToolbar.downloadProject")}
        width="20px"
        height="20px"
      />
    </button>
    <button
      class="top-button"
      id="expand-button"
      on:click={handleExpand}
      title={$_("editor.generalToolbar.expand")}
    >
      <div class="title">{$_("editor.generalToolbar.expand")}</div>
      <img src={ExpandIcon} alt="Expand" width="20px" height="20px" />
    </button>
  </div>
  <div class="dropdown">
    <button class="dropbtn {isDropdownExtended ? 'extended' : ''}">
      <img
        src={ToolsIcon}
        alt="Tools"
        width="30px"
        height="30px"
        style="transform: translateY(5px); pointer-events: none;"
      />
    </button>
    <div class="dropdown-content">
      {#if isLoggedIn && !isViewer && !isTemplate}
        <button
          id="Assistant-button"
          class="top-button {isAssistantClickable ? 'enabled' : 'disabled'}"
          title={$_("editor.generalToolbar.assistant")}
          on:click={handleAssistant}
        >
          <div class="title">{$_("editor.generalToolbar.assistant")}</div>
          <img src={AssistantIcon} alt="Assistant" width="25px" height="25px" />
        </button>
      {/if}
      <button
        id="clipboard-button"
        class="top-button"
        on:click={handleCopyToClipboard}
      >
        <div class="title">{$_("editor.generalToolbar.copyToClipboard")}</div>
        <img
          src={CopyToClipboardIcon}
          alt="Copy to Clipboard"
          width="20px"
          height="20px"
        />
      </button>
      {#if !isViewer && !isTemplate}
        <button
          id="upload-file-button"
          class="top-button"
          title={$_("editor.generalToolbar.uploadFile")}
          on:click={handleFileUpload}
        >
          <div class="title">{$_("editor.generalToolbar.uploadFile")}</div>
          <img
            src={UploadFileIcon}
            alt={$_("editor.generalToolbar.uploadFile")}
            width="20px"
            height="20px"
          />
        </button>
      {/if}
      <button class="top-button" id="save-as-button" on:click={handleSaveLocal}>
        <div class="title">{$_("editor.generalToolbar.downloadFile")}</div>
        <img
          src={SaveAsIcon}
          alt={$_("editor.generalToolbar.downloadFile")}
          width="20px"
          height="20px"
        />
      </button>
      <button
        class="top-button"
        id="save-as-button"
        on:click={handleSaveProject}
        title={$_("editor.generalToolbar.downloadProject")}
      >
        <div class="title">{$_("editor.generalToolbar.downloadProject")}</div>
        <img
          src={DownloadProjectIcon}
          alt={$_("editor.generalToolbar.downloadProject")}
          width="20px"
          height="20px"
        />
      </button>
      <button class="top-button" id="expand-button" on:click={handleExpand}>
        <div class="title">{$_("editor.generalToolbar.expand")}</div>
        <img src={ExpandIcon} alt="Expand" width="20px" height="20px" />
      </button>
    </div>
  </div>
</div>

<style>
  #top-toolbar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    position: relative;
    padding: 0;
    flex: 0 0 auto;
    width: 100%;
    height: 40px;
    border-bottom: 1px solid var(--silver);
    z-index: 4;
  }

  .menu {
    list-style-type: none;
    margin: 0;
    padding: 0;
    display: flex;
  }

  .dropdown {
    display: none;
    position: relative;
  }

  .dropdown-content {
    display: none;
    position: absolute;
    background-color: var(--dodger-blue);
    min-width: 160px;
    border-radius: 8px 0 0 8px;
    right: 0;
    z-index: 1;
  }
  .enabled {
    background-color: var(--dodger-blue);
  }
  .disabled {
    background-color: rgb(147, 147, 165);
    pointer-events: none;
  }
  .dropdown-content button {
    color: var(--white);
    background-color: var(--dodger-blue);
    border: none;
    text-align: left;
    padding: 12px 16px;
    width: 100%;
    cursor: pointer;
  }
  .dropbtn {
    justify-content: center;
    align-items: center;
    background-color: var(--dodger-blue);
    color: var(--white);
    border-radius: 0 0 0 8px;
    padding: 16px;
    cursor: pointer;
    border: none;
    transition: border-radius 0.3s ease;
  }
  .dropbtn.extended {
    border-radius: 0;
  }
  .dropbtn:hover {
    background-color: var(--strong-blue);
  }
  .title {
    display: flex;
    margin-right: 5px;
  }
  .top-button {
    display: flex;
    appearance: none;
    backface-visibility: hidden;
    background-color: var(--dodger-blue);
    border-radius: 10px;
    border-style: none;
    box-shadow: none;
    justify-content: center;
    align-items: center;
    box-sizing: border-box;
    color: var(--white);
    cursor: pointer;
    font-family: "Montserrat", sans-serif;
    font-size: 13px;
    font-weight: 500;
    letter-spacing: normal;
    line-height: 1.5;
    outline: none;
    overflow: hidden;
    padding: 12px 20px;
    width: auto;
    position: relative;
    text-decoration: none;
    transform: translate3d(0, 0, 0);
    transition: all 0.3s;
    user-select: none;
    -webkit-user-select: none;
    touch-action: manipulation;
    vertical-align: top;
    white-space: nowrap;
    margin-left: 10px;
    margin-right: 5px;
  }

  .top-button:hover {
    background-color: var(--strong-blue);
    box-shadow:
      rgba(0, 0, 0, 0.05) 0 5px 30px,
      rgba(0, 0, 0, 0.05) 0 1px 4px;
    opacity: 1;
    transform: translateY(0);
    transition-duration: 0.35s;
  }

  .top-button:active {
    box-shadow:
      rgba(0, 0, 0, 0.1) 0 3px 6px 0,
      rgba(0, 0, 0, 0.1) 0 0 10px 0,
      rgba(0, 0, 0, 0.1) 0 1px 4px -1px;
    transform: translateY(5px);
    transition-duration: 0.35s;
  }

  @media screen and (min-width: 1000px) {
    .menu {
      display: flex;
    }
    .dropdown {
      display: none;
    }
  }
  @media screen and (max-width: 1200px) {
    .title {
      display: none;
    }
  }
  @media screen and (max-width: 920px) {
    .menu {
      display: none;
    }
    .dropdown {
      display: block;
    }
    .title {
      display: flex;
    }
  }
</style>
