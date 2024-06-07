<script>
  import { createEventDispatcher, onDestroy, onMount } from "svelte";
  import { _ } from "svelte-i18n";
  import saveIcon from "../../assets/svg/General-toolbar/save-as-icon.svg";
  import ArrowLeft from "../../assets/svg/arrow-left-icon.svg";
  import ArrowRight from "../../assets/svg/arrow-right-icon.svg";
  import leaveIcon from "../../assets/svg/leave-project-icon.svg";
  import manageTeamIcon from "../../assets/svg/manage-team-icon.svg";
  import openIcon from "../../assets/svg/open-project-icon.svg";
  import renameIcon from "../../assets/svg/rename-icon.svg";
  import deleteIcon from "../../assets/svg/trash-icon.svg";
  import DeleteOrLeave from "../Modal/Components/DeleteOrLeave.svelte";
  import RenameProject from "../Modal/Components/NameOrRenameProject.svelte";
  import ToggleAddByLinkAndEmail from "../Modal/Components/ToggleAddByLinkAndEmail.svelte";
  import CreateTemplateIcon from "../../assets/svg/create-template-icon.svg";
  import CreateTemplate from "../Modal/Components/CreateTemplate.svelte";
  import Modal from "../Modal/Modal.svelte";

  export let isOwner;
  export let project;

  const backendUrl = __BACKEND_URL__;

  const dispatch = createEventDispatcher();
  const BREAKPOINT_WIDTH_FOR_MENU_CLOSE = 820;

  let width;
  let showTeamModal = false;
  let showDeleteModal = false;
  let showRenameModal = false;
  let showTemplateModal = false;
  let showLeaveModal = false;
  let isMenuOpen = true;
  let emailEntries = [];

  $: isMenuOpen = width >= BREAKPOINT_WIDTH_FOR_MENU_CLOSE;

  onMount(() => {
    setWidth();
    window.addEventListener("resize", setWidth);
  });

  function setWidth() {
    width = window.innerWidth;
  }

  function toggleTeamModal() {
    showTeamModal = !showTeamModal;
  }

  function toggleDeleteModal() {
    showDeleteModal = !showDeleteModal;
  }

  function toggleRenameModal() {
    showRenameModal = !showRenameModal;
  }
  function toggleTemplateModal() {
    showTemplateModal = !showTemplateModal;
  }
  function toggleLeaveModal() {
    showLeaveModal = !showLeaveModal;
  }

  function handleProjectRename() {
    updateProjectsPage();
    toggleRenameModal();
  }
  function handleProjectDelete() {
    updateProjectsPage();
    toggleDeleteModal();
  }
  function handleLeaveProject() {
    updateProjectsPage();
    toggleLeaveModal();
  }

  async function handleProjectTeamUpdate() {
    updateProjectsPage();
    toggleTeamModal();
    await fetch(backendUrl + "/api/project/" + project.id + ":update", {
      method: "PUT",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(emailEntries),
    })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        loading = false;
        return null;
      });
  }

  function updateProjectsPage() {
    dispatch("updateProjectsPage");
  }

  function openProject() {
    window.location.href = `/project/${project.id}/${project.role}`;
  }
  function saveProject() {
    // TODO: Implement save project functionality
    console.log("Save project");
  }

  function toggleMenu() {
    isMenuOpen = !isMenuOpen;
  }

  onDestroy(() => {
    window.removeEventListener("resize", setWidth);
  });
</script>

<nav
  class="nav-panel {isMenuOpen ? 'menu-open' : 'menu-close'}"
  style={isOwner ? "" : "top: 40%"}
