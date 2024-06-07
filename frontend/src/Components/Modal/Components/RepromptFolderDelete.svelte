<script>
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";

  export let folderName;

  const dispatch = createEventDispatcher();

  function handleDelete() {
    dispatch("confirmDelete");
  }

  function handleCancel() {
    dispatch("closeModal");
  }
</script>

<div class="content-wrapper">
  <div class="content-wrapper__warning-wrapper">
    <h3>
      <span class="content-wrapper__warning-text"
        >{$_("modalPromptFolderDelete.warning")}</span
      >
      {$_("modalPromptFolderDelete.firstTextPrompt")}
      <span class="content-wrapper__folder-name">{folderName}</span>
      {$_("modalPromptFolderDelete.secondTextPrompt")}
    </h3>
  </div>
  <div class="content-wrapper__button-container">
    <div class="content-wrapper__button-wrapper">
      <button class="button--blue font-size-20" on:click={handleCancel}>
        {$_("modalPromptFolderDelete.cancel")}
      </button>
    </div>
    <div class="content-wrapper__button-wrapper">
      <button class="button-red font-size-20" on:click={handleDelete}>
        {$_("modalPromptFolderDelete.delete")}</button
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

    &__warning-text {
      color: red;
      font-weight: 500;
    }

    &__folder-name {
      font-weight: 500;
      overflow-wrap: break-word;
      max-width: 100%;
      word-break: break-word;
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
