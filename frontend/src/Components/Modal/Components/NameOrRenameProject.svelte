<script>
  import { clearableInput } from "../../../utils/clearableInput.js";
  import { _ } from "svelte-i18n";
  import { createEventDispatcher } from "svelte";

  export let projectName = "";
  export let isRenameComponent = false;
  export let projectId = null;

  const backendUrl = __BACKEND_URL__;
  const CURRENT_PNAME = projectName;
  const MAX_PNAME_LENGTH = 22;
  const dispatch = createEventDispatcher();

  async function handleProjectNameChange() {
    await fetch(backendUrl + `/api/project/${projectId}:rename`, {
      method: "PUT",
      headers: {
        Accept: "application/json",
        "Content-Type": "text/plain",
      },
      credentials: "include",
      body: projectName,
    }).then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    });
    dispatch("projectRenamed");
  }
</script>

<div
  class="content-wrapper"
  style={isRenameComponent ? "margin-top: 50px;" : "margin: 118px 0;"}
>
  <div class="content-wrapper__name-wrapper">
    <h3>
      <label for="pname"
        >{isRenameComponent
          ? $_("modalNameOrRenameProject.providenNewName")
          : $_("modalNameOrRenameProject.projectName")}</label
      >
    </h3>
    <input
      type="text"
      id="pname"
      name="pname"
      placeholder={isRenameComponent
        ? projectName
        : $_("modalNameOrRenameProject.placeholderMyProject")}
      bind:value={projectName}
      use:clearableInput
      maxlength={MAX_PNAME_LENGTH}
      on:focus={(event) => (event.target.placeholder = "")}
      on:blur={(event) => {
        if (event.target.value === "") {
          event.target.placeholder = isRenameComponent
            ? ""
            : $_("modalNameOrRenameProject.placeholderMyProject");
        }
      }}
    />
  </div>
  {#if isRenameComponent}
    <div class="content-wrapper__button-container">
      <button
        class="button--blue"
        class:button-disabled={projectName.trim() === "" ||
          projectName === CURRENT_PNAME ||
          projectName.length > MAX_PNAME_LENGTH}
        on:click={handleProjectNameChange}
      >
        {$_("modalNameOrRenameProject.rename")}
      </button>
    </div>
  {/if}
</div>

<style lang="scss">
  .content-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;

    &__name-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 24px;
    }
    &__button-container {
      margin-top: 24px;
      width: 30%;
    }
  }
  input {
    height: 40px;
    text-align: center;
    font-family: "Rubik", sans-serif;
    font-weight: 400;
    font-size: 24px;
    box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.3);
    border-radius: 5px;
    border: 1px solid;
  }

  h3 {
    margin-top: 0;
    font-weight: 500;
  }
  .button-disabled {
    background-color: var(--grey43);
    pointer-events: none;
  }
</style>
