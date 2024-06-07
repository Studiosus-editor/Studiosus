<script>
  import { createEventDispatcher } from "svelte";
  import { _ } from "svelte-i18n";

  const dispatch = createEventDispatcher();

  function confirm() {
    dispatch("confirm");
  }

  function cancel() {
    dispatch("cancel");
  }
</script>

<div class="modal">
  <h3>
    {$_("profileTab.passwordContainer.confirmPasswordModal.passwordQuestion")}
  </h3>
  <div class="button-container">
    <div class="button-container__button-wrapper">
      <button class="button--blue font-size-20" on:click={cancel}>
        {$_("profileTab.passwordContainer.confirmPasswordModal.cancelButton")}
      </button>
    </div>
    <div class="button-container__button-wrapper">
      <button class="button-red font-size-20" on:click={confirm}
        >{$_("profileTab.passwordContainer.confirmPasswordModal.changeButton")}
      </button>
    </div>
  </div>
</div>

<style lang="scss">
  .button-container {
    display: flex;
    justify-content: space-between;
    width: 100%;
    &__button-wrapper {
      width: 30%;
    }
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