>
  <ul>
    <li class="nav-panel__icon-container">
      <img
        src={ArrowLeft}
        alt={$_("projectsLeftSideNav.altClose")}
        class="nav-panel__toggle-icon-close"
        on:click={toggleMenu}
        on:keydown={toggleMenu}
      />
      <img
        src={ArrowRight}
        alt={$_("projectsLeftSideNav.altOpen")}
        class="nav-panel__toggle-icon-open {isMenuOpen ? 'transition' : 'show'}"
        on:click={toggleMenu}
        on:keydown={toggleMenu}
      />
    </li>
    <li class="nav-panel__name-wrapper">
      <h3
        class="nav-panel__name-container transition {isMenuOpen
          ? 'show'
          : 'hide'}"
      >
        {project.name}
      </h3>
    </li>
    <li>
      <button
        class="button--default {isMenuOpen ? '' : 'disabled'}"
        on:click={openProject}
        ><img class="nav-panel__icon" src={openIcon} alt="" />
        <h3 class="transition {isMenuOpen ? 'show' : 'hide'}">
          {$_("projectsLeftSideNav.open")}
        </h3></button
      >
    </li>
    <li>
      <button
        class="button--default {isMenuOpen ? '' : 'disabled'}"
        on:click={saveProject}
        ><img class="nav-panel__icon" src={saveIcon} alt="" />
        <h3 class="transition {isMenuOpen ? 'show' : 'hide'}">
          {$_("projectsLeftSideNav.save")}
        </h3></button
      >
    </li>
    {#if isOwner}
      <li>
        <button
          class="button--default {isMenuOpen ? '' : 'disabled'}"
          on:click={toggleTeamModal}
          ><img class="nav-panel__icon" src={manageTeamIcon} alt="" />
          <h3 class="transition {isMenuOpen ? 'show' : 'hide'}">
            {$_("projectsLeftSideNav.team")}
          </h3></button
        >
      </li>
      <li>
        <button
          class="button--default {isMenuOpen ? '' : 'disabled'}"
          on:click={toggleRenameModal}
          ><img class="nav-panel__icon" src={renameIcon} alt="" />
          <h3 class="transition {isMenuOpen ? 'show' : 'hide'}">
            {$_("projectsLeftSideNav.rename")}
          </h3></button
        >
      </li>
      <li>
        <button
          class="button--default {isMenuOpen ? '' : 'disabled'}"
          on:click={toggleTemplateModal}
          ><img class="nav-panel__icon" src={CreateTemplateIcon} alt="" />
          <h3 class="transition {isMenuOpen ? 'show' : 'hide'}">
            {$_("projectsLeftSideNav.share")}
          </h3></button
        >
      </li>
      <li>
        <button
          class="button--default {isMenuOpen ? '' : 'disabled'}"
          on:click={toggleDeleteModal}
          ><img class="nav-panel__icon" src={deleteIcon} alt="" />
          <h3 class="transition {isMenuOpen ? 'show' : 'hide'}">
            {$_("projectsLeftSideNav.delete")}
          </h3></button
        >
      </li>
    {/if}
    {#if !isOwner}
      <li>
        <button
          class="button--default {isMenuOpen ? '' : 'disabled'}"
          on:click={toggleLeaveModal}
          ><img class="nav-panel__icon" src={leaveIcon} alt="" />
          <h3 class="transition {isMenuOpen ? 'show' : 'hide'}">
            {$_("projectsLeftSideNav.leave")}
          </h3></button
        >
      </li>
    {/if}
  </ul>
</nav>
{#if showTeamModal}
  <Modal
    panelName={$_("projectsLeftSideNav.inviteTitle")}
    width="500px"
    on:closeModal={toggleTeamModal}
    ><ToggleAddByLinkAndEmail
      bind:emailEntries
      isUpdateComponent={true}
      projectId={project.id}
      on:confirm={handleProjectTeamUpdate}
    />
  </Modal>
{/if}
{#if showDeleteModal}
  <Modal
    panelName={$_("modalDeleteOrLeave.deleteProject")}
    width="500px"
    on:closeModal={toggleDeleteModal}
    ><DeleteOrLeave
      projectId={project.id}
      on:closeModal={toggleDeleteModal}
      on:projectDeleted={handleProjectDelete}
    /></Modal
  >
{/if}
{#if showRenameModal}
  <Modal
    panelName={$_("modalNameOrRenameProject.renameProject")}
    width="500px"
    on:closeModal={toggleRenameModal}
    ><RenameProject
      isRenameComponent={true}
      projectName={project.name}
      projectId={project.id}
      on:projectRenamed={handleProjectRename}
    /></Modal
  >
{/if}
{#if showLeaveModal}
  <Modal
    panelName={$_("modalDeleteOrLeave.leaveProject")}
    width="500px"
    on:closeModal={toggleLeaveModal}
    ><DeleteOrLeave
      isLeaveComponent={true}
      projectId={project.id}
      on:closeModal={toggleLeaveModal}
      on:projectLeft={handleLeaveProject}
    /></Modal
  >
{/if}
{#if showTemplateModal}
  <Modal
    panelName={$_("modalCreateTemplate.createTemplate")}
    width="500px"
    on:closeModal={toggleTemplateModal}
    ><CreateTemplate
      projectName={project.name}
      projectId={project.id}
      on:closeTemplateModal={toggleTemplateModal}
    /></Modal
  >
{/if}

<style lang="scss">
  .nav-panel {
    opacity: 90%;
    position: fixed;
    top: 30%;
    width: 10vw;
    min-width: 175px;
    background-color: var(--periwinkle);
    overflow-y: auto;
    box-shadow: 2px 0 5px rgba(0, 0, 0, 0.3);
    border-top-right-radius: 25px;
    border-bottom-right-radius: 25px;
    transition: left 0.5s ease;

    ul {
      list-style-type: none;
      padding: 0;
      margin: 0;
    }

    button {
      width: 100%;
      padding: 0px 0px 0px 10px;
      display: flex;
      align-items: center;
      border-bottom: 1px solid black;
      &:hover {
        background-color: var(--spindle);
      }
      h3 {
        width: 100%;
        text-align: left;
      }
    }
    li:last-child button {
      border-bottom: none;
    }

    &__icon {
      width: 40px;
      height: 40px;
      margin-right: 5px;
    }

    &__name-container {
      overflow-wrap: break-word;
      text-align: center;
      margin: 0;
      width: 10vw;
      min-width: 130px;
    }

    &__name-wrapper {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 80px;
      border-bottom: 1px solid black;
      padding-bottom: 10px;
    }

    &__toggle-icon-close {
      width: 40px;
      height: 40px;
      margin-left: 10px;

      &:hover {
        cursor: pointer;
        transform: scale(1.3);
      }
    }

    &__icon-container {
      display: flex;
      justify-content: space-between;
    }
    &__toggle-icon-open {
      width: 40px;
      height: 40px;
      margin-right: 10px;

      &:hover {
        cursor: pointer;
        transform: scale(1.3);
      }
    }
  }

  .menu-open {
    left: 0;
  }

  .transition {
    opacity: 0;
    transition: opacity 0.6s ease;
  }

  .hide {
    opacity: 0;
  }

  .show {
    opacity: 1;
  }
  .disabled {
    pointer-events: none;
  }

  .menu-close {
    left: -110px;
  }

  @media (max-height: 820px) {
    .nav-panel {
      top: 20%;
    }
  }
  @media (max-height: 730px) {
    .nav-panel {
      top: 15%;
    }
  }
</style>
