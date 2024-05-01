<script>
  import { _ } from "svelte-i18n";
  import AddByEmailBox from "./AddByEmail/AddByEmailBox.svelte";
  import AddByLinkBox from "./AddByLinkBox.svelte";
  import ToggleComponent from "./ToggleComponent.svelte";
  import { createEventDispatcher } from "svelte";

  export let emailEntries = [];
  export let isUpdateComponent = false;
  const dispatch = createEventDispatcher();
  const firstName = $_("toggleAddLinkAndEmail.addByLink");
  const secondName = $_("toggleAddLinkAndEmail.addByEmail");

  // add by link is active by default
  let selection = 1;

  // listens for disptached event from ToggleComponent and toggles components
  function handleToggleEvent(event) {
    selection = event.detail.selection;
  }

  function handleButtonClick() {
    dispatch("confirm");
  }
</script>

<div class="content-wrapper">
  <ToggleComponent on:toggle={handleToggleEvent} {firstName} {secondName} />
  {#if selection === 1}
    <AddByLinkBox showHeader={false} />
  {:else}
    <AddByEmailBox bind:emailEntries showHeader={false} />
  {/if}
  <button class="button--blue" on:click={handleButtonClick}
    >{isUpdateComponent
      ? $_("toggleAddLinkAndEmail.update")
      : $_("toggleAddLinkAndEmail.create")}
  </button>
</div>

<style lang="scss">
  .button--blue {
    width: 30%;
  }
  .content-wrapper {
    margin-top: 24px;
  }
</style>
