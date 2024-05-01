<script>
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";
  import ArrowBack from "../../../assets/svg/arrow-left-icon.svg";
  import NameProject from "./NameOrRenameProject.svelte";
  import ToggleAddByLinkAndEmail from "./ToggleAddByLinkAndEmail.svelte";

  const backendUrl = __BACKEND_URL__;
  const MAX_PNAME_LENGTH = 22;
  const dispatch = createEventDispatcher();

  let currentIndex = 0;
  let emailEntries = [];

  const inviteTitle = $_("modalCreateProjectScreen.inviteTitle");
  const createTitle = $_("modalCreateProjectScreen.createProject");

  // add by link is active by default
  let selection = 1;

  let projectName = "";

  // goes back to previous page, also resets AddByLinkBox to active
  // when swapping pages, and disptaches an event to change title
  function handlePrevious() {
    dispatch("titleChange", { createTitle });
    currentIndex -= 1;
    selection = 1;
  }

  // dispatches an event that signals that the page switched meaning
  // the title must be changed to second page, also uses it's parent
  // component to set the modal title
  function handleNext() {
    dispatch("titleChange", { inviteTitle });
    currentIndex += 1;
  }

  async function handleSubmitProject() {
    await fetch(backendUrl + "/api/project/create/" + projectName, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .catch((error) => {
        console.error(error.message);
      });
    dispatch("closeModal");
    dispatch("projectCreated");
  }
</script>

<div class="content-wrapper">
  {#if currentIndex != 0}
    <img
      src={ArrowBack}
      alt={$_("modalCreateProjectScreen.altBack")}
      class="content-wrapper__back-icon"
      on:click={handlePrevious}
      on:keydown={handlePrevious}
    />
  {/if}
  <div class="content-wrapper__page-content-wrapper">
    {#if currentIndex === 0}
      <NameProject bind:projectName />
    {:else if currentIndex === 1}
      <ToggleAddByLinkAndEmail
        bind:emailEntries
        on:confirm={handleSubmitProject}
      />
    {/if}
  </div>
  <div class="content-wrapper__button-container">
    {#if currentIndex == 0}
      <button
        class="button--blue"
        on:click={handleNext}
        class:button-disabled={(projectName.trim() === "" &&
          currentIndex === 0) ||
          projectName.length > MAX_PNAME_LENGTH}
        >{$_("modalCreateProjectScreen.next")}</button
      >
    {/if}
  </div>
</div>

<style lang="scss">
  .content-wrapper {
    display: flex;
    flex-direction: column;
    position: relative;

    &__back-icon {
      width: 40px;
      position: absolute;
      top: -90px;
      left: -20px;

      &:hover {
        cursor: pointer;
        transform: scale(1.3);
      }
    }

    &__button-container {
      display: flex;
      justify-content: center;
      width: 100%;
    }
  }

  .button--blue {
    width: 30%;
  }

  .button-disabled {
    background-color: var(--grey43);
    pointer-events: none;
  }
</style>
