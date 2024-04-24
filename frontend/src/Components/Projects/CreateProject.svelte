<script>
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";
  import AddIcon from "../../assets/svg/add-plus-icon.svg";
  import CreateProjectScreen from "../Modal/Components/CreateProjectScreen.svelte";
  import Modal from "../Modal/Modal.svelte";

  const dispatch = createEventDispatcher();

  let showModal = false;
  let modalTitle;

  function toggleModal() {
    showModal = !showModal;
    // clear modalTitle when modal is closed
    modalTitle = "";
  }

  // updates modal title based on passed event from CreateProjectScreen component
  function updateModalTitle(event) {
    modalTitle = event.detail.createTitle || event.detail.inviteTitle;
  }

  function updateProjectsPage() {
    dispatch("updateProjectsPage");
  }
</script>

<div class="project-wrapper" on:click={toggleModal} on:keydown={toggleModal}>
  <div class="project-wrapper__name">
    <p>{$_("projects.createProject")}</p>
  </div>
  <div class="project-wrapper__play">
    <img src={AddIcon} alt={$_("projects.addNew")} />
  </div>
</div>

{#if showModal}
  <Modal
    panelName={modalTitle
      ? modalTitle
      : $_("modalCreateProjectScreen.createProject")}
    width="500px"
    on:closeModal={toggleModal}
  >
    <CreateProjectScreen
      on:closeModal={toggleModal}
      on:titleChange={updateModalTitle}
      on:projectCreated={updateProjectsPage}
    />
  </Modal>
{/if}

<style lang="scss">
  .project-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: var(--egg-white);
    width: 40%;
    min-width: 300px;
    margin: 10px;
    padding: 5px 10px;
    box-shadow: 0px 8px 8px 0px rgba(0, 0, 0, 0.3);
    border: 1px solid transparent;

    &__name p {
      margin: 0;
      font-family: "Montserrat", sans-serif;
      font-weight: 400;
      font-size: 20px;
    }

    &__play img {
      width: 40px;
    }

    &:hover {
      cursor: pointer;
      border: 1px solid black;
    }
  }
</style>
