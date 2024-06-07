<script>
  import { createEventDispatcher, onMount } from "svelte";
  import { _ } from "svelte-i18n";
  import { writable } from "svelte/store";
  import AddByEmailBox from "./AddByEmail/AddByEmailBox.svelte";
  import AddByLinkBox from "./AddByLinkBox.svelte";
  import ToggleComponent from "./ToggleComponent.svelte";

  export let emailEntries = [];
  export let isUpdateComponent = false;
  export let viewerInviteLink = writable("");
  export let editorInviteLink = writable("");
  export let projectId;

  const backendUrl = __BACKEND_URL__;
  let loading = true;

  onMount(async () => {
    await fetch(backendUrl + "/api/project/" + projectId, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const responseJson = await response.json();
        viewerInviteLink.set(responseJson.project.viewerLink);
        editorInviteLink.set(responseJson.project.editorLink);
        emailEntries = [...responseJson.emailRole];
        emailEntries = emailEntries.filter((entry) => entry.role !== 3);
        loading = false;
        return responseJson;
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        loading = false;
        return null;
      });
  });
  const dispatch = createEventDispatcher();
  const firstName = $_("toggleAddLinkAndEmail.addByEmail");
  const secondName = $_("toggleAddLinkAndEmail.addByLink");
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
  {#if loading}
    <p>{$_("toggleAddLinkAndEmail.loading")}</p>
  {:else}
    <ToggleComponent on:toggle={handleToggleEvent} {firstName} {secondName} />
    {#if selection === 1}
      <AddByEmailBox bind:emailEntries showHeader={false} {isUpdateComponent} />
    {:else}
      <AddByLinkBox
        showHeader={false}
        viewerInviteLink={$viewerInviteLink}
        editorInviteLink={$editorInviteLink}
      />
    {/if}
    <button class="button--blue" on:click={handleButtonClick}
      >{isUpdateComponent
        ? $_("toggleAddLinkAndEmail.update")
        : $_("toggleAddLinkAndEmail.create")}
    </button>
  {/if}
</div>

<style lang="scss">
  .button--blue {
    width: 30%;
  }
  .content-wrapper {
    margin-top: 24px;
  }
</style>
