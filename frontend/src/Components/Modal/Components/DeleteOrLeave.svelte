<script>
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";

  export let projectId;
  export let isLeaveComponent = false;

  const dispatch = createEventDispatcher();

  const backendUrl = __BACKEND_URL__;

  function handleAction() {
    if (isLeaveComponent) {
      handleLeave();
    } else {
      handleDelete();
    }
  }

  function handleCancel() {
    dispatch("closeModal");
  }
  async function handleDelete() {
    await fetch(backendUrl + `/api/project/${projectId}`, {
      method: "DELETE",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    }).then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    });
    localStorage.removeItem("templateDesc-" + projectId);
    dispatch("projectDeleted");
  }

  async function handleLeave() {
    await fetch(backendUrl + `/api/project/${projectId}:leave`, {
      method: "PUT",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    }).then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    });
    dispatch("projectLeft");
  }
</script>

<div class="content-wrapper">
  <div class="content-wrapper__warning-wrapper">
    <h3>
      <span>{$_("modalDeleteOrLeave.warningDelete")}</span>
      {isLeaveComponent
        ? $_("modalDeleteOrLeave.warningLeaveText")
        : $_("modalDeleteOrLeave.warningDeleteText")}
    </h3>
  </div>
  <div class="content-wrapper__button-container">
    <div class="content-wrapper__button-wrapper">
      <button class="button--blue font-size-20" on:click={handleCancel}>
        {$_("modalDeleteOrLeave.cancel")}
      </button>
    </div>
    <div class="content-wrapper__button-wrapper">
      <button class="button-red font-size-20" on:click={handleAction}>
        {isLeaveComponent
          ? $_("modalDeleteOrLeave.leave")
          : $_("modalDeleteOrLeave.delete")}
      </button>
    </div>
  </div>
</div>

<style lang="scss">
  .content-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 30px;

    &__warning-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 50px;
    }

    &__button-container {
      display: flex;
      justify-content: space-between;
      width: 100%;
    }

    &__button-wrapper {
      width: 30%;
    }
  }

  h3 {
    margin: 0;
  }

  span {
    color: red;
    font-weight: 500;
  }

  .button-red {
    border-radius: 5px;
    width: 100%;
    height: 30px;
    border: #000 1px solid;
    font-family: "Rubik", sans-serif;
    background-color: var(--bittersweet);
    box-shadow: 0px 8px 8px 0px rgba(0, 0, 0, 0.3);

    &:hover {
      cursor: pointer;
      background-color: var(--mandy);
    }
  }
  .font-size-20 {
    font-size: 20px;
  }
</style>
