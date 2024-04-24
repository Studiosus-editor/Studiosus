<script>
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";
  import ArrowBack from "../../../assets/svg/arrow-left-icon.svg";
  import AddByEmailBox from "./AddByEmail/AddByEmailBox.svelte";
  import AddByLinkBox from "./AddByLinkBox.svelte";
  import NameProject from "./NameProject.svelte";
  import ToggleComponent from "./ToggleComponent.svelte";

  const backendUrl = __BACKEND_URL__;
  const dispatch = createEventDispatcher();

  let emailEntries = [];
  let currentIndex = 0;
  const firstName = $_("modalCreateProjectScreen.addByLink");
  const secondName = $_("modalCreateProjectScreen.addByEmail");
  const inviteTitle = $_("modalCreateProjectScreen.inviteTitle");
  const createTitle = $_("modalCreateProjectScreen.createProject");

  // add by link is active by default
  let selection = 1;

  let projectName = "";

  // listens for disptached event from ToggleComponent and toggles components
  // inside the CreateProjectScreen component
  function handleToggleEvent(event) {
    selection = event.detail.selection;
  }

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
    await fetch(backendUrl + "/api/createProject/" + projectName, {
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
        return (projects = response.json());
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
      <ToggleComponent on:toggle={handleToggleEvent} {firstName} {secondName} />
      {#if selection === 1}
        <AddByLinkBox showHeader={false} />
      {:else}
        <AddByEmailBox bind:emailEntries showHeader={false} />
      {/if}
    {/if}
  </div>
  <div class="content-wrapper__button-container">
    {#if currentIndex == 0}
      <button
        class="button--blue"
        on:click={handleNext}
        class:button-disabled={projectName.trim() === "" && currentIndex === 0}
        >{$_("modalCreateProjectScreen.next")}</button
      >
    {/if}
    {#if currentIndex == 1}
      <button class="button--blue" on:click={handleSubmitProject}
        >{$_("modalCreateProjectScreen.create")}</button
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

    &__page-content-wrapper {
      margin-top: 24px;
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
