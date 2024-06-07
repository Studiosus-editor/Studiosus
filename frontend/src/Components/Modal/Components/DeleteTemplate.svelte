<script>
  import { createEventDispatcher } from "svelte";
  import { addToast } from "../../Modal/ToastNotification/toastStore.js";
  import { _ } from "svelte-i18n";

  export let templateName;
  export let templateId;
  export let phase;

  const dispatch = createEventDispatcher();
  const backendUrl = __BACKEND_URL__;

  function handleCancel() {
    dispatch("closeDeleteModal");
  }

  async function deleteYourTemplate() {
    await fetch(backendUrl + `/api/template/${templateId}`, {
      method: "DELETE",
      headers: {
        Accept: "text/plain",
        "Content-Type": "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then(() => {
        dispatch("handleTemplateDeleted");
        addToast({
          message: $_("deleteTemplateModal.alertSuccessfulDeletion"),
          type: "success",
        });
      })
      .catch(() => {
        addToast({
          message: $_("deleteTemplateModal.alertFailedDeletion"),
          type: "error",
        });
      });
  }
</script>

<div class="content-wrapper">
  <div class="content-wrapper__warning-wrapper">
    <h3>{$_("deleteTemplateModal.promptConfirmation")}{templateName}?</h3>
    {#if phase == "APPROVED"}
      <h3>{$_("deleteTemplateModal.approvedWarning")}</h3>
    {:else if phase == "PENDING"}
      <h3>{$_("deleteTemplateModal.pendingWarning")}</h3>
    {/if}
  </div>
  <div class="content-wrapper__button-container">
    <div class="content-wrapper__button-wrapper">
      <button class="button--blue font-size-20" on:click={handleCancel}>
        {$_("deleteTemplateModal.cancel")}
      </button>
    </div>
    <div class="content-wrapper__button-wrapper">
      <button class="button-red font-size-20" on:click={deleteYourTemplate}>
        {$_("deleteTemplateModal.delete")}</button
      >
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
